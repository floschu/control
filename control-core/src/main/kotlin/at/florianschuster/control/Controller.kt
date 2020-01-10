package at.florianschuster.control

import at.florianschuster.control.configuration.Control
import at.florianschuster.control.configuration.Operation
import at.florianschuster.control.util.AssociatedObject
import at.florianschuster.control.util.ControllerScope
import at.florianschuster.control.util.safeOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan

/**
 * A [Controller] is an UI-independent class that controls the state of a view. The role
 * of a [Controller] is to separate business logic and control flow away from a view. Every
 * view should have its own [Controller] and delegates all logic to it. A [Controller] has no
 * dependency to a view, so it can easily be unit tested.
 *
 * <pre>
 *                  [Action] via [action]
 *          +-----------------------------------+
 *          |                                   |
 *     +----+-----+                    +--------v-------+
 *     |          |                    |                |
 *     |   View   |                    |  [Controller]  |
 *     |          |                    |                |
 *     +----^-----+                    +--------+-------+
 *          |                                  |
 *          +----------------------------------+
 *                  [State] via [state]
 * </pre>
 *
 * Internally the [Controller]...
 * 1. ... receives an [Action] via [action] and handles it inside [Controller.mutate]. Here all
 * asynchronous side effects happen such as e.g. API calls. The function then returns a
 * [Flow] of [Mutation].
 * 2. ... receives a [Mutation] in [Controller.reduce]. Here the previous [State] and the incoming
 * [Mutation] are reduced into a new [State] which is then published via [state].
 *
 * To support global states (such as e.g. a user session) there is a [Controller.transformMutation]
 * that takes global states and maps them to a [Controller] specific [Mutation]. When the global
 * state changes, a new [Mutation] is triggered and the current [State] is reduced.
 *
 * After you are done, call [Controller.cancel] to clear up resources.
 */
@FlowPreview
@ExperimentalCoroutinesApi
interface Controller<Action, Mutation, State> {

    /**
     * A [String] tag that is used for [Operation] logging.
     */
    val tag: String get() = this::class.java.simpleName

    /**
     * A [CoroutineScope] used to launch the [state] [Flow] in.
     * Per default [ControllerScope] is used.
     *
     * For testing with delay controls this can be overwritten with [TestCoroutineScope].
     *
     * This has be set before the [state] [Flow] is created, meaning
     * before accessing [action], [state] or [currentState]
     */
    var scope: CoroutineScope
        get() = AssociatedMap.Scope().valueForOrCreate(this) { ControllerScope() }
        set(value) {
            AssociatedMap.Scope().setValue(this, value)
            Control.log { Operation.ScopeSet(tag, value::class.java.simpleName) }
        }

    /**
     * The initial [State].
     */
    val initialState: State

    /**
     * The current [State].
     */
    val currentState: State get() = _state.value

    /**
     * The [Action] from the view. Bind user inputs to this [PublishProcessor].
     */
    val action: PublishProcessor<Action>
        get() {
            _state // init state stream
            return _action
        }

    /**
     * The [State] [Flow]. Use this to observe the state changes.
     */
    val state: Flow<State> get() = _state.asFlow()

    /**
     * Converts an [Action] to a [Mutation]. This is the place to perform side-effects such as
     * async or suspending tasks.
     */
    fun mutate(action: Action): Flow<Mutation> = emptyFlow()

    /**
     * Generates a new state with the previous [State] and the incoming [Mutation]. The method
     * should be purely functional, it should not perform any side-effects. This method is called
     * every time a [Mutation] is committed via [mutate].
     */
    fun reduce(previousState: State, mutation: Mutation): State = previousState

    /**
     * Transforms the [Action]. Use this function to combine with other [Flow]'s. This method is
     * called once before the [state] [Flow] is created.
     *
     * A possible use case would be to perform an initial action:
     * ```
     * override fun transformAction(action: Flow<Action>): Flow<Action> {
     *     return action.onStart { emit(Action.InitialLoad) }
     * }
     * ```
     */
    fun transformAction(action: Flow<Action>): Flow<Action> = action

    /**
     * Transforms the [Mutation] stream. Implement this method to transform or combine with other
     * [Flow]'s. This method is called once before the [state] [Flow] is created.
     *
     * A possible use case would be to implement a global state:
     * ```
     * val userSession: Flow<Session>
     *
     * override fun transformMutation(mutation: Flow<Mutation>): Flow<Mutation> {
     *     return flowOf(mutation, userSession.map { Mutation.SetSession(it) }).flattenMerge()
     * }
     * ```
     */
    fun transformMutation(mutation: Flow<Mutation>): Flow<Mutation> = mutation

    /**
     * Transforms the [State] stream. This method is called once after the [state] [Flow] is
     * created.
     */
    fun transformState(state: Flow<State>): Flow<State> = state

    /**
     * Clears all resources of this [Controller] and stops the [state] stream.
     */
    fun cancel() {
        scope.cancel()
        AssociatedMap.values().forEach { it().clearFor(this) }
        Control.log { Operation.Canceled(tag) }
    }

    /**
     * Set to true if you want to enable stubbing with [Stub].
     *
     * This has be set before binding [Controller.action] or [Controller.state].
     */
    var stubEnabled: Boolean
        get() = AssociatedMap.StubEnabled().valueForOrCreate(this) { false }
        set(value) {
            AssociatedMap.StubEnabled().setValue(this, value)
            Control.log { Operation.StubEnabled(tag, value) }
        }

    /**
     * Use this [Stub] for View testing.
     */
    val stub: Stub<Action, Mutation, State>
        get() = AssociatedMap.Stub().valueForOrCreate(this) { Stub(this) }

    private val _action: PublishProcessor<Action>
        get() = when {
            stubEnabled -> stub.action
            else -> AssociatedMap.Action().valueForOrCreate(this) { PublishProcessor<Action>() }
        }

    private val _state: ConflatedBroadcastChannel<State>
        get() = when {
            stubEnabled -> stub.state
            else -> AssociatedMap.State().valueForOrCreate(this, ::initState)
        }

    private fun initState(): ConflatedBroadcastChannel<State> {
        val mutationFlow: Flow<Mutation> = transformAction(_action)
            .flatMapMerge {
                Control.log { Operation.Mutate(tag, it) }
                mutate(it).catch { e -> Control.log(e) }
            }

        val stateFlow: Flow<State> = transformMutation(mutationFlow)
            .scan(initialState) { previousState, incomingMutation ->
                val reducedState = reduce(previousState, incomingMutation)
                Control.log { Operation.Reduce(tag, previousState, incomingMutation, reducedState) }
                reducedState
            }
            .catch { e -> Control.log(e) }

        val stateChannel: ConflatedBroadcastChannel<State> = ConflatedBroadcastChannel(initialState)

        // todo use .share() if available
        transformState(stateFlow)
            .onStart { Control.log { Operation.Initialized(tag, initialState) } }
            .onEach { stateChannel.safeOffer(it) }
            .launchIn(scope)

        return stateChannel
    }

    companion object {
        private enum class AssociatedMap(
            private val associatedObject: AssociatedObject = AssociatedObject()
        ) {
            Scope, Action, State, Stub, StubEnabled;

            operator fun invoke(): AssociatedObject = associatedObject
        }
    }
}
