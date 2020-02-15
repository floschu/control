package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.regularCompletion
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
        val sut = OperationController(testScopeRule)
        val testFlow = sut.state.testIn(testScopeRule)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `state is created when accessing current state`() {
        val sut = OperationController(testScopeRule)

        assertEquals(listOf("initialState", "transformedState"), sut.currentState)
    }

    @Test
    fun `state is created when accessing action`() {
        val sut = OperationController(testScopeRule)

        sut.dispatch(listOf("action"))

        assertEquals(
            listOf(
                "initialState",
                "action",
                "transformedAction",
                "mutation",
                "transformedMutation",
                "transformedState"
            ),
            sut.currentState
        )
    }

    @Test
    fun `each method is invoked`() {
        val sut = OperationController(testScopeRule)
        val testFlow = sut.state.testIn(testScopeRule)

        sut.dispatch(listOf("action"))

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
    fun `collector receives latest and following states`() {
        val sut = CounterControllerDelegate(testScopeRule) // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.testIn(testScopeRule)
        sut.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test
    fun `stream ignores error from mutator`() {
        val sut = CounterControllerDelegate(testScopeRule, mutatorErrorIndex = 2)
        val testFlow = sut.state.testIn(testScopeRule)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)

        testFlow expect emissions(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun `anonymous controller is created correctly`() {
        val sut = Controller<Unit, Unit, Int>(
            scope = testScopeRule,
            initialState = 0,
            mutator = { flowOf(it) },
            reducer = { previousState, _ -> previousState + 1 }
        )
        val testFlow = sut.state.testIn(testScopeRule)

        sut.dispatch(Unit)
        sut.dispatch(Unit)

        testFlow expect emissions(0, 1, 2)
    }

    @Test
    fun `cancel controller cancels controller flow and channels`() {
        val sut = Controller<Unit, Unit, Int>(initialState = 3)
        val testFlow = sut.state.testIn(testScopeRule)

        assertFalse(sut.cancelled)
        sut.cancel()
        assertTrue(sut.cancelled)

        testFlow expect regularCompletion()
    }

    @Test
    fun `cancel scope cancels controller flow and channels`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val sut = Controller<Unit, Unit, Int>(
            initialState = 3,
            scope = scope
        )
        val testFlow = sut.state.testIn(testScopeRule)

        assertFalse(sut.cancelled)
        scope.cancel()
        assertTrue(sut.cancelled)

        testFlow expect regularCompletion()
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = OperationController(testScopeRule)
        sut.stubEnabled = true

        sut.dispatch(expectedActions[0])
        sut.dispatch(expectedActions[1])
        sut.dispatch(expectedActions[2])

        assertEquals(expectedActions, sut.stub.actions)
    }

    @Test
    fun `stub set state`() {
        val sut = OperationController(testScopeRule)
        sut.stubEnabled = true

        val testFlow = sut.state.testIn(testScopeRule)

        sut.stub.setState(listOf("state0"))
        sut.stub.setState(listOf("state1"))
        sut.stub.setState(listOf("state2"))

        assertEquals(listOf("state2"), sut.currentState)
        testFlow expect emissions(
            listOf("initialState"),
            listOf("state0"),
            listOf("state1"),
            listOf("state2")
        )
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val sut = OperationController(testScopeRule)
        sut.stubEnabled = true

        sut.dispatch(listOf("test"))

        assertEquals(listOf("initialState"), sut.currentState)
    }

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

    @Suppress("TestFunctionName")
    private fun CounterControllerDelegate(
        scope: CoroutineScope,
        mutatorErrorIndex: Int? = null
    ): ControllerDelegate<Unit, Int> = object : ControllerDelegate<Unit, Int> {

        override val controller = Controller<Unit, Unit, Int>(
            scope = scope,
            initialState = 0,
            mutator = { action ->
                when (currentState) {
                    mutatorErrorIndex -> flow {
                        emit(action)
                        error("test")
                    }
                    else -> flowOf(action)
                }
            },
            reducer = { previousState, _ -> previousState + 1 }
        )
    }
}