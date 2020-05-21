package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.collectAsState
import androidx.compose.onDispose
import androidx.compose.remember
import at.florianschuster.control.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Creates a [CoroutineScope] with [coroutineContext] that lives until [onDispose].
 */
@Composable
internal fun ComposeCoroutineScope(
    coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
): CoroutineScope {
    val scope = remember(coroutineContext) { CoroutineScope(coroutineContext) }
    onDispose { scope.cancel() }
    return scope
}

/**
 * Collects values from the [Controller.state] and represents its latest value via [State].
 * Every time state is emitted, the returned [State] will be updated causing
 * re-composition of every [State.value] usage.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectState(): State<S> {
    return state.collectAsState(initial = currentState)
}