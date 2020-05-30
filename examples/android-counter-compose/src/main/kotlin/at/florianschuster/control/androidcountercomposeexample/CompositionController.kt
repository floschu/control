package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.Composable
import androidx.compose.CompositionLifecycleObserver
import androidx.compose.State
import androidx.compose.collectAsState
import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.ManagedController
import at.florianschuster.control.Mutator
import at.florianschuster.control.Reducer
import at.florianschuster.control.Transformer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow

/**
 * Collects values from the [Controller.state] and represents its latest value via
 * [androidx.compose.State].
 *
 * Every time a new [Controller.state] is emitted, the returned [androidx.compose.State]
 * will be updated causing re-composition.
 */
@Composable
internal fun <S> Controller<*, *, S>.collectState(): State<S> {
    return state.collectAsState(initial = currentState)
}

/**
 * Creates a [Controller] that can be used inside a composition.
 *
 * Internally, it implements [CompositionLifecycleObserver] and starts its state machine
 * when [CompositionLifecycleObserver.onEnter] is called and cancels it when
 * [CompositionLifecycleObserver.onLeave] is called.
 */
@Suppress("FunctionName")
@ExperimentalCoroutinesApi
@FlowPreview
internal fun <Action, Mutation, State> CompositionController(
    initialState: State,
    mutator: Mutator<Action, Mutation, State> = { _ -> emptyFlow() },
    reducer: Reducer<Mutation, State> = { _, previousState -> previousState },

    actionsTransformer: Transformer<Action> = { it },
    mutationsTransformer: Transformer<Mutation> = { it },
    statesTransformer: Transformer<State> = { it },

    tag: String = defaultTag(),
    controllerLog: ControllerLog = ControllerLog.default,

    dispatcher: CoroutineDispatcher = Dispatchers.Default
): Controller<Action, Mutation, State> = CompositionLifecycleObserverController(
    ManagedController(
        initialState, mutator, reducer,
        actionsTransformer, mutationsTransformer, statesTransformer,
        tag, controllerLog,
        dispatcher
    )
)

private class CompositionLifecycleObserverController<Action, Mutation, State>(
    private val delegate: ManagedController<Action, Mutation, State>
) : CompositionLifecycleObserver, Controller<Action, Mutation, State> by delegate {

    override fun onEnter() {
        delegate.start()
    }

    override fun onLeave() {
        delegate.cancel()
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun defaultTag(): String {
    val stackTrace = Throwable().stackTrace
    check(stackTrace.size >= 2) { "Stacktrace didn't have enough elements." }
    return stackTrace[1].className.split("$").first().split(".").last()
}