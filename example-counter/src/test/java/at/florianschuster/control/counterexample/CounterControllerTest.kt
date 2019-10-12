package at.florianschuster.control.counterexample

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class CounterControllerTest {

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    private lateinit var controller: CounterController
    private lateinit var controllerStates: List<CounterState>

    private fun `given counter controller`() {
        controller = CounterController().apply { scope = testScopeRule }
        controllerStates = mutableListOf<CounterState>().also { states ->
            testScopeRule.launch { controller.state.toList(states) }
        }
    }

    @Test
    fun `action increment triggers correct flow`() = testScopeRule.runBlockingTest {
        // given
        `given counter controller`()

        // when
        controller.action(CounterAction.Increment)
        advanceTimeBy(1000)

        // then
        assertEquals(4, controllerStates.count())
        assertEquals(
            listOf(
                CounterState(0, false),
                CounterState(0, true),
                CounterState(1, true),
                CounterState(1, false)
            ),
            controllerStates
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
