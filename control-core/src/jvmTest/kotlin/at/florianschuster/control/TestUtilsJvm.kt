package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

internal actual fun suspendTest(block: suspend CoroutineScope.() -> Unit) {
    runBlocking(block = block)
}