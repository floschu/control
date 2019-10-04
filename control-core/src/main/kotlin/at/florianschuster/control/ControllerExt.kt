package at.florianschuster.control

import at.florianschuster.control.configuration.ControlConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Maps changes from a [State] Flow and only emits those that are distinct from their immediate
 * predecessors.
 */
@ExperimentalCoroutinesApi
fun <State : Any, SubState : Any> Flow<State>.changesFrom(
    mapper: (State) -> SubState
): Flow<SubState> {
    return map { mapper.invoke(it) }.distinctUntilChanged()
}

/**
 * Binds a [Flow] to an UI target. Also handles errors as defined in [ControlConfig].
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.bind(to: (T) -> Unit): Flow<T> {
    return onEach { to(it) }.catch {
        ControlConfig.handleError(it)
        emitAll(emptyFlow()) // todo test
    }
}

/**
 * Binds a [Flow] to the [Controller.action] channel. Also handles errors as defined in
 * [ControlConfig].
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.bind(to: SendChannel<T>): Flow<T> {
    return onEach { to.offer(it) }.catch {
        ControlConfig.handleError(it)
        emitAll(emptyFlow()) // todo test
    }
}
