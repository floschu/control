package at.florianschuster.control

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LogTest {

    @Test
    fun `none logger - methods are not called`() {
        assertNull(ControllerLog.None.logger)
    }

    @Test
    fun `custom logger - methods are called`() {
        val logs = mutableListOf<String>()
        val sut = ControllerLog.Custom { message -> logs.add(message) }
        assertNotNull(sut.logger)

        sut.log { CreatedEvent }
        assertEquals(CreatedEvent.toString(), logs.last())
        sut.log { CompletedEvent }
        assertEquals(CompletedEvent.toString(), logs.last())
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
        private const val TAG = "TestTag"
        private val CreatedEvent: ControllerEvent = ControllerEvent.Created(TAG, "lazy")
        private val CompletedEvent: ControllerEvent = ControllerEvent.Completed(TAG)
    }
}