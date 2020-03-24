package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LaunchModeTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `default launch mode`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.DEFAULT)

        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `lazy launch mode`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.LAZY)

        assertFalse(sut.stateJob.isActive)
        sut.currentState
        assertTrue(sut.stateJob.isActive)
    }

    private fun CoroutineScope.counterController(
        coroutineStart: CoroutineStart
    ) = createSynchronousController<Int, Int>(
        initialState = 0,
        reducer = { mutation, previousState -> previousState + mutation },
        coroutineStart = coroutineStart
    ) as ControllerImplementation<Int, Int, Int>
}