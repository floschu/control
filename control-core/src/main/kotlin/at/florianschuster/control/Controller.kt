package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan

/**
 * A [Controller] is an UI-independent class that stores and controls the state of a view/a
 * consumer. The role of a [Controller] is to separate business logic away from a view to
 * bundle information and make unit testing easy. Every view/consumer should have its own
 * [Controller] and delegates all logic to it. A [Controller] has no dependency to a view,
 * so it can easily be unit tested.
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
 * 1. ... receives an [Action] via [Controller.dispatch] and handles it inside the
 * [mutator]. Here all asynchronous side effects happen such as e.g. API calls.
 * The function then returns a [Flow] of 0..n [Mutation].
 * 2. ... receives a [Mutation] in [reducer]. Here the previous [State] and the incoming
 * [Mutation] are reduced into a new [State] which is then published via [state].
 *
 * The [Controller] "lives" as long as the [state] [Flow] is active. How long it stays active
 * depends: on the one hand it is tied to the [scope], meaning when [scope] is cancelled, the
 * [Controller] dies. On the other hand when [Controller.cancel] is called, the [Controller]
 * dies too.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class Controller<Action, Mutation, State>(

    /**
     * The initial [State].
     */
    val initialState: State,

    /**
     * A [CoroutineScope] used to launch the [state] [Flow] in.
     * Per default [ControllerScope] is used.
     *
     * For testing with delay controls this can be overwritten with [TestCoroutineScope].
     */
    internal val scope: CoroutineScope = ControllerScope(),

    /**
     * Converts an [Action] to 0..n [Mutation]'s. This is the place to perform side-effects
     * such as async or suspending tasks.
     */
    mutator: (action: Action) -> Flow<Mutation> = { emptyFlow() },

    /**
     * Generates a new state with the previous [State] and the incoming [Mutation]. It is purely
     * functional, it does not perform any side-effects. This method is called every time
     * a [Mutation] is committed via the [mutator].
     */
    reducer: (previousState: State, mutation: Mutation) -> State = { state, _ -> state },

    /**
     * Transforms the [Action] stream. Use this to combine with other [Flow]'s. This method
     * is called once before the [state] [Flow] is created.
     *
     * A possible use case would be to perform an initial action:
     * ```
     * val actionsTransformer: (actions: Flow<Action>) -> Flow<Action> = { actions ->
     *     actions.onStart { emit(Action.InitialLoad) }
     * }
     * ```
     */
    actionsTransformer: (actions: Flow<Action>) -> Flow<Action> = { it },

    /**
     * Transforms the [Mutation] stream. Implement this to transform or combine with other
     * [Flow]'s. This method is called once before the [state] [Flow] is created.
     *
     * A possible use case would be to implement a global state:
     * ```
     * val userSession: Flow<Session>
     *
     * val mutationsTransformer: (mutations: Flow<Mutation>) -> Flow<Mutation> = { mutations ->
     *     flowOf(mutations, userSession.map { Mutation.SetSession(it) }).flattenMerge()
     * }
     * ```
     */
    mutationsTransformer: (mutations: Flow<Mutation>) -> Flow<Mutation> = { it },

    /**
     * Transforms the [State] stream. This method is called once after the [state] [Flow] is
     * created.
     */
    statesTransformer: (states: Flow<State>) -> Flow<State> = { it },

    /**
     * Configuration to define how a [Controller] logs its state errors and operations.
     * You can change the configuration for this [Controller] here or change the
     * [LogConfiguration.DEFAULT] for all.
     */
    var logConfiguration: LogConfiguration = LogConfiguration.DEFAULT
) {

    private val actionChannel = BroadcastChannel<Action>(1)
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    private val stateFlowJob: Job

    /**
     * The [State] [Flow]. Use this to observe the state changes.
     */
    val state: Flow<State>
        get() = if (stubEnabled) stub.stateChannel.asFlow() else stateChannel.asFlow()

    /**
     * The current [State].
     */
    val currentState: State
        get() = if (stubEnabled) stub.stateChannel.value else stateChannel.value

    /**
     * Dispatches an [Action] to be processed by this [Controller].
     */
    fun dispatch(action: Action) {
        if (stubEnabled) {
            stub.actionChannel.offer(action)
        } else {
            actionChannel.offer(action)
        }
    }

    /**
     * Set to true if you want to enable stubbing with [Stub].
     *
     * This has be set before binding [Controller.state].
     */
    var stubEnabled: Boolean = false
        set(value) {
            logConfiguration.log("stub", if (value) "enabled" else "disabled")
            field = value
        }

    /**
     * Use this [Stub] for View testing.
     */
    val stub: Stub<Action, Mutation, State> by lazy { Stub(this) }

    /**
     * Whether the [Controller] is cancelled.
     */
    val cancelled: Boolean get() = stateFlowJob.isCancelled

    init {
        val mutationFlow: Flow<Mutation> = actionsTransformer(actionChannel.asFlow())
            .flatMapMerge { action ->
                logConfiguration.log("action", "$action")
                mutator(action).catch { error ->
                    logConfiguration.log("mutator error", error)
                }
            }

        val stateFlow: Flow<State> = mutationsTransformer(mutationFlow)
            .scan(initialState) { previousState, mutation ->
                logConfiguration.log("mutation", "$mutation")
                reducer(previousState, mutation)
            }
            .catch { error ->
                logConfiguration.log("reducer error", error)
            }

        stateFlowJob = statesTransformer(stateFlow)
            .distinctUntilChanged()
            .onStart { logConfiguration.log("initialized", "$initialState") }
            .onEach { newState ->
                logConfiguration.log("state", "$newState")
                stateChannel.send(newState)
            }
            .onCompletion { error ->
                cleanUp()
                if (error != null) logConfiguration.log("finished", error)
                else logConfiguration.log("finished", "regularly")
            }
            .launchIn(scope)
    }

    /**
     * Cancels the [Controller]. Once a [Controller] is cancelled, the state [Flow] is unusable.
     * Check whether a Controller is cancelled with [Controller.cancelled]
     *
     * @return State the last [currentState] of the [Controller]
     */
    fun cancel(): State {
        val currentState = this.currentState
        cleanUp()
        logConfiguration.log("finished", "via [Controller.cancel]")
        return currentState
    }

    private fun cleanUp() {
        if (!stateFlowJob.isCancelled) stateFlowJob.cancel()
        if (!stateChannel.isClosedForSend) stateChannel.cancel()
        if (!actionChannel.isClosedForSend) actionChannel.cancel()
    }
}