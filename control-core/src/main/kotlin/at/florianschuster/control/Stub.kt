package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class Stub<Action, Mutation, State> internal constructor(
    controller: Controller<Action, Mutation, State>
) {

    /**
     * Offers a mocked [State].
     * Use this to verify if state is correctly bound to consumers (e.g. a Views).
     */
    fun setState(mocked: State) {
        stateChannel.offer(mocked)
    }

    /**
     * View actions as ordered [List].
     * Use this to verify if consumer (e.g. a View) bindings trigger the correct [Action]'s.
     */
    val actions: List<Action> get() = _actions

    /**
     * Stubbed state used by [Controller] when [Controller.stubEnabled].
     */
    internal val stateChannel: ConflatedBroadcastChannel<State> =
        ConflatedBroadcastChannel(controller.initialState)

    /**
     * Stubbed action used by [Controller] when [Controller.stubEnabled].
     */
    internal val actionChannel = BroadcastChannel<Action>(1)

    private val _actions: MutableList<Action> = mutableListOf()

    init {
        controller.scope.launch { actionChannel.asFlow().toList(_actions) }
    }
}