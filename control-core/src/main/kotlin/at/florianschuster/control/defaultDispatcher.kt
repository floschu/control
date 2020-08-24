package at.florianschuster.control

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.ContinuationInterceptor

/**
 * Helper to fetch the [CoroutineDispatcher] in the [CoroutineScope].
 */
internal fun CoroutineScope.defaultScopeDispatcher(): CoroutineDispatcher {
    val interceptor = coroutineContext[ContinuationInterceptor]
    checkNotNull(interceptor) { "CoroutineScope does not have an interceptor" }
    return interceptor as CoroutineDispatcher
}
