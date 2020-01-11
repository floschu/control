package at.florianschuster.control.githubexample.store

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow

interface StoreProvider<Action, State> {
    val initialState: State
    val store: Store<Action, *, State>

    val actions: SendChannel<Action> get() = store.actions

    val currentState: State get() = store.currentState
    val state: Flow<State> get() = store.state
}