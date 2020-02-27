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

    /**
     * Errors that can happen in a [Controller].
     */
    sealed class Error(
        message: String,
        cause: Throwable
    ) : RuntimeException(message, cause) {

        /**
         * Error during [Mutator].
         */
        class Mutator internal constructor(
            tag: String,
            action: String,
            cause: Throwable
        ) : Error("Mutator error in $tag, action = $action", cause)

        /**
         * Error during [Reducer].
         */
        class Reducer internal constructor(
            tag: String,
            previousState: String,
            mutation: String,
            cause: Throwable
        ) : Error(
            message = "Reducer error in $tag, previousState = $previousState, mutation = $mutation",
            cause = cause
        )
    }
}

/**
 * A [Mutator] takes an action and transforms it into a [Flow] of [0..n] mutations.
 *
 *
 * Example:
 *
 *     sealed class Action {
 *         object AddZero : Action()
 *         object AddOne : Action()
 *         object AddTwo : Action()
 *     }
 *
 *     sealed class Mutation {
 *         object Add : Mutation()
 *     }
 *
 *     mutator = { action, _ ->
 *         when(action) {
 *             is Action.AddZero -> emptyFlow()
 *             is Action.AddOne -> flowOf(Mutation.Add)
 *             is Action.AddTwo -> flow {
 *                 emit(Mutation.Add)
 *                 emit(Mutation.Add)
 *             }
 *         }
 *     }
 */
typealias Mutator<Action, State, Mutation> = (action: Action, stateAccessor: StateAccessor<State>) -> Flow<Mutation>

/**
 * A [StateAccessor] retrieves the current state in a [Mutator].
 */
typealias StateAccessor<State> = () -> State

/**
 * A [Reducer] takes the previous state and a mutation and returns a new state synchronously.
 *
 *
 * Example:
 *
 *     sealed class Mutation {
 *         object Add : Mutation()
 *         data class Set(val valueToSet: Int) : Mutation()
 *     }
 *
 *     data class State(val value: Int)
 *
 *     reducer = { previousState, mutation ->
 *         when(mutation) {
 *             is Mutation.Add -> previousState.copy(value = previousState.value + 1)
 *             is Mutation.Set -> previousState.copy(value = mutation.valueToSet)
 *         }
 *     }
 */
typealias Reducer<Mutation, State> = (previousState: State, mutation: Mutation) -> State

/**
 * A [Transformer] transforms a [Flow] of a type - such as action, mutation or state.
 *
 *
 * Examples:
 *
 * Transformer<Action> -> Example: Initial action
 *
 *     actionsTransformer = { actions ->
 *         actions.onStart { emit(Action.InitialLoad) }
 *     }
 *
 *
 * Transformer<Mutation> -> Example: Merge global [Flow]
 *
 *     val userSession: Flow<Session>
 *
 *     mutationsTransformer = { mutations ->
 *         flowOf(mutations, userSession.map { Mutation.SetSession(it) }).flattenMerge()
 *     }
 *
 *
 * Transformer<State> -> Example: Logging
 *
 *     statesTransformer = { states ->
 *         states.onEach { Log.d("New State: $it) }
 *     }
 */
typealias Transformer<Type> = (emissions: Flow<Type>) -> Flow<Type>