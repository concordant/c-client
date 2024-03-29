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

package client.utils

import client.Collection
import client.Session
import crdtlib.crdt.DeltaCRDT
import crdtlib.utils.ClientUId
import crdtlib.utils.Environment
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Adapter to the Concordant service (C-Service).
 */
class CServiceAdapter {
    companion object {

        /**
         * Connection to the database
         * @param dbName database name
         */
        fun connect(dbName: String, serviceUrl: String, session: Session) {
            GlobalScope.launch {
                if (isServiceWorkerAvailable()) {
                    registerServiceWorker(session)
                } else {
                    connectWebSocket(session)
                }
                val client = HttpClient()
                val resp = client.post<String> {
                    url("$serviceUrl/api/create-app")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"$dbName"}"""
                }
                client.close()
            }
        }

        /**
         * Get a CRDT from the database
         * @param dbName database name
         * @param objectUId crdt id
         * @param target the delta crdt in which distant value should be merged
         */
        fun getObject(dbName: String, serviceUrl: String, objectUId: CObjectUId, collection: Collection) {
            GlobalScope.launch {
                val client = HttpClient()
                try {
                    var crdtJson = client.post<String>{
                        url("$serviceUrl/api/get-object")
                        contentType(ContentType.Application.Json)
                        body = """{"appName":"$dbName","id":"${Json.encodeToString(objectUId).replace("\"","\\\"")}"}"""
                    }
                    crdtJson = crdtJson.removePrefix("\"").removeSuffix("\"")
                    crdtJson = crdtJson.replace("\\\\\\\"", "\\\\\""); // replace \\\" with \\"
                    crdtJson = crdtJson.replace("\\\\'", "'"); // replace \\' with '
                    crdtJson = crdtJson.replace("\\\\n", "\\n"); // replace \\n with \n
                    crdtJson = crdtJson.replace("\\\\\\", "\\\\"); // replace \\\ with \\
                    crdtJson = crdtJson.replace("\\\"", "\""); // replace \" with "

                    collection.newUpdate(objectUId, DeltaCRDT.fromJson(crdtJson))
                } catch (e: Exception) {
                    delay(1000L)
                } finally {
                    client.close()
                }
            }
        }

        /**
         * Update a CRDT
         * @param dbName database name
         * @param objectUId CRDT id
         * @param crdt new crdt
         */
        fun updateObject(dbName: String, serviceUrl: String, objectUId: CObjectUId, crdt: DeltaCRDT) {
            GlobalScope.launch {
                while (true) {
                    val client = HttpClient()
                    try {
                        var crdtJson = crdt.toJson().replace("\\\\", "\\\\\\"); // replace \\ with \\\
                        crdtJson = crdtJson.replace("\\\"", "\\\\\""); // replace \" with \\"
                        crdtJson = crdtJson.replace("'", "\\\\'"); // replace ' with \\'
                        crdtJson = crdtJson.replace("\\n", "\\\\n"); // replace \n with \\n
                        crdtJson = crdtJson.replace("\"", "\\\""); // replace " with \"
                        client.post<String> {
                            url("$serviceUrl/api/update-object")
                            contentType(ContentType.Application.Json)
                            body = """{"appName":"$dbName","id":"${
                                Json.encodeToString(objectUId).replace("\"", "\\\"")
                            }", "document":"$crdtJson"}"""
                        }
                        break
                    } catch (e: Exception) {
                        delay(1000L)
                    } finally {
                        client.close()
                    }
                }
            }
        }

        /**
         * Close the connection to the database
         * @param dbName database name
         */
        fun close(dbName: String, serviceUrl: String) {
            GlobalScope.launch {
                disconnectWebSocket()
            }
        }

        /**
         * Delete the database
         * @param dbName database name
         */
        fun delete(dbName: String, serviceUrl: String) {
            GlobalScope.launch {
                val client = HttpClient()
                val resp = client.post<String> {
                    url("$serviceUrl/api/delete-app")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"$dbName"}"""
                }
                client.close()
            }
        }

        /**
         * Subscribe to the collection notification
         * @param dbName the database name
         * @param collectionUId the collection id
         */
        fun subscribe(dbName: String, serviceUrl: String, collectionUId: CollectionUId, userId: ClientUId) {
            GlobalScope.launch {
                val client = HttpClient()
                val resp = client.post<String> {
                    url("$serviceUrl/api/subscribe")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"$dbName","collectionUId":"$collectionUId","userId":"$userId"}"""
                }
                client.close()
            }
        }

        /**
         * Unsubscribe to the collection notification
         * @param dbName the database name
         * @param collectionUId the collection id
         */
        fun unsubscribe(dbName: String, serviceUrl: String, collectionUId: CollectionUId, userId: ClientUId) {
            GlobalScope.launch {
                val client = HttpClient()
                val resp = client.post<String> {
                    url("$serviceUrl/api/unsubscribe")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"$dbName","collectionUId":"$collectionUId","userId":"$userId"}"""
                }
                client.close()
            }
        }
    }
}
