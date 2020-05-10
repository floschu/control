package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Use this [ControllerStub] for view testing.
 */
interface ControllerStub<Action, State> {

    /**
     * [Controller] [Action]'s as ordered [List].
     * Use this to verify if view bindings trigger the correct [Action]'s.
     */
    val actions: List<Action>

    /**
     * Offers a mocked [State].
     * Use this to verify if [State] is correctly bound to a view.
     */
    fun setState(state: State)
}

/**
 * An implementation of [ControllerStub].
 */
@ExperimentalCoroutinesApi
internal class ControllerStubImplementation<Action, State>(
    initialState: State
) : ControllerStub<Action, State> {

    internal val mutableActions = mutableListOf<Action>()
    internal val stateFlow = MutableStateFlow(initialState)

    override val actions: List<Action> get() = mutableActions

    override fun setState(state: State) {
        stateFlow.value = state
    }
}