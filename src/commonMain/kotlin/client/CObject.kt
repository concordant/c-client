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

import client.utils.ActiveSession
import client.utils.ActiveTransaction
import client.utils.CObjectUId
import client.utils.CService
import client.utils.OperationUId
import crdtlib.crdt.DeltaCRDT

/**
* Class representing a Concordant object.
*/
open class CObject<T> {

    /**
     * The collection from which this objects depends.
     */
    private val attachedCollection: Collection

    /**
     * The Concordant object unique identifier.
     */
    private val id: CObjectUId<T>

    /**
     * Is object opened in read-only mode.
     */
    private val readOnly: Boolean

    /**
     * The encapsulated CRDT.
     */
    protected var crdt: DeltaCRDT<T>? = null

    /**
     * Is object closed.
     */
    private var isClosed: Boolean = false

    /**
     * Default constructor.
     * @param attachedCollection the collection from which this objects depends.
     * @param id Concordant object unique identifier.
     * @param readOnly is object opened in read-only mode.
     */
    internal constructor(attachedCollection: Collection, id: CObjectUId<T>, readOnly: Boolean) {
        this.attachedCollection = attachedCollection
        this.id = id
        this.readOnly = readOnly
    }

    protected fun beforeUpdate(): OperationUId {
        if (ActiveTransaction == null) throw RuntimeException("Object function should be call within a transaction.")
        if (this.isClosed) throw RuntimeException("This object has been closed.")
        if (this.readOnly) throw RuntimeException("This object has been opened in read-only mode.")

        this.crdt = CService.getObject<T>(this.id)
        return (ActiveSession as Session).environment.tick()
    }

    protected fun afterUpdate() {
        CService.updateObject<T>(this.id, this.crdt)
    }

    protected fun beforeGetter() {
        if (ActiveTransaction == null) throw RuntimeException("Object function should be call within a transaction.")
        if (this.isClosed) throw RuntimeException("This object has been closed.")

        this.crdt = CService.getObject<T>(this.id)
    }

    protected fun afterGetter() {
    }

    /**
     * Closes this object.
     */
    fun close() {
        this.isClosed = true

        this.attachedCollection.notifyClosedObject(this.id as CObjectUId<Any>)
    }
}
