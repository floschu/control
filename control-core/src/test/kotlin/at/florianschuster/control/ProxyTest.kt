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

class ProxyTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `all functions and variables are available`() {
        val proxy = object : Proxy<Unit, Int> {
            override val controller: Controller<Unit, Unit, Int> = Controller(
                initialState = 0,
                scope = testScopeRule,
                mutator = { flowOf(it) },
                reducer = { previousState, _ -> previousState + 1 }
            )
        }

        assertNotNull(proxy.controller)

        proxy.dispatch(Unit)

        assertNotNull(proxy.currentState)
        assertEquals(1, proxy.currentState)

        assertNotNull(proxy.state)

        val testFlow = proxy.state.testIn(testScopeRule)
        testFlow expect lastEmission(1)

        proxy.cancel()
        assertTrue(proxy.controller.cancelled)
        testFlow expect regularCompletion()
    }
}