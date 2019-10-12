package at.florianschuster.control

import at.florianschuster.control.util.CoroutineScopeRule
import at.florianschuster.control.util.FlowTest
import org.junit.Rule
import org.junit.Test

class ActionProcessorTest : FlowTest {

    @get:Rule
    override val testScopeRule = CoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = ActionProcessor<Int>()

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
        val processor = ActionProcessor<Int>()

        processor(0)
        val testCollector = processor.test()
        processor(1)

        with(testCollector) {
            assertValuesCount(1)
            assertValue(0, 1)
        }
    }

    @Test
    fun `all subscribers collect`() {
        val processor = ActionProcessor<Int>()

        val testCollector0 = processor.test()
        val testCollector1 = processor.test()
        val testCollector2 = processor.test()

        processor(0)
        processor(1)
        processor(2)

        testCollector0.assertValues(listOf(0, 1, 2))
        testCollector1.assertValues(listOf(0, 1, 2))
        testCollector2.assertValues(listOf(0, 1, 2))
    }
}