package at.florianschuster.control

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.io.PrintStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LogTest {

    @Test
    fun `none logger, methods are not called`() {
        val sut = spyk<ControllerLog.None>()
        assertNull(sut.logger)
    }

    @Test
    fun `println logger, methods are called`() {
        val out = mockk<PrintStream>(relaxed = true)
        System.setOut(out)
        val capturedLogMessage = slot<Any>()
        every { out.println(capture(capturedLogMessage)) } just Runs

        val sut = ControllerLog.Println
        assertNotNull(sut.logger)

        sut.log { CreatedEvent }
        assertEquals(CreatedEvent.toString(), capturedLogMessage.captured)
        sut.log { CompletedEvent }
        assertEquals(CompletedEvent.toString(), capturedLogMessage.captured)
        verify(exactly = 2) { out.println(any<Any>()) }
    }

    @Test
    fun `custom logger, methods are called`() {
        val sut = spyk(ControllerLog.Custom { })
        assertNotNull(sut.logger)

        val capturedLogMessage = slot<String>()
        every { sut.logger.invoke(any(), capture(capturedLogMessage)) } just Runs

        sut.log { CreatedEvent }
        assertEquals(CreatedEvent.toString(), capturedLogMessage.captured)
        sut.log { CompletedEvent }
        assertEquals(CompletedEvent.toString(), capturedLogMessage.captured)
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