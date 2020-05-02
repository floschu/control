package at.florianschuster.control.countercomposeexample

import androidx.compose.Composable
import androidx.compose.FrameManager
import androidx.compose.State
import androidx.compose.onPreCommit
import androidx.compose.state
import at.florianschuster.control.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Collects values from [Controller.state] and represents its latest value via [State].
 * Executes th effect every time the [Controller] or the [context] changes.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectAsState(): State<S> {
    val context = Dispatchers.Main
    val mutableState = state { currentState }
    onPreCommit(this, context) {
        val job = CoroutineScope(context).launch {
            state.collect { FrameManager.framed { mutableState.value = it } }
        }
        onDispose { job.cancel() }
    }
    return mutableState
}