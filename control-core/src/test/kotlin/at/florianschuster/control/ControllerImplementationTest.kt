package at.florianschuster.control

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

internal class ControllerImplementationTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `initial state only emitted once`() {
        val sut = testScopeRule.createOperationController()
        val testFlow = sut.state.testIn(testScopeRule)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `state is created when accessing current state`() {
        val sut = testScopeRule.createOperationController()

        assertEquals(listOf("initialState", "transformedState"), sut.currentState)
    }

    @Test
    fun `state is created when accessing action`() {
        val sut = testScopeRule.createOperationController()

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
        val sut = testScopeRule.createOperationController()
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
    fun `synchronous controller builder`() {
        val counterSut = testScopeRule.createSynchronousController<Int, Int>(
            tag = "counter",
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
        val sut = testScopeRule.createCounterController() // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.testIn(testScopeRule)
        sut.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test(expected = Controller.Error.Mutator::class)
    fun `state flow throws error from mutator`() = runBlockingTest {
        val sut = createCounterController(mutatorErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }

    @Test(expected = Controller.Error.Reducer::class)
    fun `state flow throws error from reducer`() = runBlockingTest {
        val sut = createCounterController(reducerErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }
}

private fun CoroutineScope.createOperationController() =
    createController<List<String>, List<String>, List<String>>(

        // 1. ["initialState"]
        initialState = listOf("initialState"),

        // 2. ["action"] + ["transformedAction"]
        actionsTransformer = { actions ->
            actions.map { it + "transformedAction" }
        },

        // 3. ["action", "transformedAction"] + ["mutation"]
        mutator = { action, _ ->
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

private fun CoroutineScope.createCounterController(
    mutatorErrorIndex: Int? = null,
    reducerErrorIndex: Int? = null
) = createController<Unit, Unit, Int>(
    initialState = 0,
    mutator = { action, stateAccessor ->
        when (stateAccessor()) {
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