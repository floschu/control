package at.florianschuster.test.counterexample

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

    @Before
    fun setup() {
        controller = CounterController(0).apply { scope = testScopeRule }
    }

    @Test
    fun `action triggers correct flow`() = testScopeRule.runBlockingTest {
        val controllerStates = mutableListOf<CounterState>()
        testScopeRule.launch { controller.state.toList(controllerStates) }

        controller.action(CounterAction.Increment)

        advanceTimeBy(1000)

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
    fun `multiple actions trigger correct current state`() = testScopeRule.runBlockingTest {
        controller.action(CounterAction.Increment)
        controller.action(CounterAction.Increment)
        controller.action(CounterAction.Decrement)
        controller.action(CounterAction.Decrement)
        controller.action(CounterAction.Decrement)

        advanceTimeBy(5000)

        assertEquals(CounterState(-1, false), controller.currentState)
    }
}
