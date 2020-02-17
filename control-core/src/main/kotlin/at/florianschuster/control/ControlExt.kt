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
): Flow<T> = onEach { to(it) }

/**
 * Binds a [Flow] of [Action] to [Controller.dispatch].
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <Action> Flow<Action>.bind(
    to: Controller<Action, *>
): Flow<Action> = bind(to::dispatch)