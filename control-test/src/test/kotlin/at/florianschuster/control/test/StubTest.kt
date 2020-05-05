package at.florianschuster.control.test

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class StubTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    @Test
    fun `stub actions are recorded correctly`() {
        val expectedActions = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = ControllerStub<List<String>, Unit, List<String>>(initialState)

        expectedActions.forEach(sut::dispatch)

        assertEquals(expectedActions, sut.dispatchedActions)
    }

    @Test
    fun `stub set state`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = ControllerStub<List<String>, Unit, List<String>>(initialState)
        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut::emitState)

        testFlow expect emissions(listOf(initialState) + expectedStates)
        assertEquals(listOf("three"), sut.currentState)
    }

    @Test
    fun `stub state contains latest and following states`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = ControllerStub<List<String>, Unit, List<String>>(initialState)

        sut.emitState(listOf("something 1"))
        sut.emitState(listOf("something 2"))

        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut::emitState)

        testFlow expect emissions(listOf(listOf("something 2")) + expectedStates)
    }

    companion object {
        private val initialState = listOf("initialState")
    }
}