package at.florianschuster.control.counterexample

import at.florianschuster.control.Controller
import at.florianschuster.control.LogConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

// action triggered by view
internal enum class CounterAction {
    Increment, Decrement
}

// mutation that is used to alter the state
internal sealed class CounterMutation {
    object IncreaseValue : CounterMutation()
    object DecreaseValue : CounterMutation()
    data class SetLoading(val loading: Boolean) : CounterMutation()
}

// immutable state
internal data class CounterState(
    val value: Int = 0,
    val loading: Boolean = false
)

internal typealias CounterController = Controller<CounterAction, CounterMutation, CounterState>

@Suppress("FunctionName")
internal fun CounterController(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
): CounterController = Controller(

    // scope that the state flow is launched in
    scope = scope,

    // we start with the initial state
    initialState = CounterState(value = 0, loading = false),

    // every action is transformed into [0..n] mutations
    mutator = { action ->
        when (action) {
            CounterAction.Increment -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(1000)
                emit(CounterMutation.IncreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
            CounterAction.Decrement -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(1000)
                emit(CounterMutation.DecreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
        }
    },

    // every mutation is used to reduce the previous state to a new state
    // that is then published to the view
    reducer = { previousState, mutation ->
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
    },

    // used for logging
    logConfiguration = LogConfiguration.Elaborate("CounterController")
)
