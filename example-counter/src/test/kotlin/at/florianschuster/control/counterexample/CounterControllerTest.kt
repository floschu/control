package at.florianschuster.control.counterexample

import at.florianschuster.control.Controller
import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.TestFlow
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class CounterControllerTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    private lateinit var controller: Controller<CounterAction, CounterMutation, CounterState>
    private lateinit var states: TestFlow<CounterState>

    private fun `given counter controller`() {
        controller = CounterController(testScopeRule)
        states = controller.state.testIn(testScopeRule)
    }

    @Test
    fun `action increment triggers correct flow`() = testScopeRule.runBlockingTest {
        // given
        `given counter controller`()

        // when
        controller.dispatch(CounterAction.Increment)
        advanceTimeBy(1000)

        // then
        states expect emissionCount(4)
        states expect emissions(
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
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)

        advanceTimeBy(5000)

        // then
        assertEquals(CounterState(-1, false), controller.currentState)
    }
}
