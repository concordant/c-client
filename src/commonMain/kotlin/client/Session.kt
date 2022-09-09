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

import client.utils.ActiveSession
import client.utils.ActiveTransaction
import client.utils.CObjectUId
import client.utils.CServiceAdapter
import client.utils.CollectionUId
import client.utils.ConsistencyLevel
import client.utils.Name
import client.utils.TransactionBody
import client.utils.generateUUId4
import crdtlib.crdt.DeltaCRDT
import crdtlib.utils.ClientUId

/**
 * Default value for websocket path used in session
 */
const val DEFAULT_WEBSOCKET_PATH = "/"
const val DEFAULT_WEBSOCKET_PORT = 8999

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
     * The WebSocket path
     */
    private val webSocketPath: String

    /**
     * The WebSocket port
     */
    private val webSocketPort: Int


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
    private constructor(dbName: String, serviceUrl: String, clientUId: ClientUId, webSocketPath: String, webSocketPort: Int) {
        this.dbName = dbName
        this.serviceUrl = serviceUrl
        this.clientUId = clientUId
        this.webSocketPath = webSocketPath
        this.webSocketPort = webSocketPort
        this.environment = ClientEnvironment(this, this.clientUId)
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
     * Get the client unique identifier
     */
    @Name("getClientUId")
    fun getClientUId() : ClientUId {
        return this.clientUId
    }

    /**
     * Get the web socket path
     */
    @Name("getWebSocketPath")
    fun getWebSocketPath() : String {
        return this.webSocketPath
    }

    /**
     * Get the web socket port
     */
    @Name("getWebSocketPort")
    fun getWebSocketPort() : Int {
        return this.webSocketPort
    }


    /**
     * Get the map of opened collection
     */
    internal fun getOpenedCollection() : MutableMap<CollectionUId, Collection> {
        return this.openedCollections
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
        CServiceAdapter.subscribe(this.getDbName(), this.getServiceUrl(), collectionUId, this.getClientUId())
        return newCollection
    }

    /**
     * Notifies this session that a collection has been closed.
     * @param collectionUId the closed collection unique identifier.
     */
    internal fun notifyClosedCollection(collectionUId: CollectionUId) {
        this.openedCollections.remove(collectionUId)
        CServiceAdapter.unsubscribe(this.getDbName(), this.getServiceUrl(), collectionUId, this.getClientUId())
    }

    /**
     * Get the [CObjectUID] of [obj]
     *
     * @throws RuntimeException if object is not a DeltaCRDT
     *     managed in this session
     */
    internal fun getObjectUId(obj: DeltaCRDT): CObjectUId {
        return openedCollections.values.elementAtOrNull(0)
            ?.getObjectUId(obj) ?: throw RuntimeException(
                "obj is not a managed DeltaCRDT")
    }

    /**
     * Check if [obj] is open and writable
     */
    internal fun isWritable(obj: DeltaCRDT): Boolean {
        return openedCollections.values.elementAtOrNull(0)
            ?.isWritable(obj) == true
    }

    /**
     * Creates a transaction.
     * @param type the desired consistency level.
     * @param body the transaction body function.
     */
    @Name("transaction")
    fun transaction(type: ConsistencyLevel, body: TransactionBody): Transaction {
        if (ActiveTransaction != null) throw RuntimeException("A transaction is already opened.")

        val transaction = Transaction(this, body)
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
            CServiceAdapter.unsubscribe(this.getDbName(), this.getServiceUrl(), collection.getId(), this.getClientUId())
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
         * @param webSocketPath the path to the web socket
         * @param credentials the credentials provided by the client.
         * @return the client session to communicate with Concordant.
         */
        @Name("connect")
        fun connect(dbName: String, serviceUrl: String,
                    credentials: String,
                    webSocketPath: String = DEFAULT_WEBSOCKET_PATH,
                    webSocketPort: Int = DEFAULT_WEBSOCKET_PORT): Session {
            if (ActiveSession != null) throw RuntimeException("Another session is already active.")
            val clientUId = ClientUId(generateUUId4())
            val session = Session(dbName, serviceUrl, clientUId, webSocketPath, webSocketPort)
            ActiveSession = session
            CServiceAdapter.connect(dbName, serviceUrl, session)
            return session
        }
    }
}
