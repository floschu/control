package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emission
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
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

        val sut2 = testCoroutineScope.createOperationController()
        assertEquals(listOf("initialState", "transformedState"), sut2.state.value)
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
            sut.state.value
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

        assertTrue(sut.state.value == 10) // 2+3+4+1

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
    fun `MutatorContext is built correctly`() {
        val stateAccessor = { 1 }
        val actions = flowOf(1)
        var emittedEffect: Int? = null
        val sut = ControllerImplementation.createMutatorContext<Int, Int, Int>(
            stateAccessor,
            actions
        ) { emittedEffect = it }

        sut.emitEffect(1)

        assertEquals(stateAccessor(), sut.currentState)
        assertEquals(actions, sut.actions)
        assertEquals(1, emittedEffect)
    }

    @Test
    fun `ReducerContext is built correctly`() {
        var emittedEffect: Int? = null
        val sut = ControllerImplementation.createReducerContext<Int> { emittedEffect = it }
        sut.emitEffect(2)
        assertEquals(2, emittedEffect)
    }

    @Test
    fun `TransformerContext is built correctly`() {
        var emittedEffect: Int? = null
        val sut = ControllerImplementation.createTransformerContext<Int> { emittedEffect = it }
        sut.emitEffect(3)
        assertEquals(3, emittedEffect)
    }

    @Test
    fun `cancelling the implementation will return the last state`() {
        val sut = testCoroutineScope.createGlobalStateMergeController(emptyFlow())

        val states = sut.state.testIn(testCoroutineScope)

        sut.dispatch(0)
        sut.dispatch(1)

        sut.cancel()

        sut.dispatch(2)

        states expect lastEmission(1)
    }

    @Test
    fun `effects are received from mutator, reducer and transformer`() {
        val sut = testCoroutineScope.createEffectTestController()
        val states = sut.state.testIn(testCoroutineScope)
        val effects = sut.effects.testIn(testCoroutineScope)

        val testEmissions = listOf(
            TestEffect.Reducer,
            TestEffect.ActionTransformer,
            TestEffect.MutationTransformer,
            TestEffect.Mutator,
            TestEffect.StateTransformer
        )

        testEmissions.map(TestEffect::ordinal).forEach(sut::dispatch)

        states expect emissions(listOf(0) + testEmissions.map(TestEffect::ordinal))
        effects expect emissions(testEmissions)
    }

    @Test
    fun `effects are only received once collector`() {
        val sut = testCoroutineScope.createEffectTestController()
        val effects = mutableListOf<TestEffect>()
        sut.effects.onEach { effects.add(it) }.launchIn(testCoroutineScope)
        sut.effects.onEach { effects.add(it) }.launchIn(testCoroutineScope)

        val testEmissions = listOf(
            TestEffect.Reducer,
            TestEffect.ActionTransformer,
            TestEffect.MutationTransformer,
            TestEffect.Reducer,
            TestEffect.Mutator,
            TestEffect.StateTransformer,
            TestEffect.Reducer
        )

        testEmissions.map(TestEffect::ordinal).forEach(sut::dispatch)

        assertEquals(testEmissions, effects)
    }

    @Test
    fun `effects overflow throws error`() {
        val scope = TestCoroutineScope()
        val sut = scope.createEffectTestController()

        repeat(ControllerImplementation.CAPACITY) { sut.dispatch(1) }
        assertTrue(scope.uncaughtExceptions.isEmpty())

        sut.dispatch(1)

        assertEquals(1, scope.uncaughtExceptions.size)
        val error = scope.uncaughtExceptions.first()
        assertEquals(ControllerError.Effect::class, assertNotNull(error.cause)::class)
    }

    @Test
    fun `state is cancellable`() = runBlockingTest {
        val sut = createCounterController()

        sut.dispatch(Unit)

        var state: Int? = null
        launch {
            cancel()
            state = -1
            state = sut.state.first() // this should be cancelled and thus not return a value
        }

        assertEquals(-1, state)
        sut.cancel()
    }

    @Test
    fun `effects are cancellable`() = runBlockingTest {
        val sut = createEffectTestController()

        sut.dispatch(TestEffect.Mutator.ordinal)

        var effect: TestEffect? = null
        launch {
            cancel()
            effect = TestEffect.Reducer
            effect = sut.effects.first() // this should be cancelled and thus not return a value
        }

        assertEquals(TestEffect.Reducer, effect)
        sut.cancel()
    }

    @Test
    fun `controller is started lazily when only effects field is accessed`() {
        val sut = ControllerImplementation<Int, Int, Int, String>(
            scope = testCoroutineScope,
            dispatcher = testCoroutineScope.defaultScopeDispatcher(),
            controllerStart = ControllerStart.Lazy,
            initialState = 0,
            mutator = { action -> flowOf(action) },
            reducer = { mutation, _ -> mutation },
            actionsTransformer = { actions ->
                merge(actions, flow {
                    emitEffect("actionsTransformer started")
                })
            },
            mutationsTransformer = { mutations -> mutations },
            statesTransformer = { states -> states },
            tag = "ImplementationTest.EffectController",
            controllerLog = ControllerLog.None
        )

        val effects = sut.effects.testIn(testCoroutineScope)

        effects expect emissions(listOf("actionsTransformer started"))
    }

    private fun CoroutineScope.createAlwaysSameStateController() =
        ControllerImplementation<Unit, Unit, Int, Nothing>(
            scope = this,
            dispatcher = defaultScopeDispatcher(),
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
        ControllerImplementation<List<String>, List<String>, List<String>, Nothing>(
            scope = this,
            dispatcher = defaultScopeDispatcher(),
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
    ) = ControllerImplementation<Unit, Unit, Int, Nothing>(
        scope = this,
        dispatcher = defaultScopeDispatcher(),
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

    private sealed interface StopWatchAction {
        object Start : StopWatchAction
        object Stop : StopWatchAction
    }

    private fun CoroutineScope.createStopWatchController() =
        ControllerImplementation<StopWatchAction, Int, Int, Nothing>(
            scope = this,
            dispatcher = defaultScopeDispatcher(),
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
    ) = ControllerImplementation<Int, Int, Int, Nothing>(
        scope = this,
        dispatcher = defaultScopeDispatcher(),
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

    enum class TestEffect {
        Mutator, Reducer, ActionTransformer, MutationTransformer, StateTransformer
    }

    private fun CoroutineScope.createEffectTestController() =
        ControllerImplementation<Int, Int, Int, TestEffect>(
            scope = this,
            dispatcher = defaultScopeDispatcher(),
            controllerStart = ControllerStart.Lazy,
            initialState = 0,
            mutator = { action ->
                if (action == TestEffect.Mutator.ordinal) emitEffect(TestEffect.Mutator)
                flowOf(action)
            },
            reducer = { mutation, _ ->
                if (mutation == TestEffect.Reducer.ordinal) emitEffect(TestEffect.Reducer)
                mutation
            },
            actionsTransformer = { actions ->
                actions.onEach {
                    if (it == TestEffect.ActionTransformer.ordinal) {
                        emitEffect(TestEffect.ActionTransformer)
                    }
                }
            },
            mutationsTransformer = { mutations ->
                mutations.onEach {
                    if (it == TestEffect.MutationTransformer.ordinal) {
                        emitEffect(TestEffect.MutationTransformer)
                    }
                }
            },
            statesTransformer = { states ->
                states.onEach {
                    if (it == TestEffect.StateTransformer.ordinal) {
                        emitEffect(TestEffect.StateTransformer)
                    }
                }
            },
            tag = "ImplementationTest.EffectController",
            controllerLog = ControllerLog.None
        )
}