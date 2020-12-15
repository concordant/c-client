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

import client.utils.runTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.string.shouldMatch
import kotlinx.coroutines.*

class CServiceAdapterTest : StringSpec({
    "connect to c-service create, write twice, read and delete" {
        CServiceAdapter.connect("myapp").shouldBeTrue()

        CServiceAdapter.updateObject("myapp", "myid", """{\"key\":\"value1\"}""")
        delay(500)
        val regex1 = """\[\"\{\\\"_id\\\":\\\"myid\\\",\\\"_rev\\\":\\\"(\d+)-(\w+)\\\",\\\"key\\\":\\\"value1\\\"\}\"\]""".toRegex()
        CServiceAdapter.getObjects("myapp").shouldMatch(regex1)

        delay(500)

        CServiceAdapter.updateObject("myapp", "myid", """{\"key\":\"value2\"}""")
        delay(500)
        val regex2 = """\"\{\\\"_id\\\":\\\"myid\\\",\\\"_rev\\\":\\\"(\d+)-(\w+)\\\",\\\"key\\\":\\\"value2\\\"\}\"""".toRegex()
        CServiceAdapter.getObject("myapp", "myid").shouldMatch(regex2)
    }
})
