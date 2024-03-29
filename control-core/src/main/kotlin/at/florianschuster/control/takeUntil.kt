package at.florianschuster.control

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Discard any emissions by a [Flow] of [T] if emission matches [predicate].
 * If [inclusive] is true, the last emission matching the [predicate] will be emitted.
 */
fun <T> Flow<T>.takeUntil(
    inclusive: Boolean = false,
    predicate: suspend (T) -> Boolean
): Flow<T> = flow {
    try {
        collect { value ->
            if (predicate(value)) {
                if (inclusive) emit(value)
                throw TakeUntilException()
            } else emit(value)
        }
    } catch (e: TakeUntilException) {
        // this is fine
    }
}

/**
 * Discard any emissions by a [Flow] of [T] after [other] [Flow] of [U] emits an item or completes.
 *
 * Example:
 * If channel emits 2, someFlow is no longer consumed
 *
 * ```
 * val channel: BroadCastChannel<Int>
 * someFlow
 *     .takeUntil(channel.asFlow().filter{ it == 2 })
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
