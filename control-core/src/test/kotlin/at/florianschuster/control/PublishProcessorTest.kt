package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.anyError
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.noError
import at.florianschuster.test.flow.testIn
import org.junit.Rule
import org.junit.Test

internal class PublishProcessorTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `emit and collect work correctly`() {
        val processor = PublishProcessor<Int>()

        val testFlow = processor.testIn(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testFlow expect noError()
        testFlow expect emissionCount(3)
        testFlow expect emissions(0, 1, 2)
    }

    @Test
    fun `only last value is received after collect`() {
        val processor = PublishProcessor<Int>()

        processor(0)
        val testFlow = processor.testIn(testScopeRule)
        processor(1)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, 1)
    }

    @Test
    fun `only one subscriber can collect if singleCollector true`() {
        val processor = PublishProcessor<Int>(singleCollector = true)

        val testFlow0 = processor.testIn(testScopeRule)
        val testFlow1 = processor.testIn(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testFlow0 expect noError()
        testFlow0 expect emissions(0, 1, 2)

        testFlow1 expect anyError()
        testFlow1 expect emissionCount(0)
    }

    @Test
    fun `all subscribers collect if singleCollector false`() {
        val processor = PublishProcessor<Int>()

        val testFlow0 = processor.testIn(testScopeRule)
        val testFlow1 = processor.testIn(testScopeRule)
        val testFlow2 = processor.testIn(testScopeRule)

        processor(0)
        processor(1)
        processor(2)

        testFlow0 expect emissions(0, 1, 2)
        testFlow1 expect emissions(0, 1, 2)
        testFlow2 expect emissions(0, 1, 2)
    }
}
