package at.florianschuster.control

import kotlinx.coroutines.flow.Flow

/**
 * TODO
 */
interface Proxy<Action, State> {
    val controller: Controller<Action, *, State>

    fun dispatch(action: Action) = controller.dispatch(action)

    val currentState: State get() = controller.currentState
    val state: Flow<State> get() = controller.state

    val stub: Stub<Action, *, State> get() = controller.stub
}