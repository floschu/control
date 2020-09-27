@file:Suppress("EXPERIMENTAL_API_USAGE")

package at.florianschuster.control

import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest

internal actual fun suspendTest(
    block: suspend SuspendedTestScope.() -> Unit
) {
    val scope = TestCoroutineScope()
    scope.runBlockingTest {
        block(JvmSuspendedTestScope(scope))
    }
}

private class JvmSuspendedTestScope(
    scope: TestCoroutineScope
) : SuspendedTestScope, TestCoroutineScope by scope