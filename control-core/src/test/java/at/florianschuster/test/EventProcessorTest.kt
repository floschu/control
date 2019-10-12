package at.florianschuster.test

import at.florianschuster.test.util.CoroutineScopeRule
import at.florianschuster.test.util.FlowTest
import org.junit.Rule
import org.junit.Test

class EventProcessorTest : FlowTest {

    @get:Rule
    override val testScopeRule = CoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = EventProcessor<Int>()

        val testCollector = processor.test()

        processor(0)
        processor(1)
        processor(2)

        with(testCollector) {
            assertNoErrors()
            assertValuesCount(3)
            assertValues(listOf(0, 1, 2))
        }
    }

    @Test
    fun `only last value is received after collect`() {
        val processor = EventProcessor<Int>()

        processor(0)
        val testCollector = processor.test()
        processor(1)

        with(testCollector) {
            assertValuesCount(1)
            assertValue(0, 1)
        }
    }

    @Test
    fun `only one subscriber can collect`() {
        val processor = EventProcessor<Int>()

        val testCollector0 = processor.test()
        val testCollector1 = processor.test()

        processor(0)
        processor(1)
        processor(2)

        with(testCollector0) {
            assertNoErrors()
            assertValues(listOf(0, 1, 2))
        }
        with(testCollector1) {
            assertErrorsCount(1)
            assertValuesCount(0)
        }
    }
}
