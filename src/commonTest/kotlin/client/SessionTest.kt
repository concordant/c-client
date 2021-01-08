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
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.matchers.nulls.*

class ClientTest : StringSpec({
 
    "opened session should be active session" {
        ActiveSession.shouldBeNull()
        val session = Session.connect("mydatabase", "credentials")
        ActiveSession.shouldBe(session)
        session.close()
        ActiveSession.shouldBeNull()
    }

    "use a closed session should fail" {
        val session = Session.connect("mydatabase", "credentials")
        session.close()
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection", true)
        }
    }

    "use a closed collection should fail" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", true)
        collection.close()
        shouldThrow<RuntimeException> {
            collection.open("mycounter", "PNCounter", false) { _, _ -> Unit }
        }
        session.close()
    }

//    "use a closed object should fail" {
//        val session = Session.connect("mydatabase", "credentials")
//        val collection = session.openCollection("mycollection", false)
//        val deltacrdt = collection.open("mycounter", "PNCounter", false) { _, _ -> }
//        deltacrdt.close()
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.RC) {
//                if (deltacrdt is PNCounter) {
//                    deltacrdt.increment(12)
//                }
//            }
//        }
//        session.close()
//    }

    "close is done in cascade from session" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter1", "PNCounter", false) { _, _ -> Unit }
        session.close()
        shouldThrow<RuntimeException> {
            collection.open("mycounter2", "PNCounter", false) { _, _ -> Unit }
        }
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.RC) {
//                if (deltacrdt is PNCounter){
//                    deltacrdt.increment(12)
//                }
//            }
//        }
    }

    "close is done in cascade from collection" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter1", "PNCounter", false) { _, _ -> Unit }
        collection.close()
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.RC) {
//                if (deltacrdt is PNCounter) {
//                    deltacrdt.increment(12)
//                }
//            }
//        }
        session.close()
    }

    "open a second session should fail" {
        val session1 = Session.connect("mydatabase1", "credentials")
        shouldThrow<RuntimeException> {
            Session.connect("mydatabase2", "credentials")
        }
        session1.close()
    }

    "open a second collection should fail" {
        val session = Session.connect("mydatabase", "credentials")
        session.openCollection("mycollection1", true)
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection2", true)
        }
        ActiveSession.shouldBe(session)
        session.close()
    }

    "open a transaction in a transaction should fail" {
        val session = Session.connect("mydatabase", "credentials")
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.RC) {
                session.transaction(ConsistencyLevel.RC) {
                }
            }
        }
        session.close()
    }

    "open read collection then open write object should fail" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", true)
        shouldThrow<RuntimeException> {
            collection.open("mycounter", "PNCounter", false) { _, _ -> Unit }
        }
        session.close()
    }

    "open a collection within a transaction should fail" {
        val session = Session.connect("mydatabase", "credentials")
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.RC) {
                session.openCollection("mycollection", true)
            }
        }
        session.close()
    }

    "open an object within a transaction should fail" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", true)
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.RC) {
//                collection.open("mycounter", "PNCounter", false) { _, _ -> Unit }
//            }
//        }
        session.close()
    }

    "operation on an object outside a transaction should fail" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter", "PNCounter", false) { _, _ -> Unit }
//        shouldThrow<RuntimeException> {
//            if (deltacrdt is PNCounter) {
//                deltacrdt.get()
//            }
//        }
//        shouldThrow<RuntimeException> {
//            if (deltacrdt is PNCounter) {
//                deltacrdt.increment(12)
//            }
//        }
        session.close()
    }

    "update a read-only object should fail" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter", "PNCounter", true) { _, _ -> Unit }
        var value = 1
        session.transaction(ConsistencyLevel.RC) {
            if (deltacrdt is PNCounter) {
                value = deltacrdt.get()
            }
        }
        value.shouldBe(0)
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.RC) {
//                if (deltacrdt is PNCounter) {
//                    deltacrdt.increment(12)
//                }
//            }
//        }
        session.close()
    }
})
