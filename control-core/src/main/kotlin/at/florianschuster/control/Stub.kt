package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Use this [Stub] for view testing.
 */
interface Stub<Action, State> {

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

    companion object
}

/**
 * An implementation of [Stub].
 */
@ExperimentalCoroutinesApi
@FlowPreview
internal class StubImplementation<Action, State>(
    initialState: State
) : Stub<Action, State> {

    internal val mutableActions = mutableListOf<Action>()
    internal val stateChannel = ConflatedBroadcastChannel(initialState)

    override val actions: List<Action> get() = mutableActions

    override fun setState(state: State) {
        stateChannel.offer(state)
    }
}