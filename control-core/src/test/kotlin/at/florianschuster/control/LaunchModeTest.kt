package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class LaunchModeTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `setting default LaunchMode`() {
        LaunchMode.default = LaunchMode.Immediate
        assertEquals(LaunchMode.Immediate, LaunchMode.default)

        LaunchMode.default = LaunchMode.OnAccess
        assertEquals(LaunchMode.OnAccess, LaunchMode.default)
    }

    @Test
    fun `immediate launch mode`() {
        val sut = testCoroutineScope.createController<Int,Int,Int>(
            initialState = 0,
            reducer = {

            }
        )
    }

    @Test
    fun `on-access launch mode`() {
        TODO()
    }
}