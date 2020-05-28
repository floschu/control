package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class StartTest {

    @Test
    fun `default start mode`() {
        val scope = TestCoroutineScope(Job())
        val sut = scope.createSimpleCounterController(controllerStart = ControllerStart.Immediately)

        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode`() {
        val scope = TestCoroutineScope(Job())
        val sut = scope.createSimpleCounterController(controllerStart = ControllerStart.Lazy)

        assertFalse(sut.stateJob.isActive)
        sut.currentState
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `managed start mode`() {
        val scope = TestCoroutineScope(Job())
        val sut = scope.createSimpleCounterController(controllerStart = ControllerStart.Managed)

        assertFalse(sut.stateJob.isActive)
        sut.currentState
        assertFalse(sut.stateJob.isActive)

        val started = sut.start()
        assertTrue(started)
        assertTrue(sut.stateJob.isActive)

        sut.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `managed start mode, start when already started`() {
        val sut = TestCoroutineScope().createSimpleCounterController(
            controllerStart = ControllerStart.Managed
        )

        assertFalse(sut.stateJob.isActive)
        sut.start()
        val started = sut.start()
        assertFalse(started)
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `managed start mode, cancel implementation`() {
        val sut = TestCoroutineScope().createSimpleCounterController(
            controllerStart = ControllerStart.Managed
        )

        val started = sut.start()
        assertTrue(sut.stateJob.isActive)
        assertTrue(started)

        sut.dispatch(42)

        sut.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    private fun CoroutineScope.createSimpleCounterController(
        controllerStart: ControllerStart
    ) = ControllerImplementation<Int, Int, Int>(
        scope = this,
        dispatcher = scopeDispatcher,
        controllerStart = controllerStart,
        initialState = 0,
        mutator = { flowOf(it) },
        reducer = { _, previousState -> previousState },
        actionsTransformer = { it },
        mutationsTransformer = { it },
        statesTransformer = { it },
        tag = "ImplementationStartStopTest.SimpleCounterController",
        controllerLog = ControllerLog.None
    )
}