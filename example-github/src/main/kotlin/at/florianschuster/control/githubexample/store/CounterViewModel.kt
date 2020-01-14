package at.florianschuster.control.githubexample.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.newone.Controller
import at.florianschuster.control.newone.Proxy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

// represent user/consumer actions. are sent by view e.g.
sealed class CounterEvent {
    object Increment : CounterEvent()
    object Decrement : CounterEvent()
}

// represents the current state. is collected in view. e.g.
data class CounterState(
    val value: Int = 0
)

class SimpleCounterViewModel : ViewModel(),
    Proxy<CounterEvent, CounterState> {

    // this is where the magic happens. completely composable/pluggable.
    // state flow gets cancelled when scope is cancelled.
    override val controller = Controller(
        scope = viewModelScope,
        initialState = CounterState(),
        eventHandler = ::eventHandler,
        updater = ::updater
    )

    // represent state changes/updates. only needed in the store
    sealed class Update {
        object IncrementValue : Update()
        object DecrementValue : Update()
    }

    // handles user/consumer events and transforms them into 0..n state updates
    private fun eventHandler(event: CounterEvent): Flow<Update> = when (event) {
        is CounterEvent.Increment -> flowOf(Update.IncrementValue)
        is CounterEvent.Decrement -> flowOf(Update.DecrementValue)
    }

    // updates the state. this is a pure synchronous function
    private fun updater(previousState: CounterState, update: Update): CounterState =
        when (update) {
            is Update.IncrementValue -> previousState.copy(value = previousState.value + 1)
            is Update.DecrementValue -> previousState.copy(value = previousState.value - 1)
        }
}

// a use case that emits a flow with values
private val valueFlowUseCase: Flow<Int> = flow {
    delay(3000)
    emit(1)
    delay(1000)
    emit(2)
    delay(1000)
    emit(3)
}

// a use case that performs a very hard computation to get the value
private val valueSuspendUseCase: suspend () -> Int = {
    delay(1000000)
    1 + 1
}

class UseCaseCounterViewModel(
    private val incrementByFlowValueUseCase: Flow<Int> = valueFlowUseCase,
    private val incrementBySuspendValueUseCase: suspend () -> Int = valueSuspendUseCase
) : ViewModel(), Proxy<CounterEvent, CounterState> {

    sealed class Update {
        data class IncrementValue(val by: Int = 1) : Update()
        object DecrementValue : Update()
    }

    override val controller = Controller(
        scope = viewModelScope,
        initialState = CounterState(),
        eventHandler = ::eventHandler,
        updater = ::updater,

        // these transformers are a possibility to transform the different flows
        eventsTransformer = ::eventsTransformer,
        updatesTransformer = ::updatesTransformer,
        statesTransformer = ::statesTransformer
    )

    private fun eventHandler(event: CounterEvent): Flow<Update> = when (event) {
        is CounterEvent.Increment -> flow {
            val value = incrementBySuspendValueUseCase()
            emit(Update.IncrementValue(by = value))
        }
        is CounterEvent.Decrement -> flowOf(Update.DecrementValue)
    }

    private fun updater(previousState: CounterState, update: Update): CounterState = when (update) {
        is Update.IncrementValue -> previousState.copy(value = previousState.value + update.by)
        is Update.DecrementValue -> previousState.copy(value = previousState.value - 1)
    }

    private fun eventsTransformer(events: Flow<CounterEvent>): Flow<CounterEvent> =
        events.onStart { emit(CounterEvent.Increment) } // initial action

    private fun updatesTransformer(updates: Flow<Update>): Flow<Update> =
        flowOf(
            updates,
            incrementByFlowValueUseCase.map { Update.IncrementValue(by = it) }
        ).flattenMerge() // merge use case with updates flow to bring global states into vm state

    private fun statesTransformer(states: Flow<CounterState>): Flow<CounterState> =
        states.onEach { print("New State: $it") }
}

class NoEventCounterViewModel(
    private val useCase: Flow<Int> = valueFlowUseCase
) : ViewModel(),
    Proxy<Nothing, CounterState> {

    sealed class Update {
        data class IncrementValue(val by: Int = 1) : Update()
        object DecrementValue : Update()
    }

    override val controller =
        Controller<Nothing, Update, CounterState>(
            scope = viewModelScope,
            initialState = CounterState(),
            updater = ::updater,
            updatesTransformer = ::updatesTransformer
        )

    private fun updatesTransformer(updates: Flow<Update>): Flow<Update> =
        flowOf(updates, useCase.map { Update.IncrementValue(by = it) }).flattenMerge()

    private fun updater(previousState: CounterState, update: Update): CounterState = when (update) {
        is Update.IncrementValue -> previousState.copy(value = previousState.value + update.by)
        is Update.DecrementValue -> previousState.copy(value = previousState.value - 1)
    }
}
