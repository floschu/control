package at.florianschuster.control.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Tests a [Flow] by creating and returning a [TestCollector] which caches all value
 * emissions and all error emissions.
 *
 * Similar to RxJava Observable.test()
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(testScope: TestCoroutineScope): TestCollector<T> {
    val collector = TestCollector<T>()
    onEach { collector.add(it) }.catch { e -> collector.add(e) }.launchIn(testScope)
    return collector
}