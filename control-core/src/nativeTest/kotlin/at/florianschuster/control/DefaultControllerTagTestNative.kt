package at.florianschuster.control

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultControllerTagTestNative {

    @Test
    fun `defaultControllerTag in object`() {
        assertEquals("Controller", defaultControllerTag())
    }
}