package at.florianschuster.control

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LogTest {

    @Test
    fun `setting default logger`() {
        ControllerLog.default = ControllerLog.Println
        assertEquals(ControllerLog.Println, ControllerLog.default)

        ControllerLog.default = ControllerLog.None
        assertEquals(ControllerLog.None, ControllerLog.default)
    }

    @Test
    fun `setting default message creator`() {
        val expectedMessage = "a test message"
        ControllerLog.defaultMessageCreator = { _, _ -> expectedMessage }
        val spiedLogs = mutableListOf<String>()
        val sut = ControllerLog.Custom { spiedLogs.add(it) }

        sut.log(tag, ControllerLog.Event.Created)
        sut.log(tag, ControllerLog.Event.Destroyed)

        assertEquals(2, spiedLogs.count())
        assertTrue(spiedLogs.all { it == expectedMessage })
    }

    @Test
    fun `none logger, methods are not called`() {
        var spiedAmountOfMessagesCreated = 0
        ControllerLog.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }

        ControllerLog.None.log(tag, ControllerLog.Event.Created)
        ControllerLog.None.log(tag, ControllerLog.Event.Destroyed)

        assertEquals(0, spiedAmountOfMessagesCreated)
    }

    @Test
    fun `println logger, methods are called`() {
        var spiedAmountOfMessagesCreated = 0
        ControllerLog.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }

        ControllerLog.Println.log(tag, ControllerLog.Event.Created)
        ControllerLog.Println.log(tag, ControllerLog.Event.Destroyed)

        assertEquals(2, spiedAmountOfMessagesCreated)
    }

    @Test
    fun `custom logger, methods are called`() {
        var spiedAmountOfMessagesCreated = 0
        ControllerLog.defaultMessageCreator = { tag, _ ->
            spiedAmountOfMessagesCreated++
            tag
        }
        val spiedLogs = mutableListOf<String>()

        ControllerLog.Custom { spiedLogs.add(it) }.log(tag, ControllerLog.Event.Created)
        ControllerLog.Custom { spiedLogs.add(it) }.log(tag, ControllerLog.Event.Destroyed)

        assertEquals(2, spiedLogs.count())
        assertTrue(spiedLogs.all { it.contains(tag) })
        assertEquals(2, spiedAmountOfMessagesCreated)
    }

    companion object {
        private const val tag = "TestTag"
    }
}