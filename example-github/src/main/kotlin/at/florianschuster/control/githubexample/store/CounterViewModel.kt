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
    val vm = CounterViewModel(setValueUseCase = flowOf(1, 2, 3))
    vm.actions.offer(CounterAction.Increment)
    vm.state.onEach { it.value }
    vm.currentState
    vm.initialState
}

sealed class CounterAction {
    object Increment : CounterAction()
    object Decrement : CounterAction()
}

data class CounterState(
    val value: Int = 0
)

class CounterViewModel(
    private val setValueUseCase: Flow<Int>
) : ViewModel(), StoreProvider<CounterAction, CounterState> {

    override val initialState: CounterState = CounterState()

    sealed class CounterMutation {
        object IncrementValue : CounterMutation()
        object DecrementValue : CounterMutation()
        data class SetValue(val value: Int) : CounterMutation()
    }

    override val store = Store(
        scope = viewModelScope,
        initialState = initialState,
        actionSideEffects = ::actionSideEffects,
        mutator = ::mutator,
        mutationSideEffects = ::mutationSideEffects,
        reducer = ::reducer
    )

    private fun actionSideEffects(actions: Flow<CounterAction>): Flow<CounterAction> =
        actions.onStart { emit(CounterAction.Increment) }

    private fun mutator(action: CounterAction): Flow<CounterMutation> = when (action) {
        is CounterAction.Increment -> flow {
            delay(1000)
            emit(CounterMutation.IncrementValue)
        }
        is CounterAction.Decrement -> flowOf(CounterMutation.DecrementValue)
    }

    private fun mutationSideEffects(mutations: Flow<CounterMutation>): Flow<CounterMutation> =
        flowOf(mutations, setValueUseCase.map { CounterMutation.SetValue(it) }).flattenMerge()

    private fun reducer(previousState: CounterState, mutation: CounterMutation): CounterState =
        when (mutation) {
            is CounterMutation.IncrementValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecrementValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetValue -> previousState.copy(value = mutation.value)
        }
}
