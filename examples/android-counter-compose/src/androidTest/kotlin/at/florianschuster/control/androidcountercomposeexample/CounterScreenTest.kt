package at.florianschuster.control.androidcountercomposeexample

import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.doClick
import androidx.ui.test.findByTag
import androidx.ui.test.findByText
import at.florianschuster.control.ControllerStub
import at.florianschuster.control.stub
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
internal class CounterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var stub: ControllerStub<CounterController.Action, CounterController.State>

    @Before
    fun setup() {
        val controller = CounterController()
        stub = controller.stub()
        composeTestRule.setContent { CounterScreen(controller) }
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        findByText("+").run {
            assertIsDisplayed()
            doClick()
        }

        // then
        assertEquals(CounterController.Action.Increment, stub.dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        findByText("-").run {
            assertIsDisplayed()
            doClick()
        }

        // then
        assertEquals(CounterController.Action.Decrement, stub.dispatchedActions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 42

        // when
        stub.emitState(CounterController.State(value = testValue))

        // then
        findByText("$testValue").assertIsDisplayed()
    }

    @Test
    fun whenStateOffersNotLoadingProgressBarDoesNotExist() {
        // when
        stub.emitState(CounterController.State(loading = false))

        // then
        findByTag("progressIndicator").assertDoesNotExist()
    }
}