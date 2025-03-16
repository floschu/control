package at.florianschuster.control

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class LogTestJvm {

    @Test
    fun `println logger - methods are called`() {
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        val sut = ControllerLog.Println
        assertNotNull(sut.logger)

        sut.log { CreatedEvent }
        assertTrue(CreatedEvent.toString() in outContent.toString()  )
        sut.log { CompletedEvent }
        assertTrue(CompletedEvent.toString() in outContent.toString())

        System.setOut(originalOut)
    }

    companion object {
        private const val TAG = "TestTag"
        private val CreatedEvent: ControllerEvent = ControllerEvent.Created(TAG, "lazy")
        private val CompletedEvent: ControllerEvent = ControllerEvent.Completed(TAG)
    }
}