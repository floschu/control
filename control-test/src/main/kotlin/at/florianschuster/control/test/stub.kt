package at.florianschuster.control.test

import at.florianschuster.control.Controller
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * [ControllerStub] for view testing. Can be provided instead of the [Controller].
 */
interface ControllerStub<Action, Mutation, State> : Controller<Action, Mutation, State> {

    /**
     * The [Action]'s dispatched to the [Controller] as ordered [List].
     * Use this to verify if view bindings trigger the correct [Action]'s.
     */
    val dispatchedActions: List<Action>

    /**
     * Emits a new [State] for [Controller.state] and [Controller.currentState].
     * Use this to verify if [State] is correctly bound to a view.
     */
    fun emitState(state: State)
}

/**
 * Factory function to create a [ControllerStub].
 */
@ExperimentalCoroutinesApi
@FlowPreview
@Suppress("FunctionName")
fun <Action, Mutation, State> ControllerStub(
    initialState: State
): ControllerStub<Action, Mutation, State> = ControllerStubImplementation(initialState)

/**
 * An implementation of [ControllerStub].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class ControllerStubImplementation<Action, Mutation, State>(
    initialState: State
) : ControllerStub<Action, Mutation, State> {

    // controller

    override val currentState: State get() = stateChannel.value
    override val state: Flow<State> get() = stateChannel.asFlow()

    override fun dispatch(action: Action) {
        mutableActions.add(action)
    }

    // stub

    private val mutableActions = mutableListOf<Action>()
    private val stateChannel = ConflatedBroadcastChannel(initialState)

    override val dispatchedActions: List<Action> get() = mutableActions

    override fun emitState(state: State) {
        stateChannel.offer(state)
    }
}