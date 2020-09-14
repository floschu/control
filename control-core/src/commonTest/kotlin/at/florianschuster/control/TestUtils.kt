package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal expect fun suspendTest(block: suspend CoroutineScope.() -> Unit)

internal abstract class TestCoroutineScopeTest {
    protected lateinit var testCoroutineScope: CoroutineScope

    @BeforeTest
    fun setup() {
        testCoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    @AfterTest
    fun tearDown() {
        testCoroutineScope.cancel()
    }

    protected fun <T> Flow<T>.test(): TestFlow<T> = testIn(testCoroutineScope)
}

interface TestFlow<T> {
    val items: List<T>
    fun assertEmissionCount(count: Int)
    fun assertEmissionAt(index: Int, expected: T)
    fun assertEmissions(vararg emissions: T)
    fun assertEmissions(emissions: List<T>)
    fun assertLastEmission(emission: T)
}

internal fun <T> Flow<T>.testIn(scope: CoroutineScope): TestFlow<T> {
    val items = mutableListOf<T>()
    scope.launch { toList(items) }
    return object : TestFlow<T> {
        override val items: List<T> get() = items

        override fun assertEmissionAt(index: Int, expected: T) {
            assertEquals(expected, items[index])
        }

        override fun assertEmissionCount(count: Int) {
            assertEquals(count, items.count())
        }

        override fun assertEmissions(vararg emissions: T) {
            assertEmissions(*emissions)
        }

        override fun assertEmissions(emissions: List<T>) {
            assertEquals(emissions, items)
        }

        override fun assertLastEmission(emission: T) {
            assertEquals(emission, items.last())
        }
    }
}