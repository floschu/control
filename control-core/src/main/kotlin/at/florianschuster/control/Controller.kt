package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * A [Controller] is an ui-independent class that controls the state of a view. The role of a
 * [Controller] is to separate business-logic from view-logic. A [Controller] has no dependency
 * to the view, so it can easily be unit tested.
 *
 *
 * <pre>
 *                  [Action] via [dispatch]
 *          +-----------------------------------+
 *          |                                   |
 *     +----+-----+                    +--------|-------+
 *     |          |                    |        v       |
 *     |   View   |                    |   Controller   |
 *     |    ^     |                    |                |
 *     +----|-----+                    +--------+-------+
 *          |                                   |
 *          +-----------------------------------+
 *                  [State] via [state]
 * </pre>
 *
 * The [Controller] creates an uni-directional stream of data as shown in the diagram above, by
 * handling incoming [Action]'s via [Controller.dispatch] and creating new [State]'s that
 * can be collected via [Controller.state].
 *
 * Basic Principle: 1 [Action] -> [0..n] [Mutation] -> each 1 new [State]
 *
 * For implementation details look into:
 * 1. [Mutator]: [Action] -> [Mutation]
 * 2. [Reducer]: [Mutation] -> [State]
 * 3. [Transformer]
 * 4. [ControllerImplementation]
 *
 * To create a [Controller] use [CoroutineScope.createController].
 */
interface Controller<Action, Mutation, State> {

    /**
     * Dispatches an [Action] to be processed by this [Controller].
     * Calling this, starts the [Controller].
     */
    fun dispatch(action: Action)

    /**
     * The current [State].
     * Accessing this, starts the [Controller].
     */
    val currentState: State

    /**
     * The [State] [Flow]. Use this to collect [State] changes.
     * Accessing this, starts the [Controller].
     */
    val state: Flow<State>

    /**
     * Set to true if you want to enable stubbing with [stub].
     * This has be set before binding [Controller.state].
     */
    var stubEnabled: Boolean

    /**
     * Use this [ControllerStub] for view testing.
     */
    val stub: ControllerStub<Action, State>
}

/**
 * A [Mutator] takes an action and transforms it into a [Flow] of [0..n] mutations.
 *
 *
 * Use the stateAccessor to access the [Controller.currentState] during a suspending mutation.
 *
 * Use the transformedActionFlow if a [Flow] inside the [Mutator] needs to be cancelled or
 * transformed due to the incoming action (e.g. takeUntil(actionFlow.filterIsInstance<Action.Cancel>()) ).
 * The actionFlow is accessed after [ControllerImplementation.actionsTransformer] is applied.
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
 * mutator = Mutator { action, _, _ ->
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
typealias Mutator<Action, Mutation, State> = (
    action: Action,
    stateAccessor: () -> State,
    transformedActionFlow: Flow<Action>
) -> Flow<Mutation>

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
 * reducer = Reducer { mutation, previousState ->
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
 * actionsTransformer = Transformer { actions ->
 *     actions.onStart { emit(Action.InitialLoad) }
 * }
 * ```
 *
 * Transformer<Mutation> -> Example: Merge global [Flow]
 *
 * ```
 * val userSession: Flow<Session>
 *
 * mutationsTransformer = Transformer { mutations ->
 *     flowOf(mutations, userSession.map { Mutation.SetSession(it) }).flattenMerge()
 * }
 * ```
 *
 * Transformer<State> -> Example: Logging
 *
 * ```
 * statesTransformer = Transformer { states ->
 *     states.onEach { Log.d("New State: $it) }
 * }
 * ```
 */
typealias Transformer<Emission> = (emissions: Flow<Emission>) -> Flow<Emission>