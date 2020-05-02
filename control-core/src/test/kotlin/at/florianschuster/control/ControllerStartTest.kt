package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ControllerStartTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `default start mode`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.DEFAULT)

        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.LAZY)

        assertFalse(sut.stateJob.isActive)
        sut.currentState
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `manually start job`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.LAZY)

        assertFalse(sut.stateJob.isActive)
        sut.stateJob.start()
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `manually start via implementation_startStateJob`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.LAZY)

        assertFalse(sut.stateJob.isActive)
        sut.startStateJob()
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `verify double checked locking`() {
        val sut = testCoroutineScope.counterController(coroutineStart = CoroutineStart.LAZY)

        assertFalse(sut.stateJob.isActive)
        sut.stateJob.start()
        assertFalse(sut.startStateJob())
    }

    private fun CoroutineScope.counterController(
        coroutineStart: CoroutineStart
    ) = createSynchronousController<Int, Int>(
        initialState = 0,
        coroutineStart = coroutineStart
    ) as ControllerImplementation<Int, Int, Int>
}