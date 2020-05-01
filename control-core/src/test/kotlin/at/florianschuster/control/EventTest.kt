package at.florianschuster.control

import org.junit.Test
import kotlin.test.assertTrue

internal class EventTest {

    @Test
    fun `event message contains library name and tag`() {
        val tag = "some_tag"
        val event = ControllerEvent.Created(tag)

        assertTrue(event.toString().contains("control"))
        assertTrue(event.toString().contains(tag))
    }
}