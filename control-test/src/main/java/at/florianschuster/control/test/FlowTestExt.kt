package at.florianschuster.control.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Tests a [Flow] by creating and returning a [TestCollector] which caches all value
 * emissions, all error emissions and all completions.
 *
 * Similar to RxJava Observable.test()
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(testScope: TestCoroutineScope): TestCollector<T> {
    val collector = TestCollector<T>()
    testScope.launch {
        onEach { collector.add(it) }.catch { e -> collector.add(e) }.collect()
    }
    return collector
}