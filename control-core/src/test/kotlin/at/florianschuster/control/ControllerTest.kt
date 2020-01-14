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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
internal class ControllerTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `initial state only emitted once`() {
        val controller = OperationController(testScopeRule)
        val testFlow = controller.state.testIn(testScopeRule)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `each method is invoked`() {
        val controller = OperationController(testScopeRule)
        val testFlow = controller.state.testIn(testScopeRule)

        controller.dispatch(listOf("action"))

        testFlow expect emissionCount(2)
        testFlow expect emissions(
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

        controller.dispatch(listOf("action"))

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
        val controller = CounterProxy(testScopeRule) // 0

        controller.dispatch(Unit) // 1
        controller.dispatch(Unit) // 2
        controller.dispatch(Unit) // 3
        controller.dispatch(Unit) // 4
        val testFlow = controller.state.testIn(testScopeRule)
        controller.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test
    fun `stream ignores error from mutate`() {
        val controller = CounterProxy(testScopeRule, mutateErrorIndex = 2)
        val testFlow = controller.state.testIn(testScopeRule)

        controller.dispatch(Unit)
        controller.dispatch(Unit)
        controller.dispatch(Unit)
        controller.dispatch(Unit)
        controller.dispatch(Unit)

        testFlow expect emissions(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun `anonymous controller is created correctly`() {
        val controller = Controller<Unit, Unit, Int>(
            scope = testScopeRule,
            initialState = 0,
            mutator = { flowOf(it) },
            reducer = { previousState, _ -> previousState + 1 }
        )
        val testFlow = controller.state.testIn(testScopeRule)

        controller.dispatch(Unit)
        controller.dispatch(Unit)

        testFlow expect emissions(0, 1, 2)
    }

    @Test
    fun `controller cancel`() {
        val controller = Controller<Unit, Unit, Int>(initialState = 3)

        assertFalse(controller.stateJob.isCancelled)
        controller.cancel()
        assertTrue(controller.stateJob.isCancelled)
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

        controller.dispatch(expectedActions[0])
        controller.dispatch(expectedActions[1])
        controller.dispatch(expectedActions[2])

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

        controller.dispatch(listOf("test"))

        assertEquals(listOf("initialState"), controller.currentState)
    }

    // todo wait for Flow.takeUntil
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

    @Suppress("TestFunctionName")
    private fun OperationController(
        scope: CoroutineScope
    ): Controller<List<String>, List<String>, List<String>> = Controller(
        scope = scope,

        // 1. ["initialState"]
        initialState = listOf("initialState"),

        // 2. ["action"] + ["transformedAction"]
        actionsTransformer = { actions ->
            actions.map { it + "transformedAction" }
        },

        // 3. ["action", "transformedAction"] + ["mutation"]
        mutator = { flowOf(it + "mutation") },

        // 4. ["action", "transformedAction", "mutation"] + ["transformedMutation"]
        mutationsTransformer = { mutations ->
            mutations.map { it + "transformedMutation" }
        },

        // 5. ["initialState"] + ["action", "transformedAction", "mutation", "transformedMutation"]
        reducer = { previousState, mutation -> previousState + mutation },

        // 6. ["initialState", "action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
        statesTransformer = { states -> states.map { it + "transformedState" } }
    )

    private fun CounterProxy(
        scope: CoroutineScope,
        mutateErrorIndex: Int? = null
    ): Proxy<Unit, Int> = object : Proxy<Unit, Int> {

        override val controller = Controller<Unit, Unit, Int>(
            scope = scope,
            initialState = 0,
            mutator = { action ->
                when (currentState) {
                    mutateErrorIndex -> flow {
                        emit(action)
                        throw CancellationException()
                    }
                    else -> flowOf(action)
                }
            },
            reducer = { previousState, _ -> previousState + 1 }
        )
    }

    // class StopwatchProxy(
    //     scope: CoroutineScope
    // ) : Proxy<StopwatchProxy.Action, Int> {
    //
    //     enum class Action { Start, Stop }
    //
    //     override val controller = Controller<Action, Int, Int>(
    //
    //         initialState = 0,
    //         scope = scope,
    //         mutator = { action ->
    //             when (action) {
    //                 Action.Start -> {
    //                     flow {
    //                         while (true) {
    //                             delay(1000)
    //                             emit(1)
    //                         }
    //                     }.takeUntil(this@StopwatchController.action.filter { it == Action.Stop })
    //                 }
    //                 Action.Stop -> emptyFlow()
    //             }
    //         },
    //         reducer = { previousState, mutation -> previousState + mutation }
    //     )
    // }
}