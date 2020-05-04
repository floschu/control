package at.florianschuster.control.counterexample

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.test.ControllerStub
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class CounterViewTest {

    private lateinit var controllerStub: ControllerStub<CounterAction, CounterMutation, CounterState>

    @Before
    fun setup() {
        controllerStub = ControllerStub(initialState = CounterState())
        CounterView.ControllerFactory = { controllerStub }
        launchFragmentInContainer<CounterView>()
    }

    @Test
    fun whenPressingIncreaseButtonIncrementActionIsTriggered() {
        // when
        onView(withId(R.id.increaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Increment, controllerStub.dispatchedActions.last())
    }

    @Test
    fun whenPressingDecreaseButtonDecrementActionIsTriggered() {
        // when
        onView(withId(R.id.decreaseButton)).perform(click())

        // then
        assertEquals(CounterAction.Decrement, controllerStub.dispatchedActions.last())
    }

    @Test
    fun whenStateOffersValueItIsDisplayedInTextView() {
        // given
        val testValue = 1

        // when
        controllerStub.emitState(CounterState(value = testValue))

        // then
        onView(withId(R.id.valueTextView)).check(matches(withText("$testValue")))
    }

    @Test
    fun whenStateOffersLoadingProgressBarIsVisible() {
        // when
        controllerStub.emitState(CounterState(loading = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))
    }
}