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
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

internal val serviceWorkerURL = "c-service-worker.js"
internal var isActive = false

@Serializable
data class Message(val type: String, val data: String)

/**
 * Is service worker feature available?
 * @return true if service worker is available, false otherwise
 */
actual fun isServiceWorkerAvailable(): Boolean {
    // Is code running in a browser? And is service worker feature supported?
    return js("typeof window !== 'undefined'") &&
        js("typeof window.navigator.serviceWorker !== 'undefined'")
}

/**
 * Register the service worker.
 * @param session the actual session
 */
actual fun registerServiceWorker(session: Session) {
    if (isServiceWorkerAvailable()) {
        window.navigator.serviceWorker.register(serviceWorkerURL).then(
            onFulfilled = {
                registration -> isActive = true
            },
            onRejected = {
                connectWebSocket(session)
            }
        )
        window.navigator.serviceWorker.oncontrollerchange = {
            session.getOpenedCollection().keys.forEach({
                CServiceAdapter.unsubscribe(session.getDbName(), session.getServiceUrl(), it, session.getClientUId())
                CServiceAdapter.subscribe(session.getDbName(), session.getServiceUrl(), it, session.getClientUId())
            })
        }
        window.navigator.serviceWorker.onmessage = {
            val msg = Json.decodeFromString<Message>("" + it.data)
            if (msg.type === "update") {
                val collection = session.openedCollections.values.elementAtOrNull(0)
                    ?: throw RuntimeException("There is no opened collection.")
                collection.newMessage(msg.data)
            } else {
                throw RuntimeException("Unknown message type received from the service worker : " + msg)
            }
        }
    } else {
        throw RuntimeException("Services worker not supported by the browser.")
    }
}

/**
 * Is service worker active?
 * @return true if service worker is registered, otherwise false.
 */
actual fun isServiceWorkerActive(): Boolean {
    return isActive
}
