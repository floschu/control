package at.florianschuster.control.store

import kotlinx.coroutines.flow.Flow

/**
 * A [Store] handles incoming [Action]'s via [Store.dispatch] and creates new [State]'s that
 * can be collected via [state].
 *
 * Basic Principle: 1 [Action] -> [0..n] [Mutation] -> each 1 new [State]
 *
 * For implementation details look into:
 * 1. [Mutator]: [Action] -> [Mutation]
 * 2. [Reducer]: [Mutation] -> [State]
 * 3. [Transformer]
 * 4. [StoreImplementation]
 */
interface Store<Action, Mutation, State> {

    /**
     * Dispatches an [Action] to be processed by this [Store].
     * Calling this, starts the [Store].
     */
    fun dispatch(action: Action)

    /**
     * The current [State].
     * Accessing this, starts the [Store].
     */
    val currentState: State

    /**
     * The [State] [Flow]. Use this to collect [State] changes.
     * Accessing this, starts the [Store].
     */
    val state: Flow<State>

    /**
     * Set to true if you want to enable stubbing with [stub].
     * This has be set before binding [Store.state].
     */
    var stubEnabled: Boolean

    /**
     * Use this [StoreStub] for view testing.
     */
    val stub: StoreStub<Action, State>

    companion object
}

/**
 * A [Mutator] takes an action and transforms it into [0..n] [Flow]'s of mutations.
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
 *     mutator = { action ->
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
typealias Mutator<Action, Mutation> = (action: Action) -> Flow<Mutation>

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