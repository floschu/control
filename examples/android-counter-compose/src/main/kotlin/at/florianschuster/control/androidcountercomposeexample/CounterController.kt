package at.florianschuster.control.androidcountercomposeexample

import at.florianschuster.control.ControllerLog
import at.florianschuster.control.ManagedController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.io.Serializable

class CounterController(
    initialState: State = State()
) : CompositionControllerDelegate<CounterController.Action, CounterController.Mutation, CounterController.State> {

    sealed class Action {
        object Increment : Action()
        object Decrement : Action()
        data class SetValue(val value: Int) : Action()
    }

    sealed class Mutation {
        object IncreaseValue : Mutation()
        object DecreaseValue : Mutation()
        data class SetLoading(val loading: Boolean) : Mutation()
        data class SetValue(val value: Int) : Mutation()
    }

    data class State(
        val value: Int = 0,
        val loading: Boolean = false
    ) : Serializable

    override val controller = ManagedController<Action, Mutation, State>(
        initialState = initialState,
        mutator = { action ->
            when (action) {
                Action.Increment -> flow {
                    emit(Mutation.SetLoading(true))
                    delay(500)
                    emit(Mutation.IncreaseValue)
                    emit(Mutation.SetLoading(false))
                }
                Action.Decrement -> flow {
                    emit(Mutation.SetLoading(true))
                    delay(500)
                    emit(Mutation.DecreaseValue)
                    emit(Mutation.SetLoading(false))
                }
                is Action.SetValue -> flowOf(Mutation.SetValue(action.value))
            }
        },
        reducer = { mutation, previousState ->
            when (mutation) {
                is Mutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
                is Mutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
                is Mutation.SetLoading -> previousState.copy(loading = mutation.loading)
                is Mutation.SetValue -> previousState.copy(value = mutation.value)
            }
        },
        controllerLog = ControllerLog.Println
    )
}
