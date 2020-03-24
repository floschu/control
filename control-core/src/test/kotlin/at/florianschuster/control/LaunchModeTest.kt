package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        val sut = testCoroutineScope.counterController(launchMode = LaunchMode.Immediate)

        assertNotNull(sut.stateJob)
    }

    @Test
    fun `on-access launch mode`() {
        val sut = testCoroutineScope.counterController(launchMode = LaunchMode.OnAccess)

        assertNull(sut.stateJob)
        sut.currentState
        assertNotNull(sut.stateJob)
    }

    private fun CoroutineScope.counterController(
        launchMode: LaunchMode
    ) = createSynchronousController<Int, Int>(
        initialState = 0,
        reducer = { mutation, previousState -> previousState + mutation },
        launchMode = launchMode
    ) as ControllerImplementation<Int, Int, Int>
}