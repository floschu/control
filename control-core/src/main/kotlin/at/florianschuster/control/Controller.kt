package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * A [Controller] is an UI-independent class that controls the state of a view. The role of a
 * [Controller] is to separate business-logic from view-logic. A [Controller] has no dependency
 * to the view, so it can easily be unit tested.
 *
 *
 * ```
 *                     dispatch(Action)
 *          ┌───────────────────────────────────┐
 *          │                                   │
 *          │                                   │
 *     ┏━━━━━━━━━━┓                    ┏━━━━━━━━▼━━━━━━━┓
 *     ┃          ┃                    ┃                ┃
 *     ┃   View   ┃                    ┃   Controller   ┃
 *     ┃          ┃                    ┃                ┃
 *     ┗━━━━▲━━━━━┛                    ┗━━━━━━━━━━━━━━━━┛
 *          │                                   │
 *          │                                   │
 *          └───────────────────────────────────┘
 *                          state
 * ```
 *
 * The [Controller] creates an uni-directional stream of data as shown in the diagram above,
 * by handling incoming [Action]'s via [Controller.dispatch] and creating new [State]'s that
 * can be collected via [Controller.state].
 */
interface Controller<Action, State> {

    /**
     * Dispatches an [Action] to be processed by this [Controller].
     */
    fun dispatch(action: Action)

    /**
     * The [State]. Use this to collect [State] changes
     * or get the current [State] via [StateFlow.value].
     */
    val state: StateFlow<State>
}

/**
 * Creates a [Controller] bound to the [CoroutineScope] via [ControllerImplementation].
 * If the [CoroutineScope] is cancelled, the internal state machine of the [Controller]
 * completes.
 *
 * The principle of the created state machine is:
 *
 *   1 [Action] -> [0..n] [Mutation] -> each 1 new [State]
 *
 * ```
 *                              Action
 *          ┏━━━━━━━━━━━━━━━━━━━━━│━━━━━━━━━━━━━━━━━┓
 *          ┃                     │                 ┃
 *          ┃                     │                 ┃
 *          ┃                     │                 ┃
 *          ┃               ┏━━━━━▼━━━━━┓           ┃  side effect ┏━━━━━━━━━━━━━━━━━━━━┓
 *          ┃               ┃  mutator ◀────────────────────────────▶  service/usecase  ┃
 *          ┃               ┗━━━━━━━━━━━┛           ┃              ┗━━━━━━━━━━━━━━━━━━━━┛
 *          ┃                     │                 ┃
 *          ┃                     │ 0..n mutations  ┃
 *          ┃                     │                 ┃
 *          ┃               ┏━━━━━▼━━━━━┓           ┃
 *          ┃  ┌───────────▶┃  reducer  ┃           ┃
 *          ┃  │            ┗━━━━━━━━━━━┛           ┃
 *          ┃  │ previous         │                 ┃
 *          ┃  │ state            │ new state       ┃
 *          ┃  │                  │                 ┃
 *          ┃  │            ┏━━━━━▼━━━━━┓           ┃
 *          ┃  └────────────┃   state   ┃           ┃
 *          ┃               ┗━━━━━━━━━━━┛           ┃
 *          ┃                     │                 ┃
 *          ┗━━━━━━━━━━━━━━━━━━━━━│━━━━━━━━━━━━━━━━━┛
 *                                ▼
 *                              state
 * ```
 *
 * For implementation details look into:
 * 1. [Mutator]: This corresponds to [Action] -> [Mutation]
 * 2. [Reducer]: This corresponds to [Mutation] -> [State]
 * 3. [Transformer]
 * 4. [ControllerImplementation]
 */
fun <Action, Mutation, State> CoroutineScope.createController(

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
    controllerLog: ControllerLog = ControllerLog.None,

    /**
     * When the internal state machine [Flow] should be started. See [ControllerStart].
     */
    controllerStart: ControllerStart = ControllerStart.Lazy,

    /**
     * Override to launch the internal state machine [Flow] in a different [CoroutineDispatcher]
     * than the one used in the [CoroutineScope.coroutineContext].
     *
     * [Mutator] and [Reducer] will run on this [CoroutineDispatcher].
     */
    dispatcher: CoroutineDispatcher = defaultScopeDispatcher()
): Controller<Action, State> = ControllerImplementation<Action, Mutation, State, Nothing>(
    scope = this, dispatcher = dispatcher, controllerStart = controllerStart,

    initialState = initialState, mutator = mutator, reducer = reducer,
    actionsTransformer = actionsTransformer,
    mutationsTransformer = mutationsTransformer,
    statesTransformer = statesTransformer,

    tag = tag, controllerLog = controllerLog
)

/**
 * A [Mutator] takes an action and transforms it into a [Flow] of [0..n] mutations.
 *
 *
 * Example:
 *
 * ```
 * sealed interface Action {
 *     object AddZero : Action
 *     object AddOne : Action
 *     object AddTwo : Action
 * }
 *
 * sealed interface Mutation {
 *     object Add : Mutation
 * }
 *
 * mutator = { action ->
 *     when(action) {
 *         is Action.AddZero -> emptyFlow()
 *         is Action.AddOne -> flowOf(Mutation.Add)
 *         is Action.AddTwo -> flow {
 *             emit(Mutation.Add)
 *             emit(Mutation.Add)
 *         }
 *     }
 * }
 * ```
 */
typealias Mutator<Action, Mutation, State> = MutatorContext<Action, State>.(action: Action) -> Flow<Mutation>

/**
 * The [MutatorContext] provides access to the current [State] and the [actions] [Flow] in
 * a [Mutator].
 */
interface MutatorContext<Action, State> {

    /**
     * A generated property, thus always providing the current [State] when accessed.
     */
    val currentState: State

    /**
     * The [Flow] of incoming actions, accessed after [Action] [Transformer] is applied.
     */
    val actions: Flow<Action>
}

/**
 * A [Reducer] takes the previous state and a mutation and returns a new state synchronously.
 *
 *
 * Example:
 *
 * ```
 * sealed interface Mutation {
 *     object Add : Mutation
 *     data class Set(val valueToSet: Int) : Mutation
 * }
 *
 * data class State(val value: Int)
 *
 * reducer = { mutation, previousState ->
 *     when(mutation) {
 *         is Mutation.Add -> previousState.copy(value = previousState.value + 1)
 *         is Mutation.Set -> previousState.copy(value = mutation.valueToSet)
 *     }
 * }
 * ```
 */
typealias Reducer<Mutation, State> = ReducerContext.(mutation: Mutation, previousState: State) -> State

/**
 * A context used for a [Reducer]. Does not provide any additional functionality.
 */
interface ReducerContext

/**
 * A [Transformer] transforms a [Flow] of a type - such as action, mutation or state.
 *
 *
 * Examples:
 *
 * Transformer<Action> -> Example: Initial action
 *
 * ```
 * actionsTransformer = { actions ->
 *     actions.onStart { emit(Action.InitialLoad) }
 * }
 * ```
 *
 * Transformer<Mutation> -> Example: Merge global [Flow]
 *
 * ```
 * val userSession: Flow<Session>
 *
 * mutationsTransformer = { mutations ->
 *     merge(mutations, userSession.map { Mutation.SetSession(it) })
 * }
 * ```
 *
 * Transformer<State> -> Example: Logging
 *
 * ```
 * statesTransformer = { states ->
 *     states.onEach { println("New State: $it) }
 * }
 * ```
 */
typealias Transformer<Emission> = TransformerContext.(emissions: Flow<Emission>) -> Flow<Emission>

/**
 * A context used for a [Transformer]. Does not provide any additional functionality.
 */
interface TransformerContext
