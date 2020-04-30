package at.florianschuster.control.countercomposeexample

import androidx.compose.Composable
import androidx.compose.FrameManager
import androidx.compose.State
import androidx.compose.onPreCommit
import androidx.compose.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * https://android-review.googlesource.com/c/platform/frameworks/support/+/1287599/21/compose/compose-runtime/src/jvmMain/kotlin/androidx/compose/FlowAdapter.kt#1
 *
 * Can be replaced by standard function when compose dev11 is released.
 *
 * Collects values from this [Flow] and represents its latest value via [State]. Every time there
 * would be new value posted into the [Flow] the returned [State] will be updated causing
 * recomposition of every [State.value] usage.
 *
 * @sample androidx.compose.samples.FlowWithInitialSample
 *
 * @param context [CoroutineContext] to use for collecting.
 */
@Composable
fun <T> Flow<T>.collectAsState(
    initial: T,
    context: CoroutineContext = Dispatchers.Main
): State<T> {
    val state = state { initial }
    onPreCommit(this, context) {
        val job = CoroutineScope(context).launch {
            collect {
                FrameManager.framed {
                    state.value = it
                }
            }
        }
        onDispose { job.cancel() }
    }
    return state
}