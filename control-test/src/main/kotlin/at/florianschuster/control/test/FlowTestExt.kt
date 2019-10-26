package at.florianschuster.control.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Tests a [Flow] by creating and returning a [TestCollector] which caches all value
 * emissions and all error emissions.
 *
 * Similar to RxJava Observable.test()
 */
@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(testScope: TestCoroutineScope): TestCollector<T> {
    return TestCollector(this, testScope)
}