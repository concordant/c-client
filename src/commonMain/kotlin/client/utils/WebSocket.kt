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

import client.Session
import client.utils.CObjectUId
import crdtlib.crdt.DeltaCRDT
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*

var client : HttpClient? = null

val DISCONNECTTIMEOUT = 5000L
val SERVERERRORTIMEOUT = 30000L

/**
 * Open a web socket connection.
 * @param session The actual session
 */
fun connectWebSocket(session: Session) {
    disconnectWebSocket()
    client = HttpClient {
        install(WebSockets)
    }
    val expr = """https?:\/\/(www\.)?([-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6})\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)"""
    var regex = expr.toRegex()
    val host = regex.find(session.getServiceUrl())!!.groupValues!!.get(2)
    GlobalScope.launch {
        client?.webSocket(method = HttpMethod.Get, host = host, port = session.getWebSocketPort(), path = session.getWebSocketPath()) {
            send("""{"appName":""""+session.getDbName()+"""","userId":""""+session.getClientUId()+""""}""")
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val collection = session.openedCollections.values.elementAtOrNull(0)
                            ?: throw RuntimeException("There is no opened collection.")
                        collection.newMessage(frame.readText())
                    }
                    is Frame.Close -> {
                        delay(DISCONNECTTIMEOUT)
                        connectWebSocket(session)
                    }
                    else -> {
                        throw RuntimeException("Error Other Frame Type : " + frame)
                    }
                }
            }
        }
    }
}

/**
 * Close the web socket connection.
 */
fun disconnectWebSocket() {
    client?.close()
}
