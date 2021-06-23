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

import client.utils.ConsistencyLevel
import crdtlib.crdt.PNCounter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests suite for Concordant objects usage.
 */
class CObjectTest : StringSpec({
    "open read collection then open write object should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", true)
        shouldThrow<RuntimeException> {
            collection.open("mycounter", "PNCounter", false)
        }
        session.close()
    }

    "open a transaction in a transaction should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                session.transaction(ConsistencyLevel.None) {
                }
            }
        }
        session.close()
    }

    "open an object within a transaction should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", true)
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                collection.open("mycounter", "PNCounter", false)
            }
        }
        session.close()
    }

    "operation on an object outside a transaction should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter", "PNCounter", false)
        shouldThrow<RuntimeException> {
            if (deltacrdt is PNCounter) {
                deltacrdt.get()
            }
        }
        shouldThrow<RuntimeException> {
            if (deltacrdt is PNCounter) {
                deltacrdt.increment(12)
            }
        }
        session.close()
    }

    "update a read-only object should fail" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter", "PNCounter", true)
        var value = 1
        session.transaction(ConsistencyLevel.None) {
            if (deltacrdt is PNCounter) {
                value = deltacrdt.get()
            }
        }
        value.shouldBe(0)
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                if (deltacrdt is PNCounter) {
                    deltacrdt.increment(12)
                }
            }
        }
        session.close()
    }

    // Opening twice an object should return the same object
    // instead of a new copy.
    "open two times an object should work" {
        val session = Session.connect(dbname, svcUrl, svcCred)
        val collection = session.openCollection("mycollection", false)
        val deltacrdt1 = collection.open("mycounter", "PNCounter", false)
        val deltacrdt2 = collection.open("mycounter", "PNCounter", false)
        deltacrdt1.shouldBe(deltacrdt2)
        session.close()
    }

//    "use a closed object should fail" {
//        val session = Session.connect(dbname, svcUrl, svcCred)
//        val collection = session.openCollection("mycollection", false)
//        val deltacrdt = collection.open("mycounter", "PNCounter", false)
//        deltacrdt.close()
//        shouldThrow<RuntimeException> {
//            session.transaction(ConsistencyLevel.None) {
//                if (deltacrdt is PNCounter) {
//                    deltacrdt.increment(12)
//                }
//            }
//        }
//        session.close()
//    }
})
