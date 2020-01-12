package at.florianschuster.control.githubexample.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan

class Machine<Event, Update, State>(
    scope: CoroutineScope,

    initialState: State,

    eventSideEffects: (events: Flow<Event>) -> Flow<Event> = { it },
    eventHandler: (event: Event) -> Flow<Update> = { emptyFlow() },

    updateSideEffects: (updates: Flow<Update>) -> Flow<Update> = { it },
    updater: (previousState: State, update: Update) -> State = { s, _ -> s },

    stateSideEffects: (states: Flow<State>) -> Flow<State> = { it }
) {

    private val eventsChannel: BroadcastChannel<Event> = BroadcastChannel(1)
    private val stateChannel = ConflatedBroadcastChannel<State>()

    val events: SendChannel<Event> get() = eventsChannel
    val state: Flow<State> get() = stateChannel.asFlow()
    val currentState: State get() = stateChannel.value

    init {
        val updateFlow: Flow<Update> = eventSideEffects(eventsChannel.asFlow())
            .flatMapMerge { incomingAction ->
                eventHandler(incomingAction).catch { e -> println(e) }
            }

        val stateFlow: Flow<State> = updateSideEffects(updateFlow)
            .scan(initialState) { previousState, update ->
                updater(previousState, update)
            }
            .catch { e -> println(e) }

        stateSideEffects(stateFlow).onEach(stateChannel::send).launchIn(scope)
    }
}