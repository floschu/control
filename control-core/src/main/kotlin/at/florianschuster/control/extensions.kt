package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Binds a [Flow] to an non suspending block.
 */
fun <T> Flow<T>.bind(
    to: (T) -> Unit
): Flow<T> = onEach { to(it) }

/**
 * Binds a [Flow] of [Action] to [Controller.dispatch].
 */
fun <Action> Flow<Action>.bind(
    to: Controller<Action, *, *>
): Flow<Action> = bind(to::dispatch)

/**
 * Maps emissions of a [Flow] and only emits those that are distinct from their immediate
 * predecessors.
 */
@ExperimentalCoroutinesApi
fun <State, SubState> Flow<State>.distinctMap(
    by: (State) -> SubState
): Flow<SubState> = map { by(it) }.distinctUntilChanged()