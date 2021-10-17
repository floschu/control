package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
internal class StubTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `custom controller implementation cannot be stubbed`() {
        val sut = object : Controller<Int, Int> {
            override fun dispatch(action: Int) = Unit
            override val currentState: Int get() = error("")
            override val state: StateFlow<Int> get() = error("")
        }

        assertFailsWith<IllegalArgumentException> { sut.toStub() }
    }

    @Test
    fun `custom EffectController implementation cannot be stubbed`() {
        val sut = object : EffectController<Int, Int, Int> {
            override fun dispatch(action: Int) = Unit
            override val currentState: Int get() = error("")
            override val state: StateFlow<Int> get() = error("")
            override val effects: Flow<Int> get() = error("")
        }

        assertFailsWith<IllegalArgumentException> { sut.toStub() }
    }

    @Test
    fun `Controller stub is enabled only after conversion()`() {
        val sut = testCoroutineScope.createStringController()
        assertFalse(sut.stubEnabled)

        (sut as Controller<List<String>, List<String>>).toStub()
        assertTrue(sut.stubEnabled)
    }

    @Test
    fun `EffectController stub is enabled only after conversion()`() {
        val sut = testCoroutineScope.createStringController()
        assertFalse(sut.stubEnabled)

        (sut as EffectController<List<String>, List<String>, String>).toStub()
        assertTrue(sut.stubEnabled)
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { toStub() }

        expectedActions.forEach(sut::dispatch)

        assertEquals(expectedActions, sut.toStub().dispatchedActions)
    }

    @Test
    fun `stub set state`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { toStub() }
        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.toStub()::emitState)

        testFlow expect emissions(listOf(initialState) + expectedStates)
    }

    @Test
    fun `stub state contains latest and following states`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { toStub() }

        sut.toStub().emitState(listOf("something 1"))
        sut.toStub().emitState(listOf("something 2"))

        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.toStub()::emitState)

        testFlow expect emissions(listOf(listOf("something 2")) + expectedStates)
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val sut = testCoroutineScope.createStringController().apply { toStub() }

        sut.dispatch(listOf("test"))

        assertEquals(initialState, sut.currentState)
    }

    @Test
    fun `stub emits effects`() {
        val sut = testCoroutineScope.createStringController().apply { toStub() }
        val testFlow = sut.effects.testIn(testCoroutineScope)

        sut.emitEffect("effect1")
        sut.emitEffect("effect2")

        testFlow expect emissions("effect1", "effect2")
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