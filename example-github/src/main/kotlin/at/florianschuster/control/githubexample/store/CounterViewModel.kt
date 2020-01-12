package at.florianschuster.control.githubexample.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

fun test() {
    val vm: ViewModelType<CounterEvent, CounterState> = CounterViewModel(
        setValueUseCase = flowOf(1, 2, 3)
    )
    vm.events.offer(CounterEvent.Increment)
    vm.state.onEach { it.value }
    vm.currentState
    vm.initialState
}

sealed class CounterEvent {
    object Increment : CounterEvent()
    object Decrement : CounterEvent()
    // data class SetValue(val value: Int) : CounterAction()
}

data class CounterState(
    val value: Int = 0
)

class CounterViewModel(
    private val setValueUseCase: Flow<Int>
) : ViewModel(), ViewModelType<CounterEvent, CounterState> {

    override val initialState: CounterState = CounterState()

    sealed class CounterUpdate {
        object IncrementValue : CounterUpdate()
        object DecrementValue : CounterUpdate()
        data class SetValue(val value: Int) : CounterUpdate()
    }

    override val machine = Machine(
        scope = viewModelScope,
        initialState = initialState,
        eventSideEffects = ::eventSideEffects,
        eventHandler = ::eventHandler,
        updateSideEffects = ::updateSideEffects,
        updater = ::updater
    )

    private fun eventSideEffects(actions: Flow<CounterEvent>): Flow<CounterEvent> =
        actions.onStart { emit(CounterEvent.Increment) }

    private fun eventHandler(event: CounterEvent): Flow<CounterUpdate> = when (event) {
        is CounterEvent.Increment -> flow {
            delay(1000)
            emit(CounterUpdate.IncrementValue)
        }
        is CounterEvent.Decrement -> flowOf(CounterUpdate.DecrementValue)
    }

    private fun updateSideEffects(mutations: Flow<CounterUpdate>): Flow<CounterUpdate> =
        flowOf(mutations, setValueUseCase.map { CounterUpdate.SetValue(it) }).flattenMerge()

    private fun updater(previousState: CounterState, update: CounterUpdate): CounterState =
        when (update) {
            is CounterUpdate.IncrementValue -> previousState.copy(value = previousState.value + 1)
            is CounterUpdate.DecrementValue -> previousState.copy(value = previousState.value - 1)
            is CounterUpdate.SetValue -> previousState.copy(value = update.value)
        }
}
