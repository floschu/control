package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

/**
 * An interface that contains a [Controller] but only exposes [Action] and [State].
 * It also delegates [Controller.dispatch], [Controller.currentState] and [Controller.state].
 *
 * This could be implemented by a ViewModel.
 */
@ExperimentalCoroutinesApi
@FlowPreview
interface ControllerDelegate<Action, State> {

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
}