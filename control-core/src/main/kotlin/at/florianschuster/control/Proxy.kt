package at.florianschuster.control

import kotlinx.coroutines.flow.Flow

/**
 * A proxy interface that contains a [Controller] but only exposes [Action] and [State].
 * It also proxy's [Controller.dispatch], [Controller.currentState], [Controller.state]
 * and [Controller.stub].
 *
 * This could be implemented by a Presenter or a ViewModel.
 */
interface Proxy<Action, State> {

    val controller: Controller<Action, *, State>

    /**
     * See [Controller.dispatch] for more details.
     */
    fun dispatch(action: Action) = controller.dispatch(action)

    /**
     * See [Controller.currentState] for more details.
     */
    val currentState: State get() = controller.currentState

    /**
     * See [Controller.state] for more details.
     */
    val state: Flow<State> get() = controller.state

    /**
     * See [Controller.stub] for more details.
     */
    val stub: Stub<Action, *, State> get() = controller.stub
}