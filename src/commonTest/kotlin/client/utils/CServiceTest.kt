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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import io.ktor.client.features.*

class CServiceTest : StringSpec({
    
    "connect to c-service create, write. read, and delete" {
        val client = HttpClient()
        client.post<String>{
            url("http://127.0.0.1:4000/api/create-app")
            contentType(ContentType.Application.Json)
            body = """{"appName":"myapp"}"""
        }.shouldBe("OK")
        client.post<String>{
            url("http://127.0.0.1:4000/api/update-object")
            contentType(ContentType.Application.Json)
            body = """{"appName":"myapp","id":"myid","document":"{\"key\":\"value1\"}"}"""
        }.shouldBe("OK")
        client.post<String>{
            url("http://127.0.0.1:4000/api/update-object")
            contentType(ContentType.Application.Json)
            body = """{"appName":"myapp","id":"myid","document":"{\"key\":\"value2\"}"}"""
        }.shouldBe("OK")
        client.post<String>{
            url("http://127.0.0.1:4000/api/app-object")
            contentType(ContentType.Application.Json)
            body = """{"appName":"myapp","id":"myid"}"""
        }.shouldBe("""{\"key\":\"value2\"}""")
        client.post<String>{
            url("http://127.0.0.1:4000/api/delete-app")
            contentType(ContentType.Application.Json)
            body = """{"appName":"myapp"}"""
        }.shouldBe("OK")
        client.close()
    }
})
