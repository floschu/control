package at.florianschuster.control.newone

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow

interface Proxy<Event, State> {
    val controller: Controller<Event, *, State>

    val events: SendChannel<Event> get() = controller.events

    val currentState: State get() = controller.currentState
    val state: Flow<State> get() = controller.state
}