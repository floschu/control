package at.florianschuster.control

import at.florianschuster.control.store.Store
import at.florianschuster.control.store.createStore
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.regularCompletion
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ControllerTest {

    @Test
    fun `all functions and variables are available`() {
        val scope = TestCoroutineScope(Job())

        val sut = object : Controller<Unit, Int> {
            override val store: Store<Unit, Unit, Int> = scope.createStore(
                tag = "test",
                initialState = 0,
                mutator = { flowOf(it) },
                reducer = { previousState, _ -> previousState + 1 }
            )
        }

        assertNotNull(sut.store)

        sut.dispatch(Unit)

        assertNotNull(sut.currentState)
        assertEquals(1, sut.currentState)

        assertNotNull(sut.state)

        val testFlow = sut.state.testIn(scope)
        testFlow expect lastEmission(1)

        scope.cancel()
        testFlow expect regularCompletion()
    }
}