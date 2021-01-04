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
import crdtlib.crdt.PNCounter
import crdtlib.utils.ClientUId
import crdtlib.utils.SimpleEnvironment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import kotlinx.coroutines.delay

class CServiceAdapterTest : StringSpec({
    "connect to c-service create, write twice, read and delete" {
        CServiceAdapter.connect("myapp").shouldBeTrue()
        delay(200)

        val uid = ClientUId("clientid")
        val my_env = SimpleEnvironment(uid)

        val objectUId = CObjectUId("myCollection", "PNCounter", "myPNCounter")
        val my_crdt : DeltaCRDT = CServiceAdapter.getObject("myapp", objectUId, my_env)
        delay(200)

        my_crdt.toJson().shouldBe("{\"type\":\"PNCounter\",\"metadata\":{\"increment\":[],\"decrement\":[]},\"value\":0}")
        delay(200)

        if (my_crdt is PNCounter) {
            my_crdt.increment(10)
            CServiceAdapter.updateObject("myapp", objectUId, my_crdt).shouldBeTrue()
            my_crdt.decrement(5)
            CServiceAdapter.updateObject("myapp", objectUId, my_crdt).shouldBeTrue()
            delay(200)
            val text = "{\"type\":\"PNCounter\",\"metadata\":{\"increment\":[{\"name\":\"clientid\"},{\"first\":10,\"second\":{\"uid\":{\"name\":\"clientid\"},\"cnt\":-2147483647}}],\"decrement\":[{\"name\":\"clientid\"},{\"first\":5,\"second\":{\"uid\":{\"name\":\"clientid\"},\"cnt\":-2147483646}}]},\"value\":5}"
            CServiceAdapter.getObject("myapp", objectUId, my_env).toJson().shouldBe(text)
            delay(200)
//            val regex = """\[\"\{\\\"_id\\\":\\\"myid\\\",\\\"_rev\\\":\\\"(\d+)-(\w+)\\\",\\\"type\\\":\\\"PNCounter\\\",\\\"metadata\\\":\{\\\"increment\\\":\[\],\\\"decrement\\\":\[\]\},\\\"value\\\":0\}\"\]""".toRegex()
//            CServiceAdapter.getObjects("myapp").shouldMatch(regex)
        }

        CServiceAdapter.close("myapp").shouldBeTrue()
        CServiceAdapter.delete("myapp").shouldBeTrue()
        delay(200)
    }
})
