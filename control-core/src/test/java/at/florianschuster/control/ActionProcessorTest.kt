package at.florianschuster.control

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionProcessorTest {

    @Test
    fun `emit and collect work correctly`() = runBlockingTest {
        val processor = ActionProcessor<Int>()
        val testCollector = processor.test(this)

        processor.emit(0)
        processor.emit(1)
        processor.emit(2)

        assertTrue(testCollector.count() == 3)
        assertEquals(listOf(0, 1, 2), testCollector)
    }

    @Test
    fun `only last value is received after collect`() = runBlockingTest {
        val processor = ActionProcessor<Int>()

        processor.emit(0)
        val testCollector = processor.test(this)
        processor.emit(1)

        assertTrue(testCollector.count() == 1)
        assertEquals(listOf(1), testCollector)
    }

    @Test
    fun `all subscribers collect`() = runBlockingTest {
        val processor = ActionProcessor<Int>()

        val testCollector0 = processor.test(this)
        val testCollector1 = processor.test(this)
        val testCollector2 = processor.test(this)

        processor.emit(0)
        processor.emit(1)
        processor.emit(2)

        assertEquals(listOf(0, 1, 2), testCollector0)
        assertEquals(listOf(0, 1, 2), testCollector1)
        assertEquals(listOf(0, 1, 2), testCollector2)
    }
}