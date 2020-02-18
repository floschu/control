package at.florianschuster.control

import at.florianschuster.control.store.Store
import at.florianschuster.control.store.StoreError
import at.florianschuster.control.store.createStore
import at.florianschuster.control.store.createSynchronousStore
import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class StoreTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `initial state only emitted once`() {
        val sut = OperationStore(testScopeRule)
        val testFlow = sut.state.testIn(testScopeRule)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `state is created when accessing current state`() {
        val sut = OperationStore(testScopeRule)

        assertEquals(listOf("initialState", "transformedState"), sut.currentState)
    }

    @Test
    fun `state is created when accessing action`() {
        val sut = OperationStore(testScopeRule)

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
        val sut = OperationStore(testScopeRule)
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
    fun `synchronous store builder`() {
        val counterSut = testScopeRule.createSynchronousStore<Int, Int>(
            tag = "sync store",
            initialState = 0,
            reducer = { previousState, mutation -> previousState + mutation }
        )

        counterSut.dispatch(1)
        counterSut.dispatch(2)
        counterSut.dispatch(3)

        assertEquals(6, counterSut.currentState)
    }

    @Test
    fun `collector receives latest and following states`() {
        val sut = CounterController(testScopeRule) // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.testIn(testScopeRule)
        sut.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test(expected = StoreError.Mutator::class)
    fun `store throws error from mutator`() = runBlockingTest {
        val sut = CounterController(this, mutatorErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }

    @Test(expected = StoreError.Reducer::class)
    fun `store throws error from reducer`() = runBlockingTest {
        val sut = CounterController(this, reducerErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = OperationStore(testScopeRule)
        sut.stubEnabled = true

        sut.dispatch(expectedActions[0])
        sut.dispatch(expectedActions[1])
        sut.dispatch(expectedActions[2])

        assertEquals(expectedActions, sut.stub.actions)
    }

    @Test
    fun `stub set state`() {
        val sut = OperationStore(testScopeRule)
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
        val sut = OperationStore(testScopeRule)
        sut.stubEnabled = true

        sut.dispatch(listOf("test"))

        assertEquals(listOf("initialState"), sut.currentState)
    }

    @Suppress("TestFunctionName")
    private fun OperationStore(
        scope: CoroutineScope
    ): Store<List<String>, List<String>, List<String>> = scope.createStore(
        tag = "OperationStore",

        // 1. ["initialState"]
        initialState = listOf("initialState"),

        // 2. ["action"] + ["transformedAction"]
        actionsTransformer = { actions ->
            actions.map { it + "transformedAction" }
        },

        // 3. ["action", "transformedAction"] + ["mutation"]
        mutator = { action ->
            flowOf(action + "mutation")
        },

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
    private fun CounterController(
        scope: CoroutineScope,
        mutatorErrorIndex: Int? = null,
        reducerErrorIndex: Int? = null
    ): Controller<Unit, Int> = object : Controller<Unit, Int> {

        override val store: Store<Unit, Unit, Int> = scope.createStore(
            tag = "CounterController",
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
            reducer = { previousState, _ ->
                if (previousState == reducerErrorIndex) error("test")
                previousState + 1
            }
        )
    }
}