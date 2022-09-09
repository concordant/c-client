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

import crdtlib.crdt.DeltaCRDT
import client.Session
import client.utils.ActiveTransaction
import client.utils.CServiceAdapter
import client.utils.TransactionBody
import client.utils.Name

/**
* This class represents a transaction.
*/
class Transaction {

    private val session: Session;

    /**
     * Objects modified in this transaction.
     */
    private val dirtyObjects: MutableSet<DeltaCRDT> = mutableSetOf();

    /**
     * The transaction body function.
     */
     private val body: TransactionBody

    /**
     * Default constructor.
     * @param body the function body of the transaction.
     */
    internal constructor(session: Session, body: TransactionBody) {
        this.session = session
        this.body = body
    }

    /**
     * Launches the transaction.
     */
     internal fun launch() {
         try {
            this.body()
            this.commit()
        } finally {
            ActiveTransaction = null
        }
     }

    /**
     * On write handler called after an updater function on an object.
     * @param obj the object on which updater was called.
     * @param delta the delta generated by the updater function.
     */
    internal fun onWrite(obj: DeltaCRDT, delta: DeltaCRDT) {
        // assert obj is opened and writable
        if (! session.isWritable(obj)) {
            throw RuntimeException("Object is not writable.")
        }
        dirtyObjects.add(obj)
    }

    /**
     * Commits this transaction.
     */
    @Name("commit")
    fun commit() {
        for (obj in dirtyObjects) {
            // Push the new local version to backend
            CServiceAdapter.updateObject(session.getDbName(),
                                         session.getServiceUrl(),
                                         session.getObjectUId(obj),
                                         obj)
        }
        dirtyObjects.clear()
    }

    /**
     * Aborts this transaction.
     */
    @Name("abort")
    fun abort() {
        throw RuntimeException("Transaction abort is not supported yet.")
    }
}
