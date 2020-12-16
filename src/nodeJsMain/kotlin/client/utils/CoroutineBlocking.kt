package client.utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise

actual fun coroutineBlocking(block: suspend () -> Unit): dynamic = GlobalScope.promise { block() }