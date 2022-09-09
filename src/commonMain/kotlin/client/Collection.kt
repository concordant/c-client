/*
* MIT License
*
* Copyright © 2022, Concordant and contributors.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
* associated documentation files (the "Software"), to deal in the Software without restriction,
* including without limitation the rights to use, copy, modify, merge, publish, distribute,
* sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all copies or
* substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
* NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package client

import client.utils.ActiveTransaction
import client.utils.CObjectUId
import client.utils.CollectionUId
import client.utils.NotificationHandler
import client.utils.Name
import client.utils.ConsistencyLevel
import client.utils.CServiceAdapter
import crdtlib.crdt.DeltaCRDT
import crdtlib.crdt.DeltaCRDTFactory
import crdtlib.utils.VersionVector
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class CRDTJson(val _id: String)

/**
* This class represents a collection of objects.
*/
class Collection {

    /**
     * The session from which this collection depends.
     */
    private val attachedSession: Session

    /**
     * The collection unique identifier.
     */
    private val id: CollectionUId

    /**
     * Is the collection open in read-only mode.
     */
    private val readOnly: Boolean

    /**
     * Is this collection closed.
     */
    private var isClosed: Boolean = false

    /**
     * Contains all objects (opened or not) within this collection indexed by ID.
     */
    internal val objectsById: MutableMap<CObjectUId, DeltaCRDT> = mutableMapOf()

    /**
     * The objects opened within this collection indexed by reference.
     */
    internal val openedObjectsByRef: MutableMap<DeltaCRDT, Triple<CObjectUId, Boolean, MutableSet<NotificationHandler>>> = mutableMapOf()

    /**
     * Remote updates ready to be pulled.
     */
    internal val waitingPull: MutableMap<CObjectUId, DeltaCRDT> = mutableMapOf()

    /**
     * Default constructor.
     * @param attachedSession the session from which this collection depends.
     * @param id the collection unique identifier.
     * @param readOnly is the collection open in read-only mode.
     */
    internal constructor(attachedSession: Session, id: CollectionUId, readOnly: Boolean) {
        this.attachedSession = attachedSession
        this.id = id
        this.readOnly = readOnly
    }

    /**
     * Get the collection ID.
     */
    @Name("getId")
    fun getId() : String {
        return this.id
    }

    /**
     * Pull remote updates into the current session.
     * @param type is the consistency level of the operation.
     */
    @Name("pull")
    fun pull(type: ConsistencyLevel) {
        for ((k, v) in this.waitingPull) {
            val crdt = this.getObject(k)
            if (crdt != null) {
                crdt.merge(v)
            } else {
                this.objectsById[k] = v
            }
            this.waitingPull.remove(k)
        }
    }

    // c_pull_XX_view(v)
    fun pull(type: ConsistencyLevel, vv: VersionVector) {
        // Not yet implemented
        throw RuntimeException("Method pull is not yet supported.")
    }

    /**
     * Opens an object of the collection.
     * @param objectId the name of the object.
     * @param type type of the object.
     * @param readOnly is the object open in read-only mode.
     * @param handler handler to call when a new update is received.
     */
    @Name("open")
    fun open(objectId: String, type: String, readOnly: Boolean, handler: NotificationHandler = { _, _ -> Unit }): DeltaCRDT {
        if (ActiveTransaction != null) throw RuntimeException("An object cannot be open within a transaction.")
        if (this.isClosed) throw RuntimeException("This collection has been closed.")
        if (this.readOnly && !readOnly) throw RuntimeException("Collection has been opened in read-only mode.")

        val objectUId = CObjectUId(this.id, type, objectId)

        var obj : DeltaCRDT? = this.getObject(objectUId)

        if (obj !== null) {
            if (this.getObjectUId(obj) === null) {
                this.openedObjectsByRef[obj] = Triple(objectUId, readOnly, mutableSetOf(handler))
            } else {
                if (this.isWritable(obj) === readOnly) {
                    throw RuntimeException("Object has been opened in different mode.")
                }
                this.openedObjectsByRef[obj]!!.third!!.add(handler)
            }
            return obj
        }

        obj = DeltaCRDTFactory.createDeltaCRDT(type, this.attachedSession.environment)
        CServiceAdapter.getObject(this.attachedSession.getDbName(), this.attachedSession.getServiceUrl(), objectUId, this)
        this.objectsById[objectUId] = obj
        this.openedObjectsByRef[obj] = Triple(objectUId, readOnly, mutableSetOf(handler))
        return obj
    }

    /**
     * Send a get request for the [obj].
     */
    @Name("forceGet")
    fun forceGet(obj: DeltaCRDT) {
        val objectUId = this.getObjectUId(obj)
        if (objectUId !== null) {
            CServiceAdapter.getObject(this.attachedSession.getDbName(), this.attachedSession.getServiceUrl(), objectUId, this)
        }
    }

    /**
     * Closes this collection.
     */
    @Name("close")
    fun close() {
        this.objectsById.clear()
        this.openedObjectsByRef.clear()

        this.isClosed = true

        this.attachedSession.notifyClosedCollection(this.id)
    }

    /**
     * Get the [obj] of [CObjectUID] or null if not managed by this collection.
     */
    internal fun getObject(objectUId: CObjectUId): DeltaCRDT? {
        return this.objectsById[objectUId]
    }

    /**
     * Get the [CObjectUID] of [obj] or null if not managed by this collection.
     */
    internal fun getObjectUId(obj: DeltaCRDT): CObjectUId? {
        return this.openedObjectsByRef[obj]?.first
    }

    /**
     * Check if [obj] is open and writable.
     */
    internal fun isWritable(obj: DeltaCRDT): Boolean {
        return this.openedObjectsByRef[obj]?.second == false
    }

    /**
     * Get the [NotificationHandler]s of [obj] or null if not managed by this collection
     */
    internal fun getHandlers(obj: DeltaCRDT): MutableSet<NotificationHandler>? {
        return this.openedObjectsByRef[obj]?.third
    }

    /**
     * Adds a incoming update in the cache.
     * and notify the application if a handler is set.
     * @param objectUId UId of the crdt.
     * @param obj new update.
     */
    internal fun newUpdate(objectUId: CObjectUId, obj: DeltaCRDT) {
        this.waitingPull[objectUId] = obj
        val crdt = this.getObject(objectUId)
        if (crdt !== null) {
            val handlers = this.getHandlers(crdt)
            if (handlers !== null) {
                for (handler in handlers) {
                    handler(this.attachedSession.environment.getState(), objectUId)
                }
            }
        }
    }

    /**
     * Called when a new message arrives.
     * @param message new message data.
     */
    internal fun newMessage(message: String) {
        val newCRDT = Json{ignoreUnknownKeys = true}.decodeFromString<CRDTJson>(message)
        val objectUId : CObjectUId = Json.decodeFromString<CObjectUId>(newCRDT._id)
        this.newUpdate(objectUId, DeltaCRDT.fromJson(message))
    }
}
