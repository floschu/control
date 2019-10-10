package at.florianschuster.control

import at.florianschuster.control.configuration.Control
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Maps changes from a [State] Flow and only emits those that are distinct from their immediate
 * predecessors.
 */
@ExperimentalCoroutinesApi
fun <State : Any, SubState : Any> Flow<State>.changesFrom(
    mapper: (State) -> SubState
): Flow<SubState> = map { mapper(it) }.distinctUntilChanged()

/**
 * Binds a [Flow] to an UI target.
 * Also handles errors as defined in [Control].
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.bind(to: (T) -> Unit): Flow<T> =
    onEach { to(it) }.catch { e -> Control.log(e) }