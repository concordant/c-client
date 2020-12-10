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

package client.utils

import client.utils.CObjectUId
import crdtlib.crdt.DeltaCRDT
import crdtlib.utils.ClientUId
import io.ktor.client.HttpClient
import io.ktor.client.request.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

class CService {

    companion object {

        fun connect(dbName: String, clientUId: ClientUId): Boolean {
            lateinit var response: String
            runBlocking {
                val client = HttpClient()
                response = client.post<String>{
                    url("http://127.0.0.1:4000/api/create-app")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"$dbName"}"""
                }
            }
            return response == "OK"
        }

        fun <T> getObject(objectUId: CObjectUId<T>): DeltaCRDT {
            runBlocking {
                val client = HttpClient()
                val crdtJson = client.get<String>{
                    url("http://127.0.0.1:4000/api/get-object")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"myapp","id":"$objectUId.name"}"""
                }
            }
            return DeltaCRDT.fromJson(crdtJson) as T
        }

        fun <T> updateObject(objectUId: CObjectUId<T>, crdt: DeltaCRDT) {
            runBlocking {
                val client = HttpClient()
                val crdtJson = crdt.toJson()
                client.post<String>{
                    url("http://127.0.0.1:4000/api/update-object")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"myapp","id":"$objectUId.name", "document":"$crdtJson"}"""
                }
            }
        }

        fun close(clientUId: ClientUId) {
            runBlocking {
                val client = HttpClient()
                client.post<String>{
                    url("http://127.0.0.1:4000/api/delete-app")
                    contentType(ContentType.Application.Json)
                    body = """{"appName":"myapp"}"""
                }
            }
        }
    }
}
