package at.florianschuster.control.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope

@ExperimentalCoroutinesApi
interface FlowTest {

    val testScopeRule: TestCoroutineScope

    fun <T> Flow<T>.test(): TestCollector<T> {
        val collector = TestCollector<T>()

        testScopeRule.launch {
            onEach { collector.add(it) }
                .catch { e -> collector.add(e) }
                .onCompletion { collector.complete() }
                .collect()
        }

        return collector
    }
}