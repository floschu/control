package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import util.TestCoroutineScopeRule
import util.test
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DirectControllerTest {

    @get:Rule
    val testScope = TestCoroutineScopeRule()

    @Test
    fun `test direct controller`() {
        val controller = IncrementDirectController()

        val testCollector = controller.state.test(testScope)

        controller.action(1)
        controller.action(2)
        controller.action(3)
        controller.action(4)

        with(testCollector) {
            assertValuesCount(5)
            assertValues(listOf(0, 1, 3, 6, 10))
            assertEquals(0 + 1 + 3 + 6 + 10, testCollector.values.sum())
        }
    }

    private class IncrementDirectController : DirectController<Int, Int> {

        override val scope: CoroutineScope = TestCoroutineScope()
        override val initialState: Int = 0

        override fun reduce(previousState: Int, action: Int): Int = previousState + action
    }
}
