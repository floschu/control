package at.florianschuster.control

import at.florianschuster.control.store.StoreLogger
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class StoreLoggerTest {

    @Test
    fun `setting default logger`() {
        StoreLogger.default = StoreLogger.Println
        assertEquals(StoreLogger.Println, StoreLogger.default)

        StoreLogger.default = StoreLogger.None
        assertEquals(StoreLogger.None, StoreLogger.default)
    }

    @Test
    fun `setting default message creator`() {
        val expectedMessage = "a test message"
        StoreLogger.defaultMessageCreator = { _, _ -> expectedMessage }
        val spiedLogs = mutableListOf<String>()
        val sut = StoreLogger.Custom { spiedLogs.add(it) }

        sut.log(tag, StoreLogger.Event.Created)
        sut.log(tag, StoreLogger.Event.Destroyed)

        assertEquals(2, spiedLogs.count())
        assertTrue(spiedLogs.all { it == expectedMessage })
    }

    @Test
    fun `none logger, methods are not called`() {
        var spiedAmountOfMessagesCreated = 0
        StoreLogger.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }

        StoreLogger.None.log(tag, StoreLogger.Event.Created)
        StoreLogger.None.log(tag, StoreLogger.Event.Destroyed)

        assertEquals(0, spiedAmountOfMessagesCreated)
    }

    @Test
    fun `println logger, methods are called`() {
        var spiedAmountOfMessagesCreated = 0
        StoreLogger.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }

        StoreLogger.Println.log(tag, StoreLogger.Event.Created)
        StoreLogger.Println.log(tag, StoreLogger.Event.Destroyed)

        assertEquals(2, spiedAmountOfMessagesCreated)
    }

    @Test
    fun `custom logger, methods are called`() {
        var spiedAmountOfMessagesCreated = 0
        StoreLogger.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }
        val spiedLogs = mutableListOf<String>()

        StoreLogger.Custom { spiedLogs.add(it) }.log(tag, StoreLogger.Event.Created)
        StoreLogger.Custom { spiedLogs.add(it) }.log(tag, StoreLogger.Event.Destroyed)

        assertEquals(2, spiedLogs.count())
        assertTrue(spiedLogs.all { it.contains(tag) })
        assertEquals(2, spiedAmountOfMessagesCreated)
    }

    companion object {
        private const val tag = "TestTag"
    }
}