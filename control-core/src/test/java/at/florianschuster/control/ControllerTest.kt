package at.florianschuster.control

import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

class ControllerTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `initial state only emitted once`() = testDispatcher.runBlockingTest {
        val controller = TestController()
        val testStateList = controller.state.toList()

        assertEquals(testStateList.count(), 1)
    }

    @Test
    fun `each method is invoked`() = runBlockingTest {
        val controller = TestController()
        val testStateList = controller.state.toList()

        controller.action.offer(arrayListOf("action"))

        assertEquals(testStateList.count(), 2)
        assertEquals(testStateList[0], listOf("transformedState"))
        assertEquals(
            testStateList[1], listOf(
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            )
        )
    }

    @Test
    fun `state replay current state`() = runBlockingTest {
        val controller = CounterController()
        val testStateList = controller.state.toList() // state: 0

        controller.action.offer(Unit) // state: 1
        controller.action.offer(Unit) // state: 2

        assertEquals(testStateList.count(), 3)
        assertEquals(testStateList, listOf(0, 1, 2))
    }

    @Test
    fun `current state`() = runBlockingTest {
        val controller = TestController()
        controller.state
        controller.action.offer(listOf("action"))

        assertEquals(
            controller.currentState, listOf(
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            )
        )
    }

    @Test
    fun `state is created when accessing action`() = runBlockingTest {
        val controller = TestController()
        controller.action.offer(listOf("action"))

        assertEquals(
            controller.currentState, listOf(
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            )
        )
    }

    @Test
    fun `stream ignores error from mutate`() = runBlockingTest {
        val controller = CounterController()
        val testStateList = controller.state.toList()

        controller.stateIndexToTriggerError = 2
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)

        assertEquals(testStateList.size, 6)
        assertEquals(testStateList, listOf(0, 1, 2, 3, 4, 5))
    }

    @Test
    fun `stream ignores completed from mutate`() = runBlockingTest {
        val controller = CounterController()
        val testStateList = controller.state.toList()

        controller.stateIndexToTriggerCompleted = 2
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)
        controller.action.offer(Unit)

        assertEquals(testStateList, listOf(0, 1, 2, 3, 4, 5))
    }

//    @Test
//    fun testCancel() = runBlockingTest {
//        RxJavaPlugins.reset()
//
//        val testScheduler = TestScheduler()
//        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
//
//        val controller = StopwatchController()
//
//        controller.action.accept(StopwatchController.Action.Start)
//        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
//        controller.action.accept(StopwatchController.Action.Stop)
//
//        controller.action.accept(StopwatchController.Action.Start)
//        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
//        controller.action.accept(StopwatchController.Action.Stop)
//
//        controller.action.accept(StopwatchController.Action.Start)
//        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)
//        controller.action.accept(StopwatchController.Action.Stop)
//
//        // this should be ignored
//        controller.action.accept(StopwatchController.Action.Start)
//        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
//        controller.action.accept(StopwatchController.Action.Stop)
//
//        controller.action.accept(StopwatchController.Action.Start)
//        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
//        controller.action.accept(StopwatchController.Action.Stop)
//
//        assertEquals(controller.currentState ,10) // 2+3+4+1
//
//        RxJavaPlugins.reset()
//    }
}

private class TestController : Controller<List<String>, List<String>, List<String>> {

    override val initialState: List<String> = emptyList()

    // 1. ["action"] + ["transformedAction"]
    override fun transformAction(action: Flow<List<String>>): Flow<List<String>> {
        return action.map { it + "transformedAction" }
    }

    // 2. ["action", "transformedAction"] + ["mutation"]
    override fun mutate(incomingAction: List<String>): Flow<List<String>> {
        return flowOf(incomingAction + "mutation")
    }

    // 3. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
    override fun transformMutation(mutation: Flow<List<String>>): Flow<List<String>> {
        return mutation.map { it + "transformedMutation" }
    }

    // 4. [] + ["action", "transformedAction", "mutation", "transformedMutation"]
    override fun reduce(previousState: List<String>, incomingMutation: List<String>): List<String> {
        return previousState + incomingMutation
    }
}

private class CounterController : Controller<Unit, Unit, Int> {
    override val initialState: Int = 0

    var stateIndexToTriggerError: Int? = null
    var stateIndexToTriggerCompleted: Int? = null

    override fun mutate(action: Unit): Flow<Unit> = when (currentState) {
        stateIndexToTriggerError -> flow {
            emit(action)
            throw Error()
        }
        stateIndexToTriggerCompleted -> callbackFlow {
            offer(action)
            close()
        }
        else -> flowOf(action)
    }

    override fun reduce(previousState: Int, incomingMutation: Unit): Int = previousState + 1
}

private class StopwatchController : Controller<StopwatchController.Action, Int, Int> {

    sealed class Action {
        object Start : Action()
        object Stop : Action()
    }

    override val initialState: Int = 0

    override fun mutate(action: Action): Flow<Int> = when (action) {
        is Action.Start -> {
            ticker(1000).consumeAsFlow()
                // .takeUntil(this.action.filter { it is Action.Stop }) todo
                .map { 1 }
        }
        is Action.Stop -> emptyFlow()
    }

    override fun reduce(previousState: Int, incomingMutation: Int): Int =
        previousState + incomingMutation
}
