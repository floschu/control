package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.regularCompletion
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ControllerDelegateTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `all functions and variables are available`() {
        val sut = object : ControllerDelegate<Unit, Int> {
            override val controller: Controller<Unit, Unit, Int> = Controller(
                initialState = 0,
                scope = testScopeRule,
                mutator = { flowOf(it) },
                reducer = { previousState, _ -> previousState + 1 }
            )
        }

        assertNotNull(sut.controller)

        sut.dispatch(Unit)

        assertNotNull(sut.currentState)
        assertEquals(1, sut.currentState)

        assertNotNull(sut.state)

        val testFlow = sut.state.testIn(testScopeRule)
        testFlow expect lastEmission(1)

        sut.cancel()
        assertTrue(sut.controller.cancelled)
        testFlow expect regularCompletion()
    }
}