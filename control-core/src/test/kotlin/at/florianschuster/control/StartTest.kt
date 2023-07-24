package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class StartTest {

    @Test
    fun `start mode logName`() {
        assertEquals("Lazy", ControllerStart.Lazy.logName)
        assertEquals("Immediately", ControllerStart.Immediately.logName)
        assertEquals("Manual", ControllerStart.Manual.logName)
    }

    @Test
    fun `default start mode`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Immediately
        )
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode with state`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Lazy
        )
        assertFalse(sut.stateJob.isActive)

        sut.state
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode with state_value`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Lazy
        )
        assertFalse(sut.stateJob.isActive)

        sut.state.value
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode with dispatch`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Lazy
        )
        assertFalse(sut.stateJob.isActive)

        sut.dispatch(1)
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode with effects`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Lazy
        )
        assertFalse(sut.stateJob.isActive)

        sut.effects
        assertTrue(sut.stateJob.isActive)

        scope.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `manual start mode`() {
        val scope = TestScope(Job())
        val sut = scope.createSimpleCounterController(
            controllerStart = ControllerStart.Manual
        )
        assertFalse(sut.stateJob.isActive)

        sut.state
        sut.state.value
        sut.dispatch(1)
        sut.effects
        assertFalse(sut.stateJob.isActive)

        val started = sut.start()
        assertTrue(started)
        assertTrue(sut.stateJob.isActive)

        sut.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    @Test
    fun `manual start mode, start when already started`() {
        val sut = TestScope().createSimpleCounterController(
            controllerStart = ControllerStart.Manual
        )
        assertFalse(sut.stateJob.isActive)

        sut.start()
        val started = sut.start()
        assertFalse(started)
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `manual start mode, cancel implementation`() {
        val sut = TestScope().createSimpleCounterController(
            controllerStart = ControllerStart.Manual
        )
        assertFalse(sut.stateJob.isActive)

        val started = sut.start()
        assertTrue(sut.stateJob.isActive)
        assertTrue(started)

        sut.dispatch(42)
        sut.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    private fun CoroutineScope.createSimpleCounterController(
        controllerStart: ControllerStart
    ) = ControllerImplementation<Int, Int, Int, Nothing>(
        scope = this,
        dispatcher = defaultScopeDispatcher(),
        controllerStart = controllerStart,
        initialState = 0,
        mutator = { flowOf(it) },
        reducer = { mutation, previousState -> previousState + mutation },
        actionsTransformer = { it },
        mutationsTransformer = { it },
        statesTransformer = { it },
        tag = "ImplementationStartStopTest.SimpleCounterController",
        controllerLog = ControllerLog.None
    )
}