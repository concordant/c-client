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
import crdtlib.crdt.DeltaCRDTFactory
import crdtlib.crdt.PNCounter
import crdtlib.utils.ClientUId
import crdtlib.utils.SimpleEnvironment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

class CServiceAdapterTest : StringSpec({
    // This test is disabled, as it currently fails on CI. See issue #37.
    "!connect to c-service create, write twice, read and delete" {
        CServiceAdapter.delete("myapp", "http://127.0.0.1:4000")
        delay(300)
        CServiceAdapter.connect("myapp", "http://127.0.0.1:4000")
        delay(300)

        val uid = ClientUId("clientid")
        val my_env = SimpleEnvironment(uid)

        val objectUId = CObjectUId("myCollection", "PNCounter", "myPNCounter")

        val my_crdt : DeltaCRDT = DeltaCRDTFactory.createDeltaCRDT("PNCounter", my_env)
        CServiceAdapter.getObject("myapp", "http://127.0.0.1:4000", objectUId, my_crdt)
        delay(300)
        my_crdt.toJson().shouldBe("{\"type\":\"PNCounter\",\"metadata\":{\"increment\":[],\"decrement\":[]},\"value\":0}")

        if (my_crdt is PNCounter) {
            my_crdt.increment(10)
            CServiceAdapter.updateObject("myapp", "http://127.0.0.1:4000", objectUId, my_crdt)
            my_crdt.decrement(5)
            CServiceAdapter.updateObject("myapp", "http://127.0.0.1:4000", objectUId, my_crdt)
            delay(300)

            val my_crdt2 : DeltaCRDT = DeltaCRDTFactory.createDeltaCRDT("PNCounter", my_env)
            CServiceAdapter.getObject("myapp", "http://127.0.0.1:4000", objectUId, my_crdt2)
            delay(300)
            val text = "{\"type\":\"PNCounter\",\"metadata\":{\"increment\":[{\"name\":\"clientid\"},{\"first\":10,\"second\":{\"uid\":{\"name\":\"clientid\"},\"cnt\":-2147483647}}],\"decrement\":[{\"name\":\"clientid\"},{\"first\":5,\"second\":{\"uid\":{\"name\":\"clientid\"},\"cnt\":-2147483646}}]},\"value\":5}"
            my_crdt2.toJson().shouldBe(text)
        }

        CServiceAdapter.close("myapp", "http://127.0.0.1:4000")
    }
})
