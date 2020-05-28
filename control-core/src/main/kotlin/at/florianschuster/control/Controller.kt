package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.coroutines.ContinuationInterceptor

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
 * The [Controller] creates an uni-directional stream of data as shown in the diagram above, by
 * handling incoming [Action]'s via [Controller.dispatch] and creating new [State]'s that
 * can be collected via [Controller.state].
 */
interface Controller<Action, Mutation, State> {

    /**
     * Dispatches an [Action] to be processed by this [Controller].
     */
    fun dispatch(action: Action)

    /**
     * The current [State].
     */
    val currentState: State

    /**
     * The [State] [Flow]. Use this to collect [State] changes.
     */
    val state: Flow<State>
}

/**
 * Creates a [Controller] bound to the [CoroutineScope] via [ControllerImplementation].
 * If the [CoroutineScope] is cancelled, the internal state machine of the [Controller] completes.
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
 *          ┃               ┏━━━━━▼━━━━━┓           ┃   side effect   ┏━━━━━━━━━━━━━━━━━━━━┓
 *          ┃               ┃  mutator ◀───────────────────────────────▶  service/usecase  ┃
 *          ┃               ┗━━━━━━━━━━━┛           ┃                 ┗━━━━━━━━━━━━━━━━━━━━┛
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
 *
 * To create a [Controller] that is not bound to a [CoroutineScope] look into [ManagedController].
 */
@ExperimentalCoroutinesApi
@FlowPreview
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
    controllerLog: ControllerLog = ControllerLog.default,

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
    dispatcher: CoroutineDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
): Controller<Action, Mutation, State> = ControllerImplementation(
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
 * sealed class Action {
 *     object AddZero : Action()
 *     object AddOne : Action()
 *     object AddTwo : Action()
 * }
 *
 * sealed class Mutation {
 *     object Add : Mutation()
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
typealias Mutator<Action, Mutation, State> = MutatorScope<Action, State>.(
    action: Action
) -> Flow<Mutation>

/**
 * The [MutatorScope] provides access to the [currentState] and the [actions] [Flow] in a [Mutator].
 */
interface MutatorScope<Action, State> {

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
 * sealed class Mutation {
 *     object Add : Mutation()
 *     data class Set(val valueToSet: Int) : Mutation()
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
typealias Reducer<Mutation, State> = (mutation: Mutation, previousState: State) -> State

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
typealias Transformer<Emission> = (emissions: Flow<Emission>) -> Flow<Emission>
