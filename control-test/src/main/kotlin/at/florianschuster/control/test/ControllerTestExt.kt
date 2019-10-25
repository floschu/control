package at.florianschuster.control.test

import at.florianschuster.control.Controller
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineScope
import java.lang.IllegalArgumentException

/**
 * Tests the [Controller.state] with [Flow.test].
 */
@FlowPreview
@ExperimentalCoroutinesApi
fun <State> Controller<*, *, State>.test(): TestCollector<State> {
    val scope = scope as? TestCoroutineScope
        ?: throw IllegalArgumentException(message)
    return state.test(scope)
}

private const val message = "`Controller.scope` must be a `TestCoroutineScope`. " +
    "Override it before calling `Controller.test()`"