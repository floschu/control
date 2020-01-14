package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Maps changes from a [State] Flow and only emits those that are distinct
 * from their immediate predecessors.
 */
@ExperimentalCoroutinesApi
fun <State, SubState> Flow<State>.changesFrom(
    mapper: (State) -> SubState
): Flow<SubState> = map { mapper(it) }.distinctUntilChanged()

/**
 * Binds a [Flow] to an non suspending block. Also provides an [errorHandler] for logging.
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.bind(
    to: (T) -> Unit,
    errorHandler: ((Throwable) -> Unit)? = null
): Flow<T> = onEach { to(it) }.catch { e -> errorHandler?.invoke(e) }