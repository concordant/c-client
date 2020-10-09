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
import client.utils.CObjectUId

class CObject<T> {

    /**
    * Concordant object unique id
    */
    private val id: CObjectUId

    /**
    * Is openned in read only mode
    */
    private val readOnly: Boolean

    constructor(oid: CObjectUId, readOnly: Boolean) {
        this.id = oid
        this.readOnly = readOnly
    }

    fun update() {
        if (this.readOnly) throw RuntimeException("CObject has been opened in read-only mode.")

        // Should be integrated to CRDTs
        val crdt = CService.getObject<T>(this.id)
        val ts = ActiveSession.environment.tick()
        // crdt.update(ts)
        CService.pushObject<T>(this.id, crdt)
    }

    fun getter(): Any {
        val crdt = CService.getObject<T>(this.id)
        return 1 // crdt.getter()
    }

    fun close() {
    }
}
