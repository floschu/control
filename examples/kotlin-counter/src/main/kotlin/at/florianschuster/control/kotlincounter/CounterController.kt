package at.florianschuster.control.kotlincounter

import at.florianschuster.control.ControllerLog
import at.florianschuster.control.Controller
import at.florianschuster.control.createController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

typealias CounterController = Controller<CounterAction, CounterMutation, CounterState>

/**
 * actions triggered by the view.
 */
sealed class CounterAction {
    object Increment : CounterAction()
    object Decrement : CounterAction()
}

/**
 * mutations that are used to alter the state.
 */
sealed class CounterMutation {
    object IncreaseValue : CounterMutation()
    object DecreaseValue : CounterMutation()
    data class SetLoading(val loading: Boolean) : CounterMutation()
}

/**
 * the immutable state.
 */
data class CounterState(
    val value: Int = 0,
    val loading: Boolean = false
)

/**
 * creates a [CounterController] from the [CoroutineScope].
 */
fun CoroutineScope.createCounterController(): CounterController = createController(

    // we start with the initial state
    initialState = CounterState(value = 0, loading = false),

    // every action is transformed into [0..n] mutations
    mutator = { action ->
        when (action) {
            CounterAction.Increment -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(500)
                emit(CounterMutation.IncreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
            CounterAction.Decrement -> flow {
                emit(CounterMutation.SetLoading(true))
                delay(500)
                emit(CounterMutation.DecreaseValue)
                emit(CounterMutation.SetLoading(false))
            }
        }
    },

    // every mutation is used to reduce the previous state to a new state
    // that is then published to the view
    reducer = { mutation, previousState ->
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
    },

    // logs to println
    controllerLog = ControllerLog.Println
)
