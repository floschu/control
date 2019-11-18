package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@FlowPreview
@ExperimentalCoroutinesApi
internal class ControllerTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `initial state only emitted once`() {
        val controller = OperationController(testScopeRule)
        val testCollector = controller.state.testIn(testScopeRule)

        testCollector expect emissionCount(1)
        testCollector expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `each method is invoked`() {
        val controller = OperationController(testScopeRule)
        val testCollector = controller.state.testIn(testScopeRule)

        controller.action(OperationController.Action)

        testCollector expect emissionCount(2)
        testCollector expect emissions(
            listOf("initialState", "transformedState"),
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            )
        )
    }

    @Test
    fun `current state`() {
        val controller = OperationController(testScopeRule)

        controller.action(OperationController.Action)

        assertEquals(
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            ),
            controller.currentState
        )
    }

    @Test
    fun `state is created when accessing action`() {
        val controller = OperationController(testScopeRule)

        controller.action(OperationController.Action)

        assertEquals(
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            ),
            controller.currentState
        )
    }

    @Test
    fun `collector receives latest and following states`() {
        val controller = CounterController(testScopeRule) // 0

        controller.action(Unit) // 1
        controller.action(Unit) // 2
        controller.action(Unit) // 3
        controller.action(Unit) // 4
        val testCollector = controller.state.testIn(testScopeRule)
        controller.action(Unit) // 5

        testCollector expect emissions(4, 5)
    }

    @Test
    fun `stream ignores error from mutate`() {
        val controller = CounterController(testScopeRule, mutateErrorIndex = 2)
        val testCollector = controller.state.testIn(testScopeRule)

        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)
        controller.action(Unit)

        testCollector expect emissions(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun `anonymous controller is created correctly`() {
        val controller = object : Controller<Unit, Unit, Int> {
            override var scope: CoroutineScope = testScopeRule
            override val initialState: Int = 0

            override fun mutate(action: Unit): Flow<Unit> = flowOf(action)
            override fun reduce(previousState: Int, mutation: Unit): Int = previousState + 1
        }
        val testCollector = controller.state.testIn(testScopeRule)

        controller.action(Unit)
        controller.action(Unit)

        testCollector expect emissions(0, 1, 2)
    }

    @Test
    fun `after controller cancel controller can be reused`() {
        val controller = object : Controller<Unit, Unit, Int> {
            override val initialState = 3
        }

        assertEquals(3, controller.currentState)
        controller.cancel()
        assertEquals(3, controller.currentState)
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val controller = OperationController(testScopeRule)
        controller.stubEnabled = true

        controller.action(expectedActions[0])
        controller.action(expectedActions[1])
        controller.action(expectedActions[2])

        assertEquals(expectedActions, controller.stub.actions)
    }

    @Test
    fun `stub set state`() {
        val controller = OperationController(testScopeRule)
        controller.stubEnabled = true

        val testFlow = controller.state.testIn(testScopeRule)

        controller.stub.setState(listOf("state0"))
        controller.stub.setState(listOf("state1"))
        controller.stub.setState(listOf("state2"))

        assertEquals(listOf("state2"), controller.currentState)
        testFlow expect emissions(
            listOf("initialState"),
            listOf("state0"),
            listOf("state1"),
            listOf("state2")
        )
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val controller = OperationController(testScopeRule)
        controller.stubEnabled = true

        controller.action(listOf("test"))

        assertEquals(listOf("initialState"), controller.currentState)
    }

    @Test
    fun `overwrite scope`() {
        val testScope = TestCoroutineScope()

        val controller = object : Controller<Unit, Unit, Unit> {
            override val initialState: Unit = Unit
        }

        assertNotEquals(testScope, controller.scope)

        controller.scope = testScope

        assertEquals(testScope, controller.scope)
    }

    // todo wait until official Flow.takeUntil implementation
    // @Test
    // fun `cancel producing flow in mutate`() = testScopeRule.runBlockingTest {
    //     val controller = StopwatchController(testScopeRule)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScopeRule.advanceTimeBy(2000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScopeRule.advanceTimeBy(3000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScopeRule.advanceTimeBy(4000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     // this should be ignored
    //     controller.action(StopwatchController.Action.Start)
    //     testScopeRule.advanceTimeBy(500)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     controller.action(StopwatchController.Action.Start)
    //     testScopeRule.advanceTimeBy(1000)
    //     controller.action(StopwatchController.Action.Stop)
    //
    //     assertEquals(10, controller.currentState) // 2+3+4+1
    // }

    private class OperationController(
        override var scope: CoroutineScope
    ) : Controller<List<String>, List<String>, List<String>> {

        // 1. ["initialState"]
        override val initialState: List<String> = listOf("initialState")

        // 2. ["action"] + ["transformedAction"]
        override fun transformAction(action: Flow<List<String>>): Flow<List<String>> {
            return action.map { it + "transformedAction" }
        }

        // 3. ["action", "transformedAction"] + ["mutation"]
        override fun mutate(action: List<String>): Flow<List<String>> {
            return flowOf(action + "mutation")
        }

        // 4. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
        override fun transformMutation(mutation: Flow<List<String>>): Flow<List<String>> {
            return mutation.map { it + "transformedMutation" }
        }

        // 5. ["initialState"] + ["action", "transformedAction", "mutation", "transformedMutation"]
        override fun reduce(previousState: List<String>, mutation: List<String>): List<String> {
            return previousState + mutation
        }

        // 6. ["initialState", "action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
        override fun transformState(state: Flow<List<String>>): Flow<List<String>> {
            return state.map { it + "transformedState" }
        }

        companion object {
            val Action: List<String> = listOf("action")
        }
    }

    private class CounterController(
        override var scope: CoroutineScope,
        val mutateErrorIndex: Int? = null
    ) : Controller<Unit, Unit, Int> {

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

    // todo wait until official Flow.takeUntil
    // private class StopwatchController(
    //     override var scope: CoroutineScope
    // ) : Controller<StopwatchController.Action, Int, Int> {
    //
    //     enum class Action { Start, Stop }
    //
    //     override val initialState: Int = 0
    //
    //     override fun mutate(action: Action): Flow<Int> = when (action) {
    //         Action.Start -> {
    //             flow {
    //                 while (true) {
    //                     delay(1000)
    //                     emit(1)
    //                 }
    //             }.takeUntil(this@StopwatchController.action.filter { it == Action.Stop })
    //         }
    //         Action.Stop -> emptyFlow()
    //     }
    //
    //     override fun reduce(previousState: Int, mutation: Int): Int = previousState + mutation
    // }
}