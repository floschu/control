package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ImplementationTest : TestCoroutineScopeTest() {

    @Test
    fun `initial state only emitted once`() {
        val sut = testCoroutineScope.createOperationController()
        val testFlow = sut.state.test()

        testFlow.assertEmissionCount(1)
        testFlow.assertEmissionAt(0, listOf("initialState", "transformedState"))
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
        val testFlow = sut.state.test()

        sut.dispatch(listOf("action"))

        testFlow.assertEmissionCount(2)
        testFlow.assertEmissions(
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
        val testFlow = sut.state.test()
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        sut.dispatch(Unit)
        testFlow.assertEmissionCount(1) // no state changes
    }

    @Test
    fun `collector receives latest and following states`() {
        val sut = testCoroutineScope.createCounterController() // 0

        sut.dispatch(Unit) // 1
        sut.dispatch(Unit) // 2
        sut.dispatch(Unit) // 3
        sut.dispatch(Unit) // 4
        val testFlow = sut.state.test()
        sut.dispatch(Unit) // 5

        testFlow.assertEmissions(4, 5)
    }

    @Test
    fun `state flow throws error from mutator`() = suspendTest {
        assertFailsWith<ControllerError.Mutate> {
            val sut = createCounterController(mutatorErrorIndex = 2)
            sut.dispatch(Unit)
            sut.dispatch(Unit)
            sut.dispatch(Unit)
        }
    }

    @Test
    fun `state flow throws error from reducer`() = suspendTest {
        assertFailsWith<ControllerError.Reduce> {
            val sut = createCounterController(reducerErrorIndex = 2)
            sut.dispatch(Unit)
            sut.dispatch(Unit)
            sut.dispatch(Unit)
        }
    }

    @Test
    fun `cancel via takeUntil`() = suspendTest {
        val sut = createStopWatchController()

        sut.dispatch(StopWatchAction.Start)
        delay(2000)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        delay(3000)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        delay(4000)
        sut.dispatch(StopWatchAction.Stop)

        // this should be ignored
        sut.dispatch(StopWatchAction.Start)
        delay(500)
        sut.dispatch(StopWatchAction.Stop)

        sut.dispatch(StopWatchAction.Start)
        delay(1000)
        sut.dispatch(StopWatchAction.Stop)

        assertTrue(sut.currentState == 10) // 2+3+4+1
    }

    @Test
    fun `global state gets merged into controller`() = suspendTest {
        val globalState = flow {
            delay(250)
            emit(42)
            delay(250)
            emit(42)
        }

        val sut = createGlobalStateMergeController(globalState)

        val states = sut.state.testIn(this)

        delay(251)
        sut.dispatch(1)
        delay(251)

        states.assertEmissions(0, 42, 43, 85)
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

        val states = sut.state.test()

        sut.dispatch(0)
        sut.dispatch(1)

        sut.cancel()

        sut.dispatch(2)

        states.assertLastEmission(1)
    }

    @Test
    fun `effects are received from mutator, reducer and transformer`() {
        val sut = testCoroutineScope.createEffectTestController()
        val states = sut.state.test()
        val effects = sut.effects.test()

        val testEmissions = listOf(
            TestEffect.Reducer,
            TestEffect.ActionTransformer,
            TestEffect.MutationTransformer,
            TestEffect.Mutator,
            TestEffect.StateTransformer
        )

        testEmissions.map(TestEffect::ordinal).forEach(sut::dispatch)

        states.assertEmissions(listOf(0) + testEmissions.map(TestEffect::ordinal))
        effects.assertEmissions(testEmissions)
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
    fun `effects overflow throws error`() = suspendTest {
        val sut = createEffectTestController()

        repeat(ControllerImplementation.EFFECTS_CAPACITY) { sut.dispatch(1) }

        assertFailsWith<ControllerError.Effect> {
            sut.dispatch(1)

            // assertEquals(1, scope.uncaughtExceptions.size)
            // val error = scope.uncaughtExceptions.first()
            // assertEquals(ControllerError.Effect::class, assertNotNull(error.cause)::class)
        }
    }

    @Test
    fun `state is cancellable`() = suspendTest {
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
    fun `effects are cancellable`() = suspendTest {
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

    private sealed class StopWatchAction {
        object Start : StopWatchAction()
        object Stop : StopWatchAction()
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