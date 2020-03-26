package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.ContinuationInterceptor

/**
 * Creates a [Controller] bound to the [CoroutineScope] via [ControllerImplementation].
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
     * Set as [CoroutineName] for the [ControllerImplementation.state] context.
     * Also used for logging if enabled via [controllerLog].
     */
    tag: String = defaultTag(),

    /**
     * Log configuration for the [ControllerImplementation]. See [ControllerLog].
     */
    controllerLog: ControllerLog = ControllerLog.default,

    /**
     * When the [ControllerImplementation.state] [Flow] should be started. The [Flow] is launched
     * in [ControllerImplementation] init.
     *
     * Default is [CoroutineStart.LAZY] -> [Flow] is started once [ControllerImplementation.state],
     * [ControllerImplementation.currentState] or [ControllerImplementation.dispatch] are accessed
     * or if the [Job] in the [CoroutineScope] is started.
     *
     * Look into [CoroutineStart] to see how the options would affect the [Flow] start.
     */
    coroutineStart: CoroutineStart = CoroutineStart.LAZY,

    /**
     * Override to launch [ControllerImplementation.state] [Flow] in different [CoroutineDispatcher]
     * than the one used in the [CoroutineScope.coroutineContext].
     */
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
): Controller<Action, Mutation, State> = ControllerImplementation(
    scope = this, dispatcher = dispatcher, coroutineStart = coroutineStart,

    initialState = initialState, mutator = mutator, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag, controllerLog = controllerLog
)

/**
 * Creates a [Controller] with [CoroutineScope.createController] where [Action] == [Mutation].
 *
 * The [Controller] can only deal with synchronous state reductions without
 * any asynchronous side-effects.
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, State> CoroutineScope.createSynchronousController(
    initialState: State,
    reducer: Reducer<Action, State> = { _, previousState -> previousState },

    actionsTransformer: Transformer<Action> = { it },
    statesTransformer: Transformer<State> = { it },

    tag: String = defaultTag(),
    controllerLog: ControllerLog = ControllerLog.default,

    coroutineStart: CoroutineStart = CoroutineStart.LAZY,
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
): Controller<Action, Action, State> = createController(
    initialState = initialState,
    mutator = { action -> flowOf(action) },
    reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = { it },
    statesTransformer = statesTransformer,

    tag = tag,
    controllerLog = controllerLog,

    coroutineStart = coroutineStart,
    dispatcher = dispatcher
)