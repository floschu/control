package at.florianschuster.control

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

/**
 * Binds a [Flow] to an non suspending block.
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.bind(
    to: (T) -> Unit
): Flow<T> = onEach { to(it) }.catch { error ->
    LogConfiguration.DEFAULT.log("bind", error)
}

/**
 * Binds a [Flow] of [Action] to [Controller.dispatch].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action> Flow<Action>.bind(
    to: Controller<Action, *, *>
): Flow<Action> = bind(to::dispatch)

/**
 * Binds a [Flow] of [Action] to [Proxy.dispatch].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action> Flow<Action>.bind(
    to: Proxy<Action, *>
): Flow<Action> = bind(to::dispatch)