package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProxyTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `all functions and variables are available`() {
        val counterProxy = object : Proxy<Unit, Int> {
            override val controller: Controller<Unit, Unit, Int> = Controller(
                initialState = 0,
                scope = testScopeRule,
                mutator = { flowOf(it) },
                reducer = { previousState, _ -> previousState + 1 }
            )
        }

        assertNotNull(counterProxy.controller)

        counterProxy.dispatch(Unit)

        assertNotNull(counterProxy.currentState)
        assertEquals(1, counterProxy.currentState)

        assertNotNull(counterProxy.state)

        val testFlow = counterProxy.state.testIn(testScopeRule)
        testFlow expect lastEmission(1)
    }
}