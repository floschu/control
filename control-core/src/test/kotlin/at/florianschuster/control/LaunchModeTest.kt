package at.florianschuster.control

import org.junit.Test
import kotlin.test.assertEquals

internal class LaunchModeTest {

    @Test
    fun `setting default LaunchMode`() {
        LaunchMode.default = LaunchMode.Immediate
        assertEquals(LaunchMode.Immediate, LaunchMode.default)

        LaunchMode.default = LaunchMode.Lazy
        assertEquals(LaunchMode.Lazy, LaunchMode.default)
    }

    @Test
    fun `immediate launch mode`() {
        TODO()
    }

    @Test
    fun `lazy launch mode`() {
        TODO()
    }
}