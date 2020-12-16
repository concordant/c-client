package client.utils
import kotlinx.coroutines.runBlocking

actual fun coroutineBlocking(block: suspend () -> Unit) = runBlocking { block() }
