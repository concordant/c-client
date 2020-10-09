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
import client.utils.CollectionUId
import client.utils.ConsistencyLevel
import crdtlib.utils.ClientUId
import crdtlib.utils.SimpleEnvironment
import crdtlib.utils.VersionVector

class Session {

    /**
    * The client unique id
    */
    private val clientUId: ClientUId

    /**
    * The environment linked to the session
    */
    public val environment: SimpleEnvironment

    private constructor(cid: ClientUId) {
        this.clientUId = cid
        this.environment = SimpleEnvironment(this.clientUId)
    }

    // c_pull_XX_view
    fun pull(type: ConsistencyLevel) {
    }

    // c_pull_XX_view(v)
    fun pull(type: ConsistencyLevel, vv: VersionVector) {
    }
  
    // c_open_collection_read|write
    fun openCollection(cid: CollectionUId, readOnly: Boolean): Collection {
        return Collection(cid, readOnly)
    }

    // c_end_session
    fun close() {
        CService.close(this.clientUId)
        ActiveSession = null
    }

    companion object {
        // c_begin_session
        fun connect(dbName: String, credentials: String): Session {
            val clientUId = ClientUId("MY_ID")
            if (!CService.connect(dbName, clientUId)) throw RuntimeException("Connection to server failed.")
            val session = Session(clientUId)
            ActiveSession = session
            return session
        }
    }
}
