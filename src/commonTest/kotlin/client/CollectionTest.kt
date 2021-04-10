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
        val deltacrdt = collection.open("mycounter1", "PNCounter", false)
        collection.close()
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                if (deltacrdt is PNCounter) {
                    deltacrdt.increment(12)
                }
            }
        }
    }
})
