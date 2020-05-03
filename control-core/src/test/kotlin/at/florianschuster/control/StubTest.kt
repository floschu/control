package at.florianschuster.control

import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.CoroutineScope
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
        val sut = testCoroutineScope.createStringController()
        sut.stubEnabled = true

        expectedActions.forEach(sut::dispatch)

        assertEquals(expectedActions, sut.stub.actions)
    }

    @Test
    fun `stub set state`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController()
        sut.stubEnabled = true
        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.stub::setState)

        testFlow expect emissions(listOf(initialState) + expectedStates)
    }

    @Test
    fun `stub state contains latest and following states`() {
        val expectedStates = listOf(
            listOf("one"),
            listOf("two"),
            listOf("three")
        )
        val sut = testCoroutineScope.createStringController()
        sut.stubEnabled = true

        sut.stub.setState(listOf("something 1"))
        sut.stub.setState(listOf("something 2"))

        val testFlow = sut.state.testIn(testCoroutineScope)

        expectedStates.forEach(sut.stub::setState)

        testFlow expect emissions(listOf(listOf("something 2")) + expectedStates)
    }

    @Test
    fun `stub action does not trigger state machine`() {
        val sut = testCoroutineScope.createStringController()
        sut.stubEnabled = true

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