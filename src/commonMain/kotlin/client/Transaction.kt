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
import client.utils.TransactionBody
import client.utils.Name

/**
* This class represents a transaction.
*/
class Transaction {

    /**
     * The transaction body function.
     */
     private val body: TransactionBody

    /**
     * Default constructor.
     * @param body the function body of the transaction.
     */
    internal constructor(body: TransactionBody) {
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
     * Commits this transaction.
     */
    @Name("commit")
    fun commit() { }

    /**
     * Aborts this transaction.
     */
    @Name("abort")
    fun abort() {
        throw RuntimeException("Transaction abort is not supported yet.")
    }
}
