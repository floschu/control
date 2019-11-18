package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * Use this [Stub] for View testing.
 * Can be accessed with [Controller.stub].
 */
@FlowPreview
@ExperimentalCoroutinesApi
class Stub<Action, Mutation, State> internal constructor(
    controller: Controller<Action, Mutation, State>
) {

    /**
     * Offers a mocked [State].
     * Use this to verify if state is correctly bound to consumers (e.g. a Views).
     */
    fun setState(mocked: State) {
        state.offer(mocked)
    }

    /**
     * View actions as ordered [List].
     * Use this to verify if consumer (e.g. a View) bindings trigger the correct [Action]'s.
     */
    val actions: List<Action> get() = _actions

    /**
     * Stubbed state used by [Controller] when [Controller.stubEnabled].
     */
    internal val state: ConflatedBroadcastChannel<State> =
        ConflatedBroadcastChannel(controller.initialState)

    /**
     * Stubbed action used by [Controller] when [Controller.stubEnabled].
     */
    internal val action: PublishProcessor<Action> = PublishProcessor()

    private val _actions: MutableList<Action> = mutableListOf()

    init {
        controller.scope.launch { action.toList(_actions) }
    }
}