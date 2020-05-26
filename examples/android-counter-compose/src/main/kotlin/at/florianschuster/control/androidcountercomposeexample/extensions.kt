package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.Composable
import androidx.compose.CompositionLifecycleObserver
import androidx.compose.State
import androidx.compose.collectAsState
import at.florianschuster.control.Controller
import at.florianschuster.control.ManagedController
import kotlinx.coroutines.flow.Flow

/**
 * Collects values from the [Controller.state] and represents its latest value via
 * [androidx.compose.State].
 *
 * Every time a new [Controller.state] is emitted, the returned [androidx.compose.State]
 * will be updated causing re-composition.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectAsState(): State<S> {
    return state.collectAsState(initial = currentState)
}

/**
 * A [Controller] delegate that implements [CompositionLifecycleObserver].
 *
 * The state machine of [controller] is started once [CompositionControllerDelegate] is used in a
 * composition and cancelled once [CompositionControllerDelegate] is no longer used.
 */
interface CompositionControllerDelegate<Action, Mutation, State> :
    Controller<Action, Mutation, State>, CompositionLifecycleObserver {

    val controller: ManagedController<Action, Mutation, State>

    // region Controller

    override fun dispatch(action: Action) = controller.dispatch(action)
    override val currentState: State get() = controller.currentState
    override val state: Flow<State> get() = controller.state

    // endregion

    // region CompositionLifecycleObserver

    override fun onEnter() {
        controller.start()
    }

    override fun onLeave() {
        controller.cancel()
    }

    // endregion
}