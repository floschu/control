package at.florianschuster.control.countercomposeexample

import androidx.compose.getValue
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.doClick
import androidx.ui.test.findByTag
import androidx.ui.test.findByText
import kotlinx.coroutines.test.TestCoroutineScope
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

    private lateinit var controller: CounterController

    @Before
    fun setup() {
        controller = CounterController(TestCoroutineScope()).apply { stubEnabled = true }
        composeTestRule.setContent {
            val state by controller.collectAsState()
            CounterScreen(counterState = state, action = controller::dispatch)
        }
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        findByText("+").run {
            assertIsDisplayed()
            doClick()
        }

        // then
        assertEquals(CounterAction.Increment, controller.stub.actions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        findByText("-").run {
            assertIsDisplayed()
            doClick()
        }

        // then
        assertEquals(CounterAction.Decrement, controller.stub.actions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 42

        // when
        controller.stub.setState(CounterState(value = testValue))

        // then
        findByText("$testValue").assertIsDisplayed()
    }

    @Test
    fun whenStateOffersNotLoadingProgressBarDoesNotExist() {
        // when
        controller.stub.setState(CounterState(loading = false))

        // then
        findByTag("progressIndicator").assertDoesNotExist()
    }
}