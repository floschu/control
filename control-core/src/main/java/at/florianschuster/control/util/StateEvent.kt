package at.florianschuster.control.util

import at.florianschuster.control.Controller
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// todo

sealed class StateEvent<out T> {
    object Uninitialized : StateEvent<Nothing>()
    data class Unhandled<T : Any>(val event: T) : StateEvent<T>()
    object Handled : StateEvent<Nothing>()
}

fun <T : Any> Flow<StateEvent<T>>.bla(): Flow<T> {
    return filterIsInstance<StateEvent.Unhandled<T>>()
        .map { it.event }
}

class Cont : Controller<Int, Int, Cont.State> {
    data class State(
        val testEvent: StateEvent<Int> = StateEvent.Uninitialized
    )

    override val initialState: State =
        State()
}

fun test() {
    val cont = Cont()
    cont.state.map { it.testEvent }
        .bla()
        .onEach {

        }
}