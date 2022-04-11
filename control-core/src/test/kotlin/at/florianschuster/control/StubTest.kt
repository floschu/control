package at.florianschuster.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
internal class StubTest {

    @Test
    fun `custom controller implementation cannot be stubbed`() {
        val sut = object : Controller<Int, Int> {
            override fun dispatch(action: Int) = Unit
            override val state: StateFlow<Int> get() = error("")
        }

        assertFailsWith<IllegalArgumentException> { sut.toStub() }
    }

    @Test
    fun `custom EffectController implementation cannot be stubbed`() {
        val sut = object : EffectController<Int, Int, Int> {
            override fun dispatch(action: Int) = Unit
            override val state: StateFlow<Int> get() = error("")
            override val effects: Flow<Int> get() = error("")
        }

        assertFailsWith<IllegalArgumentException> { sut.toStub() }
    }

    @Test
    fun `Controller stub is enabled only after conversion()`() {
        val scope = TestScope()
        val sut = scope.createStringController()
        assertFalse(sut.stubEnabled)

        (sut as Controller<List<String>, List<String>>).toStub()
        assertTrue(sut.stubEnabled)

        scope.cancel()
    }

    @Test
    fun `EffectController stub is enabled only after conversion()`() {
        val scope = TestScope()
        val sut = scope.createStringController()

        assertFalse(sut.stubEnabled)
        (sut as EffectController<List<String>, List<String>, String>).toStub()
        assertTrue(sut.stubEnabled)

        scope.cancel()
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = scope.createStringController().apply { toStub() }

        expectedActions.forEach(sut::dispatch)

        assertEquals(expectedActions, sut.dispatchedActions)

        scope.cancel()
    }

    @Test
    fun `stub set state`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = scope.createStringController().apply { toStub() }
        val states = mutableListOf<List<String>>()
        scope.launch { sut.state.toList(states) }

        expectedStates.forEach(sut::emitState)

        assertEquals(
            listOf(initialState) + expectedStates,
            states
        )

        scope.cancel()
    }

    @Test
    fun `stub state contains latest and following states`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = scope.createStringController().apply { toStub() }

        sut.toStub().emitState(listOf("something 1"))
        sut.toStub().emitState(listOf("something 2"))

        val states = mutableListOf<List<String>>()
        scope.launch { sut.state.toList(states) }

        expectedStates.forEach(sut.toStub()::emitState)

        assertEquals(listOf(listOf("something 2")) + expectedStates, states)

        scope.cancel()
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val sut = scope.createStringController().apply { toStub() }

        sut.dispatch(listOf("test"))

        assertEquals(initialState, sut.state.value)

        scope.cancel()
    }

    @Test
    fun `stub emits effects`() {
        val scope = TestScope(UnconfinedTestDispatcher())
        val sut = scope.createStringController().apply { toStub() }
        val effects = mutableListOf<String>()
        scope.launch { sut.effects.toList(effects) }

        sut.emitEffect("effect1")
        sut.emitEffect("effect2")

        assertEquals(
            listOf("effect1", "effect2"),
            effects
        )

        scope.cancel()
    }

    private fun CoroutineScope.createStringController() =
        ControllerImplementation<List<String>, List<String>, List<String>, String>(
            scope = this,
            dispatcher = defaultScopeDispatcher(),
            controllerStart = ControllerStart.Lazy,
            initialState = initialState,
            mutator = { flowOf(it) },
            reducer = { previousState, mutation -> previousState + mutation },
            actionsTransformer = { it },
            mutationsTransformer = { it },
            statesTransformer = { it },
            tag = "StubTest.StringController",
            controllerLog = ControllerLog.None
        )

    companion object {
        private val initialState = listOf("initialState")
    }
}