package at.florianschuster.control.counterexample

import at.florianschuster.control.Controller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// action triggered by view
sealed class CounterAction {
    object Increment : CounterAction()
    object Decrement : CounterAction()
}

// mutation that is used to alter the state
sealed class CounterMutation {
    object IncreaseValue : CounterMutation()
    object DecreaseValue : CounterMutation()
    data class SetLoading(val loading: Boolean) : CounterMutation()
}

// immutable state
data class CounterState(
    val value: Int = 0,
    val loading: Boolean = false
)

class CounterController : Controller<CounterAction, CounterMutation, CounterState> {

    // we start with the initial state
    override val initialState: CounterState = CounterState(value = 0, loading = false)

    // every action is transformed into [0..n] mutations
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

    // every mutation is used to reduce the previous state to a new state
    // that is then published to the view
    override fun reduce(previousState: CounterState, mutation: CounterMutation): CounterState =
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
}
