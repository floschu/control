package at.florianschuster.control.newone

import at.florianschuster.control.util.ControllerScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class Controller<Event, Update, State>(
    val initialState: State,
    val scope: CoroutineScope = ControllerScope(),

    eventHandler: (event: Event) -> Flow<Update> = { emptyFlow() },
    updater: (previousState: State, update: Update) -> State = { prev, _ -> prev },

    eventsTransformer: (events: Flow<Event>) -> Flow<Event> = { it },
    updatesTransformer: (updates: Flow<Update>) -> Flow<Update> = { it },
    statesTransformer: (states: Flow<State>) -> Flow<State> = { it },

    var tag: String = ""
) {

    private val eventsChannel = BroadcastChannel<Event>(1)
    private val stateChannel = ConflatedBroadcastChannel<State>()
    private val stub by lazy { Stub(this) }

    val events: SendChannel<Event>
        get() = if (stubEnabled) stub.eventsChannel else eventsChannel

    val state: Flow<State>
        get() = if (stubEnabled) stub.stateChannel.asFlow() else stateChannel.asFlow()

    val currentState: State
        get() = if (stubEnabled) stub.stateChannel.value else stateChannel.value

    var stubEnabled: Boolean = false
        set(value) {
            println("Controller.$tag/Stub ${if (value) "enabled" else "disabled"}")
            field = value
        }

    init {
        val updateFlow: Flow<Update> = eventsTransformer(eventsChannel.asFlow())
            .flatMapMerge { event ->
                println("Controller.$tag/Event: $event")
                eventHandler(event).catch { error -> println(error) }
            }

        val stateFlow: Flow<State> = updatesTransformer(updateFlow)
            .scan(initialState) { previousState, update ->
                println("Controller.$tag/Update: $update")
                updater(previousState, update)
            }
            .catch { error -> println(error) }

        statesTransformer(stateFlow)
            .distinctUntilChanged()
            .onEach { newState ->
                stateChannel.send(newState)
                println("Controller.$tag/State: $newState")
            }
            .onStart { println("Controller.$tag/Started: $initialState") }
            .onCompletion { error ->
                println("Controller.$tag/Stopped${if (error != null) ": $error" else ""}")
            }
            .launchIn(scope)
    }
}

class Stub<Event, Update, State> internal constructor(
    controller: Controller<Event, Update, State>
) {

    /**
     * Offers a mocked [State].
     * Use this to verify if state is correctly bound to consumers (e.g. a Views).
     */
    fun setState(mocked: State) {
        stateChannel.offer(mocked)
    }

    /**
     * View actions as ordered [List].
     * Use this to verify if consumer (e.g. a View) bindings trigger the correct [Event]'s.
     */
    val events: List<Event> get() = _events

    /**
     * Stubbed state used by [Controller] when [Controller.stubEnabled].
     */
    internal val stateChannel: ConflatedBroadcastChannel<State> =
        ConflatedBroadcastChannel(controller.initialState)

    /**
     * Stubbed action used by [Controller] when [Controller.stubEnabled].
     */
    internal val eventsChannel = BroadcastChannel<Event>(1)

    private val _events: MutableList<Event> = mutableListOf()

    init {
        controller.scope.launch { eventsChannel.asFlow().toList(_events) }
    }
}