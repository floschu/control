package at.florianschuster.control

import at.florianschuster.control.configuration.configureControl
import kotlinx.coroutines.channels.ClosedSendChannelException
import util.TestCoroutineScopeRule
import util.test
import org.junit.Rule
import org.junit.Test

class ActionProcessorTest {

    @get:Rule
    val scope = TestCoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = ActionProcessor<Int>()

        val testCollector = processor.test(scope)

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
        val testCollector = processor.test(scope)
        processor(1)

        with(testCollector) {
            assertValuesCount(1)
            assertValues(listOf(1))
        }
    }

    @Test
    fun `all subscribers collect`() {
        val processor = ActionProcessor<Int>()

        val testCollector0 = processor.test(scope)
        val testCollector1 = processor.test(scope)
        val testCollector2 = processor.test(scope)

        processor(0)
        processor(1)
        processor(2)

        testCollector0.assertValues(listOf(0, 1, 2))
        testCollector1.assertValues(listOf(0, 1, 2))
        testCollector2.assertValues(listOf(0, 1, 2))
    }

    @Test
    fun `cancelled processor does not throw error`() {
        val processor = ActionProcessor<Int>()

        val ignore = processor.test(scope)

        processor.cancel()
        processor(2)
    }
}