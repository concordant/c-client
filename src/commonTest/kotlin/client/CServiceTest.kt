package client

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*
import io.ktor.client.*
import io.ktor.client.request.*

class CServiceTest : StringSpec({
    
    "connect to express server" {
        val client = HttpClient()
        val result = client.get<String>("http://127.0.0.1:50000/")
        result.shouldBe("Hello world!!")
    }
})
