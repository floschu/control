package at.florianschuster.control.counterexample

import at.florianschuster.control.test.TestCollector
import at.florianschuster.control.test.hasEmissionCount
import at.florianschuster.control.test.hasEmissions
import at.florianschuster.control.test.test
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CounterControllerTest {

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    private lateinit var controller: CounterController
    private lateinit var stateCollector: TestCollector<CounterState>

    private fun `given counter controller`() {
        controller = CounterController().apply { scope = testScopeRule }
        stateCollector = controller.state.test(testScopeRule)
    }

    @Test
    fun `action increment triggers correct flow`() = testScopeRule.runBlockingTest {
        // given
        `given counter controller`()

        // when
        controller.action(CounterAction.Increment)
        advanceTimeBy(1000)

        // then
        with(stateCollector) {
            hasEmissionCount(4)
            hasEmissions(
                CounterState(0, false),
                CounterState(0, true),
                CounterState(1, true),
                CounterState(1, false)
            )
        }
    }

    @Test
    fun `actions trigger correct current state`() = testScopeRule.runBlockingTest {
        // given
        `given counter controller`()

        // when
        controller.action(CounterAction.Increment)
        controller.action(CounterAction.Increment)
        controller.action(CounterAction.Decrement)
        controller.action(CounterAction.Decrement)
        controller.action(CounterAction.Decrement)

        advanceTimeBy(5000)

        // then
        assertEquals(CounterState(-1, false), controller.currentState)
    }
}
