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

import client.utils.CObjectUId
import client.utils.ConsistencyLevel
import crdtlib.crdt.PNCounter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

/**
 * Tests suite for Collection class.
 */
class CollectionTest : StringSpec({

    lateinit var session: Session

    beforeTest {
        session = Session.connect(dbname, svcUrl, svcCred)
    }

    afterTest {
        session.close()
    }

    "use a closed collection should fail" {
        val collection = session.openCollection("mycollection", true)
        collection.close()
        shouldThrow<RuntimeException> {
            collection.open("mycounter", "PNCounter", false)
        }
    }

    "open a second collection should fail" {
        session.openCollection("mycollection1", true)
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection2", true)
        }
    }

    "open a collection within a transaction should fail" {
        session.transaction(ConsistencyLevel.None) {
            shouldThrow<RuntimeException> {
                session.openCollection("mycollection", true)
            }
        }
    }

    "close is done in cascade from collection" {
        val collection = session.openCollection("mycollection", false)
        val deltacrdt = collection.open("mycounter", "PNCounter", false)
        collection.close()
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                if (deltacrdt is PNCounter) {
                    deltacrdt.increment(12)
                }
            }
        }
    }

    "retrieve object UId" {
        val collection = session.openCollection("mycollection", false)
        val deltacrdt1 = collection.open("mycounter", "PNCounter", false)
        val deltacrdt2 = collection.open("myregister", "MVRegister", true)
        val unmanagedCrdt = PNCounter()

        collection.getObjectUId(deltacrdt1)
            .shouldBe(CObjectUId("mycollection",
                                 "PNCounter",
                                 "mycounter"))
        collection.getObjectUId(deltacrdt2)
            .shouldBe(CObjectUId("mycollection",
                                 "MVRegister",
                                 "myregister"))
        collection.getObjectUId(unmanagedCrdt).shouldBe(null)
        collection.close()

        // A closed collection does not manage any object.
        collection.getObjectUId(deltacrdt1).shouldBe(null)
        collection.getObjectUId(deltacrdt2).shouldBe(null)
        collection.getObjectUId(unmanagedCrdt).shouldBe(null)
    }

    "check if object is writable" {
        val collection = session.openCollection("mycollection", false)
        val deltacrdt1 = collection.open("mycounter", "PNCounter", false)
        val deltacrdt2 = collection.open("myregister", "MVRegister", true)
        val unmanagedCrdt = PNCounter()

        collection.isWritable(deltacrdt1).shouldBeTrue()
        collection.isWritable(deltacrdt2).shouldBeFalse()
        collection.isWritable(unmanagedCrdt).shouldBeFalse()
        collection.close()

        // A closed collection does not manage any object.
        collection.isWritable(deltacrdt1).shouldBeFalse()
        collection.isWritable(deltacrdt2).shouldBeFalse()
        collection.isWritable(unmanagedCrdt).shouldBeFalse()
    }
})
