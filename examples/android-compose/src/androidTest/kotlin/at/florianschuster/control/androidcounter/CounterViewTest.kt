package at.florianschuster.control.androidcounter

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import at.florianschuster.control.ControllerStub
import at.florianschuster.control.kotlincounter.CounterAction
import at.florianschuster.control.kotlincounter.CounterState
import at.florianschuster.control.kotlincounter.createCounterController
import at.florianschuster.control.toStub
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

internal class CounterViewTest {

    private lateinit var stub: ControllerStub<CounterAction, CounterState>

    @Before
    fun setup() {
        CounterView.ControllerFactory = { scope ->
            scope.createCounterController().toStub().also { stub = it }
        }
        launchFragmentInContainer<CounterView>()
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        onView(withId(R.id.increaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Increment, stub.dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        onView(withId(R.id.decreaseButton)).perform(click())

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
        onView(withId(R.id.valueTextView)).check(matches(withText("$testValue")))
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        stub.emitState(CounterState(loading = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))
    }
}