package at.florianschuster.control.test

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class TestCollectorTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `flow without errors produces correct TestCollector`() {
        val testCollector = (0 until 3).asFlow().test(testScopeRule)

        with(testCollector) {
            hasNoErrors()
            assertEquals(emptyList(), errors)

            hasEmissionCount(3)
            hasEmissions(listOf(0, 1, 2))
            hasEmission(index = 0, expected = 0)
            hasEmission(1, 1)
            hasEmission(2, 2)
            assertEquals(listOf(0, 1, 2), emissions)
        }
    }

    @Test
    fun `flow with errors produces correct TestCollector`() {
        val exception = IOException()
        val testCollector = flow {
            emit(0)
            emit(1)
            emit(2)
            throw exception
            emit(3)
        }.test(testScopeRule)

        with(testCollector) {
            hasErrorCount(1)
            hasErrors(listOf(exception))

            hasEmissionCount(3)
            hasEmissions(listOf(0, 1, 2))
        }
    }

    @Test
    fun `flow has no emissions`() {
        val testCollector = flow<Int> { throw IOException() }.test(testScopeRule)

        with(testCollector) {
            hasErrorCount(1)
            hasNoEmissions()
        }
    }

    @Test
    fun `flatMapped flows with error`() {
        val testCollector = (0..10).asFlow()
            .flatMapMerge { value ->
                if (value == 8) throw IOException()
                flow { emit(value * 2) }
            }
            .test(testScopeRule)

        with(testCollector) {
            hasErrorCount(1)
            hasError<IOException>(0)

            hasEmissionCount(8)
            hasEmissions(listOf(0, 2, 4, 6, 8, 10, 12, 14))
        }
    }

    @Test
    fun `resetting TestCollector works correctly`() {
        val channel = BroadcastChannel<Int>(1)
        val testCollector = channel.asFlow().test(testScopeRule)

        channel.offer(1)
        testCollector hasEmissionCount 1

        testCollector.reset()
        testCollector.hasNoEmissions()

        channel.offer(1)
        testCollector hasEmissionCount 1
    }
}