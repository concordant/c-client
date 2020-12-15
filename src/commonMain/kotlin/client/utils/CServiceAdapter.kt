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

import crdtlib.crdt.DeltaCRDT
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class CServiceAdapter {
    companion object {
        suspend fun connect(dbName: String): Boolean {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/create-app")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName"}"""
            }
            client.close()
            return resp == "\"OK\""
        }

        suspend fun getObjects(dbName: String): String {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/get-objects")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName"}"""
            }
            client.close()
            return resp
        }

        suspend fun getObject(dbName: String, myid: String): String {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/get-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"$myid"}"""
            }
            client.close()
            return resp
        }

        suspend fun updateObject(dbName: String, myid: String, mydoc: String): Boolean {
            val client = HttpClient()
            val resp = client.post<String> {
                url("http://127.0.0.1:4000/api/update-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"$myid", "document":"$mydoc"}"""
            }
            client.close()
            return resp == "\"OK\""
        }

        suspend fun getObject(dbName: String, objectUId: CObjectUId): DeltaCRDT {
            val client = HttpClient()
            val crdtJson = client.post<String>{
                url("http://127.0.0.1:4000/api/get-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"$objectUId.name"}"""
            }
            client.close()
            return DeltaCRDT.fromJson(crdtJson)
        }

        suspend fun updateObject(dbName: String, objectUId: CObjectUId, crdt: DeltaCRDT): Boolean{
            val client = HttpClient()
            val crdtJson = crdt.toJson()
            val resp = client.post<String>{
                url("http://127.0.0.1:4000/api/update-object")
                contentType(ContentType.Application.Json)
                body = """{"appName":"$dbName","id":"$objectUId.name", "document":"$crdtJson"}"""
            }
            client.close()
            return resp == "\"OK\""
        }

        suspend fun close(dbName: String): Boolean{
            return true
        }
    }
}
