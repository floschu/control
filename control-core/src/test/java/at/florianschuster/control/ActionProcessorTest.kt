package at.florianschuster.control

import at.florianschuster.control.configuration.configureControl
import at.florianschuster.control.util.CoroutineTestRuleScope
import at.florianschuster.control.util.test
import kotlinx.coroutines.channels.ClosedSendChannelException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionProcessorTest {

    @get:Rule
    val scope = CoroutineTestRuleScope()

    @Before
    fun setup() {
        configureControl { crashes(true) }
    }

    @Test
    fun `emit and collect work correctly`() {
        val processor = ActionProcessor<Int>()

        val testActions = processor.test(scope)

        processor(0)
        processor(1)
        processor(2)

        assertTrue(testActions.count() == 3)
        assertEquals(listOf(0, 1, 2), testActions)
    }

    @Test
    fun `only last value is received after collect`() {
        val processor = ActionProcessor<Int>()

        processor(0)
        val testActions = processor.test(scope)
        processor(1)

        assertTrue(testActions.count() == 1)
        assertEquals(listOf(1), testActions)
    }

    @Test
    fun `all subscribers collect`() {
        val processor = ActionProcessor<Int>()

        val testActions0 = processor.test(scope)
        val testActions1 = processor.test(scope)
        val testActions2 = processor.test(scope)

        processor(0)
        processor(1)
        processor(2)

        assertEquals(listOf(0, 1, 2), testActions0)
        assertEquals(listOf(0, 1, 2), testActions1)
        assertEquals(listOf(0, 1, 2), testActions2)
    }

    @Test
    fun `cancelled processor does not throw error when configured`() {
        configureControl { crashes(false) }
        val processor = ActionProcessor<Int>()

        val testActions = processor.test(scope)

        processor.cancel()
        processor(2)
    }

    @Test(expected = ClosedSendChannelException::class)
    fun `cancelled processor throws error when configured`() {
        configureControl { crashes(true) }
        val processor = ActionProcessor<Int>()

        val testActions = processor.test(scope)

        processor.cancel()
        processor(2)
    }
}