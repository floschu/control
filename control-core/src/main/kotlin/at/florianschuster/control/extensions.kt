package at.florianschuster.control

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

/**
 * Consumes a [Flow] of [T] until [other] [Flow] of [U] emits.
 *
 * Example:
 *
 * ```
 * val channel: BroadCastChannel<Int>
 * someFlow
 *     .takeUntil(channel.asFlow().filter{ it == 2 }) // if channel emits 2, someFlow is no longer consumed
 *     .onEach { result -> /* do something*/ }
 *     .launchIn(scope)
 * ```
 */
fun <T, U> Flow<T>.takeUntil(other: Flow<U>): Flow<T> = flow {
    coroutineScope {
        var gate = false

        val job = launch {
            try {
                other.collect { throw TakeUntilException() }
            } catch (ex: TakeUntilException) {
                // this is fine
            } finally {
                gate = true
            }
        }

        try {
            collect {
                if (gate) throw TakeUntilException()
                emit(it)
            }
        } catch (ex: TakeUntilException) {
            // this is also fine
        } finally {
            job.cancel(TakeUntilException())
        }
    }
}

private class TakeUntilException : CancellationException()