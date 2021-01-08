/*
* Copyright © 2020, Concordant and contributors.
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
import client.utils.CServiceAdapter
import crdtlib.crdt.DeltaCRDT
import crdtlib.crdt.DeltaCRDTFactory

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
     * The objects opened within this collection.
     */
    internal val openedObjects: MutableMap<DeltaCRDT, Pair<CObjectUId, Boolean>> = mutableMapOf()

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
     * Opens an object of the collection.
     * @param objectId the name of the object.
     * @param readOnly is the object open in read-only mode.
     * @param handler currently not used.
     */
    @Name("open")
    fun open(objectId: String, type:String, readOnly: Boolean, handler: NotificationHandler): DeltaCRDT {
        if (ActiveTransaction != null) throw RuntimeException("An object cannot be open within a transaction.")
        if (this.isClosed) throw RuntimeException("This collection has been closed.")
        if (this.readOnly && !readOnly) throw RuntimeException("Collection has been opened in read-only mode.")

        val objectUId = CObjectUId(this.id, type, objectId)

        val obj : DeltaCRDT = DeltaCRDTFactory.createDeltaCRDT(type, this.attachedSession.environment)
        CServiceAdapter.getObject(this.attachedSession.getDbName(), objectUId, obj)
        this.openedObjects[obj] = Pair(objectUId, readOnly)
        return obj
    }

    /**
     * Closes this collection.
     */
    @Name("close")
    fun close() {
        this.openedObjects.clear()

        this.isClosed = true

        this.attachedSession.notifyClosedCollection(this.id)
    }
}
