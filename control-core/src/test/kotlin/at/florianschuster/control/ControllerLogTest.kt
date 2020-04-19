package at.florianschuster.control

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.spyk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ControllerLogTest {

    @Test
    fun `setting default logger`() {
        ControllerLog.default = ControllerLog.Println
        assertEquals(ControllerLog.Println, ControllerLog.default)

        ControllerLog.default = ControllerLog.None
        assertEquals(ControllerLog.None, ControllerLog.default)
    }

    @Test
    fun `none logger, methods are not called`() {
        val sut = spyk<ControllerLog.None>()
        assertNull(sut.logger)
    }

    @Test
    fun `println logger, methods are called`() {
        val sut = spyk<ControllerLog.Println>()
        assertNotNull(sut.logger)

        val capturedLogMessage = slot<String>()
        every { sut.logger.invoke(any(), capture(capturedLogMessage)) } just Runs

        sut.log(CreatedEvent)
        assertEquals(CreatedEvent.toString(), capturedLogMessage.captured)

        sut.log(DestroyedEvent)
        assertEquals(DestroyedEvent.toString(), capturedLogMessage.captured)
    }

    @Test
    fun `custom logger, methods are called`() {
        val sut = spyk(ControllerLog.Custom { })
        assertNotNull(sut.logger)

        val capturedLogMessage = slot<String>()
        every { sut.logger.invoke(any(), capture(capturedLogMessage)) } just Runs

        sut.log(CreatedEvent)
        assertEquals(CreatedEvent.toString(), capturedLogMessage.captured)
        sut.log(DestroyedEvent)
        assertEquals(DestroyedEvent.toString(), capturedLogMessage.captured)
    }

    companion object {
        private const val tag = "TestTag"
        private val CreatedEvent: ControllerEvent = ControllerEvent.Created(tag)
        private val DestroyedEvent: ControllerEvent = ControllerEvent.Created(tag)
    }
}