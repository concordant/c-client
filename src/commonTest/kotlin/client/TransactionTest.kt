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

import client.utils.ActiveTransaction
import client.utils.ConsistencyLevel
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

/**
 * Tests suite for Transaction class.
 */
class TransactionTest : StringSpec({

    lateinit var session: Session

    beforeTest {
        session = Session.connect(dbname, svcUrl, svcCred)
    }

    afterTest {
        session.close()
    }

    "open an empty transaction" {
        ActiveTransaction.shouldBeNull()
        session.transaction(ConsistencyLevel.None) {
        }
        ActiveTransaction.shouldBeNull()
    }

    "open a transaction with non-Concordant code" {
        var x = 1
        session.transaction(ConsistencyLevel.None) {
            x = 10
            x--
            x += 2
        }
        x.shouldBe(11)
    }

    "open a transaction with exception" {
        shouldThrow<RuntimeException> {
            session.transaction(ConsistencyLevel.None) {
                throw RuntimeException("This is an exception.")
            }
        }
    }

    "open a transaction in a transaction should fail" {
        session.transaction(ConsistencyLevel.None) {
            shouldThrow<RuntimeException> {
                session.transaction(ConsistencyLevel.None) {
                }
            }
        }
    }
})
