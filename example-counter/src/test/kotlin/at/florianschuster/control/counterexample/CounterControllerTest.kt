package at.florianschuster.control.counterexample

import at.florianschuster.control.test.TestCollector
import at.florianschuster.control.test.TestCoroutineScopeRule
import at.florianschuster.control.test.emissions
import at.florianschuster.control.test.emissionCount
import at.florianschuster.control.test.expect
import at.florianschuster.control.test.test
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CounterControllerTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

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
        stateCollector expect emissionCount(4)
        stateCollector expect emissions(
            CounterState(0, false),
            CounterState(0, true),
            CounterState(1, true),
            CounterState(1, false)
        )
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
