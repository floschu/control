package at.florianschuster.control.androidcounter

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterController
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import at.florianschuster.control.stub
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class CounterViewTest {

    private lateinit var controller: CounterController

    @Before
    fun setup() {
        CounterView.CounterControllerProvider = { scope ->
            controller = scope.createCounterController().apply { stub() }
            controller
        }
        launchFragmentInContainer<CounterView>()
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        onView(withId(R.id.increaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Increment, controller.stub().dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        onView(withId(R.id.decreaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Decrement, controller.stub().dispatchedActions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 1

        // when
        controller.stub().emitState(CounterState(value = testValue))

        // then
        onView(withId(R.id.valueTextView)).check(matches(withText("$testValue")))
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        controller.stub().emitState(CounterState(loading = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))
    }
}