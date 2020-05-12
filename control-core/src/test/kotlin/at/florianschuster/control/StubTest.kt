package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.junit.Rule
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class StubTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `custom controller implementation cannot be stubbed`() {
        val sut = object : Controller<Int, Int, Int> {
            override val initialState: Int get() = error("")
            override fun dispatch(action: Int) = Unit
            override val currentState: Int get() = error("")
            override val state: Flow<Int> get() = error("")
        }

        assertFailsWith<IllegalArgumentException> { sut.stub() }
    }

    @Test
    fun `stub is initialized only after accessing stub()`() {
        val sut =
            testCoroutineScope.createStringController() as ControllerImplementation<List<String>, List<String>, List<String>>
        assertFalse(sut.stubInitialized)

        assertFailsWith<UninitializedPropertyAccessException> { sut.stub.dispatchedActions }
        assertFalse(sut.stubInitialized)

        sut.stub().dispatchedActions
        assertTrue(sut.stubInitialized)
    }

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { stub() }

        expectedActions.forEach(sut::dispatch)

        assertEquals(expectedActions, sut.stub().dispatchedActions)
    }

    @Test
    fun `stub set state`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { stub() }
        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.stub()::emitState)

        testFlow expect emissions(listOf(initialState) + expectedStates)
    }

    @Test
    fun `stub state contains latest and following states`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController().apply { stub() }

        sut.stub().emitState(listOf("something 1"))
        sut.stub().emitState(listOf("something 2"))

        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.stub()::emitState)

        testFlow expect emissions(listOf(listOf("something 2")) + expectedStates)
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val sut = testCoroutineScope.createStringController().apply { stub() }

        sut.dispatch(listOf("test"))

        assertEquals(initialState, sut.currentState)
    }

    private fun CoroutineScope.createStringController() =
        createSynchronousController<List<String>, List<String>>(
            tag = "string_controller",
            initialState = initialState,
            reducer = { previousState, mutation -> previousState + mutation }
        )

    companion object {
        private val initialState = listOf("initialState")
    }
}