package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ImplementationTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `initial state only emitted once`() {
        val sut = testCoroutineScope.createOperationController()
        val testFlow = sut.state.testIn(testCoroutineScope)

        testFlow expect emissionCount(1)
        testFlow expect emission(0, listOf("initialState", "transformedState"))
    }

    @Test
    fun `state is created when accessing current state`() {
        val sut = testCoroutineScope.createOperationController()

        assertEquals(listOf("initialState", "transformedState"), sut.currentState)
    }

    @Test
    fun `state is created when accessing action`() {
        val sut = testCoroutineScope.createOperationController()

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
        val sut = testCoroutineScope.createOperationController()
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
    fun `only distinct states are emitted`() {
        val sut = testCoroutineScope.createAlwaysSameStateController()
        val testFlow = sut.state.testIn(testCoroutineScope)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        testFlow expect emissionCount(1) // no state changes
    }

    @Test
    fun `collector receives latest and following states`() {
        val sut = testCoroutineScope.createCounterController() // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.testIn(testCoroutineScope)
        sut.dispatch(Unit) // 5

        testFlow expect emissions(4, 5)
    }

    @Test
    fun `state flow throws error from mutator`() {
        val scope = TestCoroutineScope()
        val sut = scope.createCounterController(mutatorErrorIndex = 2)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)

        assertTrue(scope.uncaughtExceptions.first() is ControllerError.Mutate)
    }

    @Test
    fun `state flow throws error from reducer`() {
        val scope = TestCoroutineScope()
        val sut = scope.createCounterController(reducerErrorIndex = 2)

        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)

        assertTrue(scope.uncaughtExceptions.first() is ControllerError.Reduce)
    }

    @Test
    fun `cancel via takeUntil`() {
        val sut = testCoroutineScope.createStopWatchController()

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

        assertTrue(sut.currentState == 10) // 2+3+4+1

        testCoroutineScope.advanceUntilIdle()
    }

    @Test
    fun `global state gets merged into controller`() {
        val globalState = flow {
            delay(250)
            emit(42)
            delay(250)
            emit(42)
        }

        val sut = testCoroutineScope.createGlobalStateMergeController(globalState)

        val states = sut.state.testIn(testCoroutineScope)

        testCoroutineScope.advanceTimeBy(251)
        sut.dispatch(1)
        testCoroutineScope.advanceTimeBy(251)

        states expect emissions(0, 42, 43, 85)
    }

    @Test
    fun `MutatorScope is built correctly`() {
        val stateAccessor = { 1 }
        val actions = flowOf(1)
        val sut = ControllerImplementation.mutatorScope(stateAccessor, actions)

        assertEquals(stateAccessor(), sut.currentState)
        assertEquals(actions, sut.actions)
    }

    @Test
    fun `cancelling the implementation will return the last state`() {
        val sut = testCoroutineScope.createGlobalStateMergeController(emptyFlow())

        val states = sut.state.testIn(testCoroutineScope)

        sut.dispatch(0)
        sut.dispatch(1)

        assertEquals(1, sut.cancel())

        sut.dispatch(2)

        states expect lastEmission(1)
    }

    private fun CoroutineScope.createAlwaysSameStateController() =
        ControllerImplementation<Unit, Unit, Int>(
            scope = this,
            dispatcher = scopeDispatcher,
            controllerStart = ControllerStart.Lazy,
            initialState = 0,
            mutator = { flowOf(it) },
            reducer = { _, previousState -> previousState },
            actionsTransformer = { it },
            mutationsTransformer = { it },
            statesTransformer = { it },
            tag = "ImplementationTest.AlwaysSameStateController",
            controllerLog = ControllerLog.None
        )

    private fun CoroutineScope.createOperationController() =
        ControllerImplementation<List<String>, List<String>, List<String>>(
            scope = this,
            dispatcher = scopeDispatcher,
            controllerStart = ControllerStart.Lazy,

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
            statesTransformer = { states -> states.map { it + "transformedState" } },

            tag = "ImplementationTest.OperationController",
            controllerLog = ControllerLog.None
        )

    private fun CoroutineScope.createCounterController(
        mutatorErrorIndex: Int? = null,
        reducerErrorIndex: Int? = null
    ) = ControllerImplementation<Unit, Unit, Int>(
        scope = this,
        dispatcher = scopeDispatcher,
        controllerStart = ControllerStart.Lazy,
        initialState = 0,
        mutator = { action ->
            flow {
                check(currentState != mutatorErrorIndex)
                emit(action)
            }
        },
        reducer = { _, previousState ->
            check(previousState != reducerErrorIndex)
            previousState + 1
        },
        actionsTransformer = { it },
        mutationsTransformer = { it },
        statesTransformer = { it },
        tag = "ImplementationTest.CounterController",
        controllerLog = ControllerLog.None
    )

    private sealed class StopWatchAction {
        object Start : StopWatchAction()
        object Stop : StopWatchAction()
    }

    private fun CoroutineScope.createStopWatchController() =
        ControllerImplementation<StopWatchAction, Int, Int>(
            scope = this,
            dispatcher = scopeDispatcher,
            controllerStart = ControllerStart.Lazy,
            initialState = 0,
            mutator = { action ->
                when (action) {
                    is StopWatchAction.Start -> {
                        flow {
                            while (true) {
                                delay(1000)
                                emit(1)
                            }
                        }.takeUntil(actions.filterIsInstance<StopWatchAction.Stop>())
                    }
                    is StopWatchAction.Stop -> emptyFlow()
                }
            },
            reducer = { mutation, previousState -> previousState + mutation },
            actionsTransformer = { it },
            mutationsTransformer = { it },
            statesTransformer = { it },
            tag = "ImplementationTest.StopWatchController",
            controllerLog = ControllerLog.None
        )

    private fun CoroutineScope.createGlobalStateMergeController(
        globalState: Flow<Int>
    ) = ControllerImplementation<Int, Int, Int>(
        scope = this,
        dispatcher = scopeDispatcher,
        controllerStart = ControllerStart.Lazy,
        initialState = 0,
        mutator = { flowOf(it) },
        reducer = { action, previousState -> previousState + action },
        actionsTransformer = { merge(it, globalState) },
        mutationsTransformer = { it },
        statesTransformer = { it },
        tag = "ImplementationTest.GlobalStateMergeController",
        controllerLog = ControllerLog.None
    )
}