package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ImplementationStartStopTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `default start mode`() {
        val sut = testCoroutineScope.createSimpleCounterController(
            coroutineStart = CoroutineStart.DEFAULT
        )

        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `lazy start mode`() {
        val sut = testCoroutineScope.createSimpleCounterController(
            coroutineStart = CoroutineStart.LAZY
        )

        assertFalse(sut.stateJob.isActive)
        sut.currentState
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `manually start implementation`() {
        val sut = testCoroutineScope.createSimpleCounterController(
            coroutineStart = CoroutineStart.LAZY
        )

        assertFalse(sut.stateJob.isActive)
        sut.start()
        assertTrue(sut.stateJob.isActive)
    }

    @Test
    fun `manually cancel implementation`() {
        val sut = testCoroutineScope.createSimpleCounterController(
            coroutineStart = CoroutineStart.LAZY
        )

        sut.start()
        assertTrue(sut.stateJob.isActive)
        sut.cancel()
        assertFalse(sut.stateJob.isActive)
    }

    private fun CoroutineScope.createSimpleCounterController(
        coroutineStart: CoroutineStart = CoroutineStart.LAZY
    ) = ControllerImplementation<Int, Int, Int>(
        scope = this,
        dispatcher = scopeDispatcher,
        coroutineStart = coroutineStart,
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