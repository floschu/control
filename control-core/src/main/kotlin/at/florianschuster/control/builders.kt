package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.ContinuationInterceptor

/**
 * Creates a [Controller] bound to the [CoroutineScope] via [ControllerImplementation].
 *
 * The [ControllerImplementation.state] [Flow] is launched once [ControllerImplementation.state],
 * [ControllerImplementation.currentState] or [ControllerImplementation.dispatch] are accessed.
 *
 * Per default, the [CoroutineDispatcher] of [CoroutineScope] is used to launch the
 * [ControllerImplementation.state] [Flow]. If another [CoroutineDispatcher] should be used,
 * override [dispatcher].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, Mutation, State> CoroutineScope.createController(

    /**
     * The [ControllerImplementation] is started with this [State].
     */
    initialState: State,

    /**
     * See [Mutator].
     */
    mutator: Mutator<Action, Mutation, State> = { _ -> emptyFlow() },

    /**
     * See [Reducer].
     */
    reducer: Reducer<Mutation, State> = { _, previousState -> previousState },

    /**
     * See [Transformer].
     */
    actionsTransformer: Transformer<Action> = { it },
    mutationsTransformer: Transformer<Mutation> = { it },
    statesTransformer: Transformer<State> = { it },

    /**
     * Override to launch [ControllerImplementation.state] [Flow] in different [CoroutineDispatcher]
     * than the one used in the [CoroutineScope.coroutineContext].
     */
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,

    /**
     * Set as [CoroutineName] for the [ControllerImplementation.state] context.
     * Also used for logging if enabled via [controllerLog].
     */
    tag: String = defaultTag(),

    /**
     * Log configuration for the [ControllerImplementation]. See [ControllerLog].
     */
    controllerLog: ControllerLog = ControllerLog.default
): Controller<Action, Mutation, State> = ControllerImplementation(
    scope = this, dispatcher = dispatcher,

    initialState = initialState, mutator = mutator, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag, controllerLog = controllerLog
)

/**
 * Creates a [Controller] with [CoroutineScope.createController] where [Action] == [Mutation].
 * The [Controller] can only deal with synchronous state reductions without any asynchronous side-effects.
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, State> CoroutineScope.createSynchronousController(
    initialState: State,
    reducer: Reducer<Action, State> = { _, previousState -> previousState },

    actionsTransformer: Transformer<Action> = { it },
    statesTransformer: Transformer<State> = { it },

    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,

    tag: String = defaultTag(),
    controllerLog: ControllerLog = ControllerLog.default
): Controller<Action, Action, State> = createController(
    dispatcher = dispatcher,

    initialState = initialState,
    mutator = { action -> flowOf(action) },
    reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = { it },
    statesTransformer = statesTransformer,

    tag = tag,
    controllerLog = controllerLog
)