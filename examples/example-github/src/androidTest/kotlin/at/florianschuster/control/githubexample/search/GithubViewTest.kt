package at.florianschuster.control.githubexample.search

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.githubexample.R
import at.florianschuster.control.test.ControllerStub
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class GithubViewTest {

    private lateinit var controllerStub: ControllerStub<GithubAction, GithubMutation, GithubState>

    @Before
    fun setup() {
        GithubViewModel.ControllerFactory = { _, initialState, _, _ ->
            controllerStub = ControllerStub(initialState)
            controllerStub
        }
        launchFragmentInContainer<GithubView>(themeResId = R.style.Theme_MaterialComponents)
    }

    @Test
    fun whenSearchEditTextInputThenCorrectControllerAction() {
        // given
        val testQuery = "test"

        // when
        onView(withId(R.id.searchEditText)).perform(replaceText(testQuery))
        onView(isRoot()).perform(idleFor(500)) // wait for debounce

        // then
        assertEquals(
            GithubAction.UpdateQuery(testQuery),
            controllerStub.dispatchedActions.last()
        )
    }

    @Test
    fun whenStateOffersLoadingNextPageThenProgressBarIsShown() {
        // when
        controllerStub.emitState(GithubState(loadingNextPage = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))

        // when
        controllerStub.emitState(GithubState(loadingNextPage = false))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(not(isDisplayed())))
    }
}

private fun idleFor(millis: Long): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isRoot()
    override fun getDescription(): String = "idle for $millis ms"

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadForAtLeast(millis)
    }
}