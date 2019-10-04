package at.florianschuster.control.counterexample

import at.florianschuster.control.Controller
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CounterController(
    initialValue: Int
) : Controller<CounterController.Action, CounterController.Mutation, CounterController.State> {

    sealed class Action {
        object Increase : Action()
        object Decrease : Action()
    }

    sealed class Mutation {
        object IncreaseValue : Mutation()
        object DecreaseValue : Mutation()
        data class SetLoading(val loading: Boolean) : Mutation()
    }

    data class State(
        val value: Int = 0,
        val loading: Boolean = false
    )

    override val initialState: State = State(initialValue)

    override fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.Increase -> flow {
            emit(Mutation.SetLoading(true))
            emit(Mutation.IncreaseValue)
            delay(1000)
            emit(Mutation.SetLoading(false))
        }
        is Action.Decrease -> flow {
            emit(Mutation.SetLoading(true))
            emit(Mutation.DecreaseValue)
            delay(1000)
            emit(Mutation.SetLoading(false))
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State =
        when (mutation) {
            is Mutation.IncreaseValue -> previousState.copy(value = previousState.value + 1)
            is Mutation.DecreaseValue -> previousState.copy(value = previousState.value - 1)
            is Mutation.SetLoading -> previousState.copy(loading = mutation.loading)
        }
}
