package at.florianschuster.control.counterexample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class CounterViewTest {

    @get:Rule
    val activityRule = activityScenarioRule<TestActivity>()

    private lateinit var controller: CounterController

    @Before
    fun setup() {
         controller = CounterController(TestCoroutineScope()).apply { store.stubEnabled = true }

        CounterView.CounterControllerProvider = { controller }

        activityRule.scenario.onActivity { it.setFragment(CounterView()) }
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        onView(withId(R.id.increaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Increment, controller.store.stub.actions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        onView(withId(R.id.decreaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Decrement, controller.store.stub.actions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 1

        // when
        controller.store.stub.setState(CounterState(value = testValue))

        // then
        onView(withId(R.id.valueTextView)).check(matches(withText("$testValue")))
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        controller.store.stub.setState(CounterState(loading = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))
    }
}