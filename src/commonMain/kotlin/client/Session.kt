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
import client.utils.CServiceAdapter
import client.utils.CollectionUId
import client.utils.ConsistencyLevel
import client.utils.Name
import client.utils.TransactionBody
import client.utils.generateUUId4
import crdtlib.utils.ClientUId
import crdtlib.utils.VersionVector

/**
* Class representing a client session.
*/
class Session {

    /**
     * Database name
     */
    private val dbName: String

    /**
     * C-Service URL
     */
    private val serviceUrl: String

    /**
     * The client unique identifier
     */
    private val clientUId: ClientUId

    /**
     * The environment linked to the session
     */
    internal val environment: ClientEnvironment

    /**
     * The collections opened within this session.
     */
    internal val openedCollections: MutableMap<CollectionUId, Collection> = mutableMapOf()

    /**
     * Is this session closed.
     */
    internal var isClosed: Boolean = false

    /**
     * Private constructor.
     * @param clientUId the client unique identifier.
     */
    private constructor(dbName: String, serviceUrl: String, clientUId: ClientUId) {
        this.dbName = dbName
        this.serviceUrl = serviceUrl
        this.clientUId = clientUId
        this.environment = ClientEnvironment(this, this.clientUId)
    }

    // c_pull_XX_view
    fun pull(type: ConsistencyLevel) {
        // Not yet implemented
        throw RuntimeException("Method pull is not yet supported.")
    }

    // c_pull_XX_view(v)
    fun pull(type: ConsistencyLevel, vv: VersionVector) {
        // Not yet implemented
        throw RuntimeException("Method pull is not yet supported.")
    }

    /**
     * Get the database name
     */
    @Name("getDbName")
    fun getDbName() : String {
        return this.dbName
    }

    /**
     * Get the C-Service URL
     */
    @Name("getServiceUrl")
    fun getServiceUrl() : String {
        return this.serviceUrl
    }

    /**
     * Opens a given collection with the given read-only mode.
     * @param collectionUId the collection unique identifier.
     * @param readOnly is read-only mode activated.
     * @return the corresponding collection.
     */
    @Name("openCollection")
    fun openCollection(collectionUId: CollectionUId, readOnly: Boolean): Collection {
        if (ActiveTransaction != null) throw RuntimeException("A collection cannot be open within a transaction.")
        if (this.isClosed) throw RuntimeException("This session has been closed.")
        if (this.openedCollections.isNotEmpty()) throw RuntimeException("A collection is already opened.")

        val newCollection = Collection(this, collectionUId, readOnly)
        this.openedCollections[collectionUId] = newCollection
        return newCollection
    }

    /**
     * Notifies this session that a collection has been closed.
     * @param collectionUId the closed collection unique identifier.
     */
    internal fun notifyClosedCollection(collectionUId: CollectionUId) {
        this.openedCollections.remove(collectionUId)
    }

    /**
     * Creates a transaction.
     * @param type the desired consistency level.
     * @param body the transaction body function.
     */
    @Name("transaction")
    fun transaction(type: ConsistencyLevel, body: TransactionBody): Transaction {
        if (ActiveTransaction != null) throw RuntimeException("A transaction is already opened.")

        val transaction = Transaction(body)
        ActiveTransaction = transaction
        transaction.launch()
        return transaction
    }

    /**
     * Closes this session.
     */
    @Name("close")
    fun close() {
        if(this.isClosed) return

        val collections = this.openedCollections.values
        for (collection in collections) {
            collection.close()
        }

        ActiveSession = null

        this.isClosed = true
        CServiceAdapter.close(this.dbName, this.serviceUrl)
    }

    companion object {
        /**
         * Connects a client to Concordant platform.
         * @param dbName the database name that should be accessed.
         * @param credentials the credentials provided by the client.
         * @return the client session to communicate with Concordant.
         */
        @Name("connect")
        fun connect(dbName: String, serviceUrl: String, credentials: String): Session {
            if (ActiveSession != null) throw RuntimeException("Another session is already active.")
            CServiceAdapter.connect(dbName, serviceUrl)

            val clientUId = ClientUId(generateUUId4())
            val session = Session(dbName, serviceUrl, clientUId)
            ActiveSession = session
            return session
        }
    }
}
