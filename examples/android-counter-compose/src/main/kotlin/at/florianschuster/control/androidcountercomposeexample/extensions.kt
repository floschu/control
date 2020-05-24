package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.Composable
import androidx.compose.CompositionLifecycleObserver
import androidx.compose.State
import androidx.compose.collectAsState
import androidx.compose.remember
import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.ManagedController
import at.florianschuster.control.Mutator
import at.florianschuster.control.Reducer
import at.florianschuster.control.Transformer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow

/**
 * Collects values from the [Controller.state] and represents its latest value via [State].
 * Every time state is emitted, the returned [State] will be updated causing
 * re-composition of every [State.value] usage.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectAsState(): State<S> {
    return state.collectAsState(initial = currentState)
}

/**
 * An intermediate solution for creating a [Controller] used in [Composable]s.
 *
 * The [Controller] state machine is started once the [Controller] is used in a composition and
 * cancelled once it is no longer used.
 *
 * The [Controller] will be kept across recompositions via [remember]. If any of the parameters
 * change, a new instance of the [Controller] will be created.
 */
@Composable
internal fun <Action, Mutation, State> ComposableController(
    initialState: State,
    mutator: Mutator<Action, Mutation, State> = { _ -> emptyFlow() },
    reducer: Reducer<Mutation, State> = { _, previousState -> previousState },

    actionsTransformer: Transformer<Action> = { it },
    mutationsTransformer: Transformer<Mutation> = { it },
    statesTransformer: Transformer<State> = { it },

    tag: String = "TODO defaultTag()",
    controllerLog: ControllerLog = ControllerLog.default,

    dispatcher: CoroutineDispatcher = Dispatchers.Default
): Controller<Action, Mutation, State> = remember(
    initialState, mutator, reducer,
    actionsTransformer, mutationsTransformer, statesTransformer,
    tag, controllerLog,
    dispatcher
) {
    ComposableLifecycleController(
        ManagedController(
            initialState, mutator, reducer,
            actionsTransformer, mutationsTransformer, statesTransformer,
            tag, controllerLog,
            dispatcher
        )
    )
}

private class ComposableLifecycleController<Action, Mutation, State>(
    private val controller: ManagedController<Action, Mutation, State>
) : Controller<Action, Mutation, State> by controller, CompositionLifecycleObserver {

    override fun onEnter() {
        controller.start()
    }

    override fun onLeave() {
        controller.cancel()
    }
}