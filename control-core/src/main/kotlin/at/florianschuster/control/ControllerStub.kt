package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Use this [ControllerStub] for view testing.
 */
interface ControllerStub<Action, State> {

    /**
     * [Controller] actions as ordered [List].
     * Use this to verify if view bindings trigger the correct [Action]'s.
     */
    val actions: List<Action>

    /**
     * Offers a mocked [State].
     * Use this to verify if state is correctly bound to a view.
     */
    fun setState(state: State)
}

/**
 * An implementation of [ControllerStub].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class ControllerStubImplementation<Action, State>(
    initialState: State
) : ControllerStub<Action, State> {

    internal val mutableActions = mutableListOf<Action>()
    internal val stateChannel = ConflatedBroadcastChannel(initialState)

    override val actions: List<Action> get() = mutableActions

    override fun setState(state: State) {
        stateChannel.offer(state)
    }
}