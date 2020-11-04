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
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*

class ClientTest : StringSpec({
 
    "open session should be active session" {
        val session = Session.connect("mydatabase", "credentials")
        ActiveSession.shouldBe(session)
        session.close()
    }

    "use a closed session" {
        val session = Session.connect("mydatabase", "credentials")
        session.close()
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection", true)
        }
    }

    "open two collections" {
        val session = Session.connect("mydatabase", "credentials")
        session.openCollection("mycollection1", true)
        shouldThrow<RuntimeException> {
            session.openCollection("mycollection2", true)
        }
        ActiveSession.shouldBe(session)
        session.close()
    }

    "open read collection then open write object" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", true)
        shouldThrow<RuntimeException> {
            collection.open<PNCounter>("mycounter", false, { _, _ -> Unit })
        }
        session.close()
    }

    "use a closed collection" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", true)
        collection.close()
        shouldThrow<RuntimeException> {
            collection.open<PNCounter>("mycounter", false, { _, _ -> Unit })
        }
        session.close()
    }

    "use a closed object" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val cobject = collection.open<PNCounter>("mycounter", false, { _, _ -> Unit })
        cobject.close()
        shouldThrow<RuntimeException> {
            cobject.increment(12)
        }
        session.close()
    }

    "update a read-only object" {
        val session = Session.connect("mydatabase", "credentials")
        val collection = session.openCollection("mycollection", false)
        val cobject = collection.open<PNCounter>("mycounter", true, { _, _ -> Unit })
        val value = cobject.get()
        shouldThrow<RuntimeException> {
            cobject.increment(12)
        }
        session.close()
    }
})
