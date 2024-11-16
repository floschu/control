package at.florianschuster.control.counter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import at.florianschuster.control.ControllerStub
import at.florianschuster.control.toStub
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

internal class CounterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var scope: TestScope
    private lateinit var stub: ControllerStub<CounterAction, CounterState>

    @Before
    fun setup() {
        scope = TestScope()
        stub = scope.createCounterController().toStub()
        composeRule.setContent {
            CounterScreen(scope = scope, controller = stub)
        }
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        composeRule.onNodeWithContentDescription("increment")
            .performClick()

        // then
        assertEquals(CounterAction.Increment, stub.dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        composeRule.onNodeWithContentDescription("decrement")
            .performClick()

        // then
        assertEquals(CounterAction.Decrement, stub.dispatchedActions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 1

        // when
        stub.emitState(CounterState(value = testValue))

        // then
        composeRule.onNodeWithContentDescription("value")
            .assertTextEquals("Value: $testValue")
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        stub.emitState(CounterState(loading = true))

        // then
        composeRule.onNodeWithContentDescription("loading")
            .assertIsDisplayed()

        // when
        stub.emitState(CounterState(loading = false))

        // then
        composeRule.onNodeWithContentDescription("loading")
            .assertDoesNotExist()
    }
}