package at.florianschuster.control

import at.florianschuster.test.flow.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class ImplementationTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Before
    fun setup() {
        // uncomment for manual testing
        // ControllerLog.default = ControllerLog.Println
    }

    @Test
    fun `initial state only emitted once`() {
        val sut = testCoroutineScope.operationController()
        val testFlow = sut.state.testIn(testCoroutineScope)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `state is created when accessing current state`() {
        val sut = testCoroutineScope.operationController()

        assertEquals(listOf("initialState", "transformedState"), sut.currentState)
    }

    @Test
    fun `state is created when accessing action`() {
        val sut = testCoroutineScope.operationController()

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
        val sut = testCoroutineScope.operationController()
        val testFlow = sut.state.testIn(testCoroutineScope)

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
        val counterSut = testCoroutineScope.createSynchronousController<Int, Int>(
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
        val sut = testCoroutineScope.counterController() // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.testIn(testCoroutineScope)
        sut.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test(expected = ControllerError.Mutate::class)
    fun `state flow throws error from mutator`() = runBlockingTest {
        val sut = counterController(mutatorErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }

    @Test(expected = ControllerError.Reduce::class)
    fun `state flow throws error from reducer`() = runBlockingTest {
        val sut = counterController(reducerErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
    }

    @Test
    fun `cancel via takeUntil`() {
        val sut = testCoroutineScope.stopWatchController()

        sut.dispatch(StopWatchAction.Start)
        testCoroutineScope.advanceTimeBy(2000)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        testCoroutineScope.advanceTimeBy(3000)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        testCoroutineScope.advanceTimeBy(4000)
        sut.dispatch(StopWatchAction.Stop)

        // this should be ignored
        sut.dispatch(StopWatchAction.Start)
        testCoroutineScope.advanceTimeBy(500)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        testCoroutineScope.advanceTimeBy(1000)
        sut.dispatch(StopWatchAction.Stop)

        assert(sut.currentState == 10) // 2+3+4+1

        testCoroutineScope.advanceUntilIdle()
    }

    private fun CoroutineScope.operationController() =
        createController<List<String>, List<String>, List<String>>(

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
            reducer = { mutation, previousState -> previousState + mutation },

            // 6. ["initialState", "action", "transformedAction", "mutation", "transformedMutation"] + ["transformedState"]
            statesTransformer = { states -> states.map { it + "transformedState" } }
        )

    private fun CoroutineScope.counterController(
        mutatorErrorIndex: Int? = null,
        reducerErrorIndex: Int? = null
    ) = createController<Unit, Unit, Int>(
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
        reducer = { _, previousState ->
            if (previousState == reducerErrorIndex) error("test")
            previousState + 1
        }
    )

    private sealed class StopWatchAction {
        object Start : StopWatchAction()
        object Stop : StopWatchAction()
    }

    private fun CoroutineScope.stopWatchController() = createController<StopWatchAction, Int, Int>(
        initialState = 0,
        mutator = { action ->
            when (action) {
                is StopWatchAction.Start -> {
                    flow {
                        while (isActive) {
                            delay(1000)
                            emit(1)
                        }
                    }.takeUntil(actions.filterIsInstance<StopWatchAction.Stop>())
                }
                is StopWatchAction.Stop -> emptyFlow()
            }
        },
        reducer = { mutation, previousState -> previousState + mutation }
    )
}