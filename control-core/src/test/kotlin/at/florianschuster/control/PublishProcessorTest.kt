package at.florianschuster.control

import at.florianschuster.control.test.TestCoroutineScopeRule
import at.florianschuster.control.test.emission
import at.florianschuster.control.test.emissionCount
import at.florianschuster.control.test.emissions
import at.florianschuster.control.test.errorCount
import at.florianschuster.control.test.expect
import at.florianschuster.control.test.noErrors
import at.florianschuster.control.test.test
import org.junit.Rule
import org.junit.Test

class PublishProcessorTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = PublishProcessor<Int>()

        val testCollector = processor.test(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testCollector expect noErrors()
        testCollector expect emissionCount(3)
        testCollector expect emissions(0, 1, 2)
    }

    @Test
    fun `only last value is received after collect`() {
        val processor = PublishProcessor<Int>()

        processor(0)
        val testCollector = processor.test(testScopeRule)
        processor(1)

        testCollector expect emissionCount(1)
        testCollector expect emission(0, 1)
    }

    @Test
    fun `only one subscriber can collect if singleCollector true`() {
        val processor = PublishProcessor<Int>(singleCollector = true)

        val testCollector0 = processor.test(testScopeRule)
        val testCollector1 = processor.test(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testCollector0 expect noErrors()
        testCollector0 expect emissions(0, 1, 2)

        testCollector1 expect errorCount(1)
        testCollector1 expect emissionCount(0)
    }

    @Test
    fun `all subscribers collect if singleCollector false`() {
        val processor = PublishProcessor<Int>()

        val testCollector0 = processor.test(testScopeRule)
        val testCollector1 = processor.test(testScopeRule)
        val testCollector2 = processor.test(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testCollector0 expect emissions(0, 1, 2)
        testCollector1 expect emissions(0, 1, 2)
        testCollector2 expect emissions(0, 1, 2)
    }
}
