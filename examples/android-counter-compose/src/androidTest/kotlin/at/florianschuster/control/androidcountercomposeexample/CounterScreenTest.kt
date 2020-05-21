package at.florianschuster.control.androidcountercomposeexample

import androidx.compose.getValue
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.createComposeRule
import androidx.ui.test.doClick
import androidx.ui.test.findByTag
import androidx.ui.test.findByText
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import at.florianschuster.control.stub
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
        controller = TestCoroutineScope().createCounterController().apply { stub() }
        composeTestRule.setContent {
            val state by controller.collectState()
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
        assertEquals(CounterAction.Increment, controller.stub().dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        findByText("-").run {
            assertIsDisplayed()
            doClick()
        }

        // then
        assertEquals(CounterAction.Decrement, controller.stub().dispatchedActions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 42

        // when
        controller.stub().emitState(CounterState(value = testValue))

        // then
        findByText("$testValue").assertIsDisplayed()
    }

    @Test
    fun whenStateOffersNotLoadingProgressBarDoesNotExist() {
        // when
        controller.stub().emitState(CounterState(loading = false))

        // then
        findByTag("progressIndicator").assertDoesNotExist()
    }
}