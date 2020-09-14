package at.florianschuster.control

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    fun `none logger, methods are not called`() {
        assertNull(ControllerLog.None.logger)
    }

    // @Test
    // fun `println logger, methods are called`() {
    //     mockkStatic("kotlin.io.ioH.kt")
    //
    //     val capturedLogMessage = slot<Any>()
    //     every { println(capture(capturedLogMessage)) } just Runs
    //
    //     val sut = ControllerLog.Println
    //     assertNotNull(sut.logger)
    //
    //     sut.log { CreatedEvent }
    //     assertEquals(CreatedEvent.toString(), capturedLogMessage.captured)
    //     sut.log { CompletedEvent }
    //     assertEquals(CompletedEvent.toString(), capturedLogMessage.captured)
    //     verify(exactly = 2) { println(any()) }
    // }

    @Test
    fun `custom logger, methods are called`() {
        val logs = mutableListOf<Pair<String, ControllerEvent>>()
        val sut = ControllerLog.Custom { message -> logs.add(message to event) }
        assertNotNull(sut.logger)

        val firstLog = CreatedEvent
        sut.log { firstLog }
        with(logs.first()) {
            assertEquals(firstLog.toString(), first)
            assertEquals(firstLog, second)
        }

        val secondLog = CompletedEvent
        sut.log { firstLog }
        with(logs.last()) {
            assertEquals(secondLog.toString(), first)
            assertEquals(secondLog, second)
        }
    }

    @Test
    fun `LoggerContext factory function`() {
        val sut = createLoggerContext(CreatedEvent)
        assertEquals(CreatedEvent, sut.event)
    }

    @Test
    fun `log event is only created if logger exists`() {
        var logged = false
        ControllerLog.None.log {
            logged = true
            ControllerEvent.Action("", "")
        }
        assertFalse(logged)

        ControllerLog.Println.log {
            logged = true
            ControllerEvent.Action("", "")
        }
        assertTrue(logged)
    }

    companion object {
        private const val tag = "TestTag"
        private val CreatedEvent: ControllerEvent = ControllerEvent.Created(tag, "lazy")
        private val CompletedEvent: ControllerEvent = ControllerEvent.Completed(tag)
    }
}