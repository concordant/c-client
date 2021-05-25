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

import kotlinx.browser.window


internal val serviceWorkerURL = "c-service-worker.js"
internal var isActive = false

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
 */
actual fun registerServiceWorker() {
    if (isServiceWorkerAvailable()) {
        window.navigator.serviceWorker.register(serviceWorkerURL).then {
            registration -> isActive = true
        }
    }
}

/**
 * Is service worker active?
 * @return true if service worker is registered, otherwise false.
 */
actual fun isServiceWorkerActive(): Boolean {
    return isActive
}
