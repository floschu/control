package at.florianschuster.control

import at.florianschuster.control.configuration.configureControl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import util.TestCoroutineScopeRule
import util.test
import kotlin.test.assertEquals

class ControllerTest {

    @get:Rule
    val testScope = TestCoroutineScopeRule()

    @Before
    fun setup() {
        configureControl {
            // todo remove
            errors { println("Error: $it") }
            operations(logger = ::println)
        }
    }

    @Test
    fun `initial state only emitted once`() {
        val controller = OperationController()
        val testCollector = controller.state.test(testScope)

        with(testCollector) {
            assertValuesCount(1)
            assertValue(0, listOf("initialState"))
        }
    }

    @Test
    fun `each method is invoked`() {
        val controller = OperationController()
        val testCollector = controller.state.test(testScope)

        controller.action(OperationController.Action)

        with(testCollector) {
            assertValuesCount(2)
            assertValue(0, listOf("initialState"))
            assertValue(
                1,
                listOf(
                    "initialState",
                    "action",
                    "transformedAction",
                    "mutation",
                    "transformedMutation"
                )
            )
        }
    }

    @Test
    fun `current state`() {
        val controller = OperationController()
        val ignored = controller.state.test(testScope)

        controller.action(OperationController.Action)

        assertEquals(
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation"
            ),
            controller.currentState
        )
    }

    @Test
    fun `state is created when accessing action`() {
        val controller = OperationController()

        controller.action(OperationController.Action)

        assertEquals(
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation"
            ),
            controller.currentState
        )
    }

    @Test
    fun `stream ignores error from mutate`() {
        val controller = CounterController(mutateErrorIndex = 2)
        val testCollector = controller.state.test(testScope)

        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)

        testCollector.assertValues(listOf(0, 1, 2, 3, 4, 5))
    }

    // @Test
    // fun `cancel hot flow in mutate`() { todo
    //     val controller = StopwatchController()
    //     val testCollector = controller.state.test(testScope)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScope.advanceTimeBy(2000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScope.advanceTimeBy(3000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScope.advanceTimeBy(4000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     // this should be ignored
    //     controller.action(StopwatchController.Action.Start)
    //     testScope.advanceTimeBy(500)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScope.advanceTimeBy(1000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     assertEquals(10, controller.currentState) // 2+3+4+1
    //     testCollector.assertNoErrors()
    // }

    private class OperationController : Controller<List<String>, List<String>, List<String>> {

        override val scope: CoroutineScope = TestCoroutineScope()

        // 1. ["initialState"]
        override val initialState: List<String> = listOf("initialState")

        // 2. ["action"] + ["transformedAction"]
        override fun transformAction(action: Flow<List<String>>): Flow<List<String>> {
            return action.map { it + "transformedAction" }
        }

        // 3. ["action", "transformedAction"] + ["mutation"]
        override fun mutate(incomingAction: List<String>): Flow<List<String>> {
            return flowOf(incomingAction + "mutation")
        }

        // 4. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
        override fun transformMutation(mutation: Flow<List<String>>): Flow<List<String>> {
            return mutation.map { it + "transformedMutation" }
        }

        // 5. ["initialState"] + ["action", "transformedAction", "mutation", "transformedMutation"]
        override fun reduce(previousState: List<String>, mutation: List<String>): List<String> {
            return previousState + mutation
        }

        companion object {
            val Action: List<String> = listOf("action")
        }
    }

    private class CounterController(
        val mutateErrorIndex: Int? = null
    ) : Controller<Unit, Unit, Int> {

        override val scope: CoroutineScope = TestCoroutineScope()
        override val initialState: Int = 0

        override fun mutate(action: Unit): Flow<Unit> = when (currentState) {
            mutateErrorIndex -> flow {
                emit(action)
                throw CancellationException()
            }
            else -> flowOf(action)
        }

        override fun reduce(previousState: Int, mutation: Unit): Int = previousState + 1
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
                    // .takeWhile { this@StopwatchController.action.first() !is Action.Stop }
                    .map { 1 }
            }
            is Action.Stop -> emptyFlow()
        }

        override fun reduce(previousState: Int, mutation: Int): Int =
            previousState + mutation
    }
}