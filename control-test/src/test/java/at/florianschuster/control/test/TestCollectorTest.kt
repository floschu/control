package at.florianschuster.control.test

import kotlinx.coroutines.flow.asFlow
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
            assertNoErrors()
            assertEquals(emptyList(), errors)

            assertValuesCount(3)
            assertValues(listOf(0, 1, 2))
            assertValue(index = 0, expectedValue = 0)
            assertValue(1, 1)
            assertValue(2, 2)
            assertEquals(listOf(0, 1, 2), values)
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
            assertErrorsCount(1)
            assertErrors(listOf(exception))

            assertValuesCount(3)
            assertValues(listOf(0, 1, 2))
        }
    }
}