package at.florianschuster.test.counterexample

import at.florianschuster.test.Controller
import at.florianschuster.test.ControllerScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class CounterAction {
    object Increment : CounterAction()
    object Decrement : CounterAction()
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
        is CounterAction.Increment -> flow {
            emit(CounterMutation.SetLoading(true))
            delay(1000)
            emit(CounterMutation.IncreaseValue)
            emit(CounterMutation.SetLoading(false))
        }
        is CounterAction.Decrement -> flow {
            emit(CounterMutation.SetLoading(true))
            delay(1000)
            emit(CounterMutation.DecreaseValue)
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
