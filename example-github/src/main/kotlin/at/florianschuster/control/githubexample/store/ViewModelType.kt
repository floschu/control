package at.florianschuster.control.githubexample.store

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow

interface ViewModelType<Event, State> {
    val initialState: State
    val machine: Machine<Event, *, State>

    val events: SendChannel<Event> get() = machine.events

    val currentState: State get() = machine.currentState
    val state: Flow<State> get() = machine.state
}