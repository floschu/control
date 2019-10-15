package at.florianschuster.control.processor

import at.florianschuster.control.processor.PublishProcessor
import at.florianschuster.test.util.CoroutineScopeRule
import at.florianschuster.test.util.FlowTest
import org.junit.Rule
import org.junit.Test

class PublishProcessorTest : FlowTest {

    @get:Rule
    override val testScopeRule = CoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = PublishProcessor<Int>()

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
        val processor = PublishProcessor<Int>()

        processor(0)
        val testCollector = processor.test()
        processor(1)

        with(testCollector) {
            assertValuesCount(1)
            assertValue(0, 1)
        }
    }

    @Test
    fun `only one subscriber can collect if singleCollector true`() {
        val processor = PublishProcessor<Int>(singleCollector = true)

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

    @Test
    fun `all subscribers collect if singleCollector false`() {
        val processor = PublishProcessor<Int>()

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
