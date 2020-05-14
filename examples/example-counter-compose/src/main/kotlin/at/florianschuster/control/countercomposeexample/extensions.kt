package at.florianschuster.control.countercomposeexample

import androidx.compose.Composable
import androidx.compose.State
import androidx.compose.collectAsState
import at.florianschuster.control.Controller

/**
 * Collects values from the [Controller.state] and represents its latest value via [State].
 * Every time state is emitted, the returned [State] will be updated causing
 * recomposition of every [State.value] usage.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectState(): State<S> {
    return state.collectAsState(initial = currentState)
}