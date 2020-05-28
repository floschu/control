package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.annotations.TestOnly

/**
 * Retrieves a [ControllerStub] for this [Controller] used for view testing.
 * Once accessed, the [Controller] is stubbed and cannot be un-stubbed.
 *
 * Custom implementations of [Controller] cannot be stubbed.
 */
@ExperimentalCoroutinesApi
@FlowPreview
@TestOnly
fun <Action, State> Controller<Action, *, State>.stub(): ControllerStub<Action, State> {
    require(this is ControllerImplementation<Action, *, State>) {
        "Cannot stub a custom implementation of a Controller."
    }
    if (!stubInitialized) {
        controllerLog.log(ControllerEvent.Stub(tag))
        stub = ControllerStubImplementation(initialState)
    }
    return stub
}

/**
 * A stub of a [Controller] for view testing.
 */
interface ControllerStub<Action, State> {

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
 * An implementation of [ControllerStub].
 */
@ExperimentalCoroutinesApi
internal class ControllerStubImplementation<Action, State>(
    initialState: State
) : ControllerStub<Action, State> {

    internal val mutableDispatchedActions = mutableListOf<Action>()
    internal val stateFlow = MutableStateFlow(initialState)

    override val dispatchedActions: List<Action> get() = mutableDispatchedActions

    override fun emitState(state: State) {
        stateFlow.value = state
    }
}
