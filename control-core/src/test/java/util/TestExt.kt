package util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope

@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(scope: TestCoroutineScope): TestCollector<T> {
    val collector = TestCollector<T>()

    onEach { collector.values.add(it) }
        .catch { e -> collector.errors.add(e) }
        .launchIn(scope)

    return collector
}