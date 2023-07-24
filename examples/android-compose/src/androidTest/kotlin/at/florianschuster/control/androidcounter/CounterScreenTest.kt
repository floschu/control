package at.florianschuster.control.androidcounter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import at.florianschuster.control.ControllerStub
import at.florianschuster.control.androidcompose.CounterScreen
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import at.florianschuster.control.toStub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@FlowPreview
@ExperimentalCoroutinesApi
internal class CounterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var stub: ControllerStub<CounterAction, CounterState>

    @Before
    fun setup() {
        val scope = TestScope()
        val controller = scope.createCounterController().toStub()
        composeTestRule.setContent {
            CounterScreen(scope = scope, controller = controller)
        }
        stub = controller
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        composeTestRule.onNodeWithContentDescription("increment")
            .performClick()

        // then
        assertEquals(CounterAction.Increment, stub.dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        composeTestRule.onNodeWithContentDescription("decrement")
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
        composeTestRule.onNodeWithContentDescription("value")
            .assertTextEquals("Value: $testValue")
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        stub.emitState(CounterState(loading = true))

        // then
        composeTestRule.onNodeWithContentDescription("loading")
            .assertIsDisplayed()

        // when
        stub.emitState(CounterState(loading = false))

        // then
        composeTestRule.onNodeWithContentDescription("loading")
            .assertDoesNotExist()
    }
}