package at.florianschuster.control.kotlincounter

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class CounterControllerTest {

    @Test
    fun `action increment triggers correct flow`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val controller = createCounterController(initialValue = 0)
        val states = mutableListOf<CounterState>()
        launch { controller.state.toList(states) }

        // when
        controller.dispatch(CounterAction.Increment)
        advanceUntilIdle()

        // then
        assertEquals(
            listOf(
                CounterState(0, false),
                CounterState(0, true),
                CounterState(1, true),
                CounterState(1, false)
            ),
            states
        )

        coroutineContext.cancelChildren()
    }

    @Test
    fun `actions trigger correct state`() = runTest(UnconfinedTestDispatcher()) {
        // given
        val controller = createCounterController(initialValue = 0)

        // when
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Increment)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)
        controller.dispatch(CounterAction.Decrement)

        advanceUntilIdle()

        // then
        assertEquals(CounterState(-1, false), controller.state.value)

        coroutineContext.cancelChildren()
    }
}
