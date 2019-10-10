package at.florianschuster.control.counterexample

import at.florianschuster.control.Controller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class CounterAction {
    object Increase : CounterAction()
    object Decrease : CounterAction()
}

sealed class CounterMutation {
    object IncreaseValue : CounterMutation()
    object DecreaseValue : CounterMutation()
    data class SetLoading(val loading: Boolean) : CounterMutation()
}

data class CounterState(
    val value: Int,
    val loading: Boolean
)

class CounterController(
    initialValue: Int
) : Controller<CounterAction, CounterMutation, CounterState> {

    override val initialState: CounterState = CounterState(initialValue, false)

    override fun mutate(action: CounterAction): Flow<CounterMutation> = when (action) {
        is CounterAction.Increase -> flow {
            emit(CounterMutation.SetLoading(true))
            emit(CounterMutation.IncreaseValue)
            delay(1000)
            emit(CounterMutation.SetLoading(false))
        }
        is CounterAction.Decrease -> flow {
            emit(CounterMutation.SetLoading(true))
            emit(CounterMutation.DecreaseValue)
            delay(1000)
            emit(CounterMutation.SetLoading(false))
        }
    }

    override fun reduce(previousState: CounterState, mutation: CounterMutation): CounterState =
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
}
