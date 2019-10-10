package at.florianschuster.control

import at.florianschuster.control.configuration.Control
import at.florianschuster.control.configuration.Operation
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
import kotlinx.coroutines.flow.scan
import java.lang.Exception

/**
 * A [Controller] is an UI-independent layer which manages the state of a view. The foremost role
 * of a [Controller] is to separate control flow from a view. Every view should have its own
 * [Controller] and delegates all logic to it. A [Controller] has no dependency to a view, so it
 * can be easily tested.
 */
@FlowPreview
@ExperimentalCoroutinesApi
interface Controller<Action : Any, Mutation : Any, State : Any> : ObjectStore {

    /**
     * A [String] tag that is used for [Operation] logging.
     */
    val tag: String get() = this::class.java.simpleName

    /**
     * A [CoroutineScope] used to launch the [state] [Flow] in.
     */
    val scope: CoroutineScope get() = associatedObject(SCOPE_KEY) { ControllerScope() }

    /**
     * The initial [State].
     */
    val initialState: State

    /**
     * The current [State].
     */
    val currentState: State get() = privateState.value

    /**
     * The [Action] from the view. Bind user inputs to this [ActionProcessor].
     */
    val action: ActionProcessor<Action>
        get() {
            privateState // init state stream
            return privateAction
        }

    /**
     * The [State] [Flow]. Use this to observe the state changes.
     */
    val state: Flow<State> get() = privateState.asFlow()

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
     */
    fun transformAction(action: Flow<Action>): Flow<Action> = action

    /**
     * Transforms the [Mutation] stream. Implement this method to transform or combine with other
     * [Flow]'s. This method is called once before the [state] [Flow] is created.
     */
    fun transformMutation(mutation: Flow<Mutation>): Flow<Mutation> = mutation

    /**
     * Destroys this [Controller].
     */
    fun cancel() {
        scope.cancel()
        clearAssociatedObjects()
        Control.log { Operation.Destroyed(tag) }
    }

    private val privateAction: ActionProcessor<Action>
        get() = associatedObject(ACTION_KEY) { ActionProcessor() }

    private val privateState: ConflatedBroadcastChannel<State>
        get() = associatedObject(STATE_KEY) { initState() }

    private fun initState(): ConflatedBroadcastChannel<State> {
        val mutationFlow: Flow<Mutation> = transformAction(privateAction)
            .flatMapMerge {
                Control.log { Operation.Mutate(tag, it.toString()) }
                mutate(it).catch { e -> Control.log(e) }
            }

        val stateFlow: Flow<State> = transformMutation(mutationFlow)
            .scan(initialState) { previousState, incomingMutation ->
                val reducedState = reduce(previousState, incomingMutation)
                Control.log {
                    Operation.Reduce(
                        tag,
                        previousState.toString(),
                        incomingMutation.toString(),
                        reducedState.toString()
                    )
                }
                reducedState
            }
            .catch { e -> Control.log(e) }

        val stateChannel: ConflatedBroadcastChannel<State> = ConflatedBroadcastChannel()

        // todo use future .share() or maybe stateFlow
        stateFlow.onEach {
            try {
                stateChannel.offer(it)
            } catch (e: Exception) {
                Control.log(e)
            }
        }.launchIn(scope)

        Control.log { Operation.Initialized(tag, initialState.toString()) }

        return stateChannel
    }

    companion object {
        private const val SCOPE_KEY = "controller_scope"
        private const val ACTION_KEY = "controller_action"
        private const val STATE_KEY = "controller_state"
    }
}
