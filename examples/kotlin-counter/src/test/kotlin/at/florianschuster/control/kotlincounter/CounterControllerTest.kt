package at.florianschuster.control.kotlincounter

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.TestFlow
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class CounterControllerTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    private lateinit var controller: CounterController
    private lateinit var states: TestFlow<CounterState>

    private fun `given counter controller`() {
        controller = testCoroutineScope.createCounterController()
        states = controller.state.testIn(testCoroutineScope)
    }

    @Test
    fun `action increment triggers correct flow`() {
        // given
        `given counter controller`()

        // when
        controller.dispatch(CounterAction.Increment)
        testCoroutineScope.advanceTimeBy(1000)

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
    fun `actions trigger correct current state`() {
        // given
        `given counter controller`()

        // when
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)

        testCoroutineScope.advanceTimeBy(5000)

        // then
        assertEquals(CounterState(-1, false), controller.currentState)
    }
}
