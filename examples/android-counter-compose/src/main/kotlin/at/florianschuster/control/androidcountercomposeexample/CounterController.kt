package at.florianschuster.control.androidcountercomposeexample

import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

internal typealias CounterController = Controller<CounterAction, CounterMutation, CounterState>

internal sealed class CounterAction {
    object Increment : CounterAction()
    object Decrement : CounterAction()
}

internal sealed class CounterMutation {
    object IncreaseValue : CounterMutation()
    object DecreaseValue : CounterMutation()
    data class SetLoading(val loading: Boolean) : CounterMutation()
}

internal data class CounterState(
    val value: Int = 0,
    val loading: Boolean = false
)

@Suppress("FunctionName")
internal fun CounterController(
    initialState: CounterState = CounterState()
): CounterController = CompositionController(
    initialState = initialState,
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
    reducer = { mutation, previousState ->
        when (mutation) {
            is CounterMutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is CounterMutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is CounterMutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
    },
    controllerLog = ControllerLog.Println
)