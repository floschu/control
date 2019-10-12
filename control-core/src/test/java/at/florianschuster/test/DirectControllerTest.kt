package at.florianschuster.test

import at.florianschuster.test.util.CoroutineScopeRule
import at.florianschuster.test.util.FlowTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DirectControllerTest : FlowTest {

    @get:Rule
    override val testScopeRule = CoroutineScopeRule()

    @Test
    fun `test direct controller`() {
        val controller = IncrementDirectController()

        val testCollector = controller.state.test()

        controller.action(1)
        controller.action(2)
        controller.action(3)
        controller.action(4)

        with(testCollector) {
            assertValuesCount(5)
            assertValues(listOf(0, 1, 3, 6, 10))
            assertEquals(0 + 1 + 3 + 6 + 10, values.sum())
        }
    }

    private class IncrementDirectController : DirectController<Int, Int> {

        override var scope: CoroutineScope = TestCoroutineScope()
        override val initialState: Int = 0

        override fun reduce(previousState: Int, action: Int): Int = previousState + action
    }
}
