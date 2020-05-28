package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emptyFlow

/**
 * A [ManagedController] is a [Controller] that additionally provides the ability to [start]
 * the internal state machine, but also requires to [cancel] the state machine to prevent leaks.
 *
 * Before using this, make sure to look into the [Controller] documentation.
 */
interface ManagedController<Action, Mutation, State> : Controller<Action, Mutation, State> {

    /**
     * Starts the internal state machine of this [ManagedController].
     *
     * Returns true if [ManagedController] was started, false if it was already started.
     */
    fun start(): Boolean

    /**
     * Cancels the internal state machine of this [ManagedController].
     * Once cancelled, the [ManagedController] cannot be re-started.
     *
     * Returns the last [State] produced by the internal state machine.
     */
    fun cancel(): State
}

/**
 * Creates a [ManagedController] via [ControllerImplementation].
 *
 * The internal state machine is started when calling [ManagedController.start].
 * A [ManagedController] also HAS to be cancelled via [ManagedController.cancel] to avoid leaks.
 */
@Suppress("FunctionName")
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action, Mutation, State> ManagedController(

    /**
     * The initial [State] for the internal state machine.
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
     * Used for [ControllerLog] and as [CoroutineName] for the internal state machine.
     */
    tag: String = defaultTag(),
    /**
     * Log configuration for [ControllerEvent]s. See [ControllerLog].
     */
    controllerLog: ControllerLog = ControllerLog.default,

    /**
     * The [CoroutineDispatcher] that the internal state machine is launched in.
     *
     * [Mutator] and [Reducer] will run on this [CoroutineDispatcher].
     */
    dispatcher: CoroutineDispatcher = Dispatchers.Default
): ManagedController<Action, Mutation, State> = ControllerImplementation(
    scope = CoroutineScope(dispatcher),
    dispatcher = dispatcher,
    controllerStart = ControllerStart.Managed,

    initialState = initialState,
    mutator = mutator,
    reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag,
    controllerLog = controllerLog
)