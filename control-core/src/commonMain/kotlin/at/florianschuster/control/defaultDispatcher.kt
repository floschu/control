package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.ContinuationInterceptor

/**
 * Helper to fetch the [CoroutineDispatcher] in the [CoroutineScope].
 */
internal fun CoroutineScope.defaultScopeDispatcher(): CoroutineDispatcher {
    val continuationInterceptor = coroutineContext[ContinuationInterceptor]
    checkNotNull(continuationInterceptor) {
        "CoroutineScope does not have a ContinuationInterceptor"
    }
    return continuationInterceptor as CoroutineDispatcher
}
