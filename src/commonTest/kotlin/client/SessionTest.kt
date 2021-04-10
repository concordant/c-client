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
import client.utils.ConsistencyLevel
import crdtlib.crdt.PNCounter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

val svcUrl = "http://127.0.0.1:4000"
val svcCred = "credentials"
val dbname = "mydatabase"
val dbname2 = "myapp"

/**
 * Tests suite for Session class.
 */
class SessionTest : StringSpec({

    "opened session should be active session" {
        ActiveSession.shouldBeNull()
        val session = Session.connect(dbname, svcUrl, svcCred)
        session.getDbName().shouldBe(dbname)
        session.getServiceUrl().shouldBe(svcUrl)
        ActiveSession.shouldBe(session)
        session.close()
        ActiveSession.shouldBeNull()
    }

    "use a closed session should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        session.close()
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection", true)
        }
    }

    "open a second session should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        shouldThrow<RuntimeException> {
            Session.connect("mydatabase2", svcUrl, svcCred)
        }
        session.close()
    }

    "close is done in cascade from session" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter1", "PNCounter", false)
        session.close()
        shouldThrow<RuntimeException> {
            collection.open("mycounter2", "PNCounter", false)
        }
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                if (deltacrdt is PNCounter){
                    deltacrdt.increment(12)
                }
            }
        }
    }
})
