package at.florianschuster.control.githubexample.search

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.githubexample.R
import at.florianschuster.control.githubexample.TestActivity
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class GithubViewTest {

    @get:Rule
    val activityRule = activityScenarioRule<TestActivity>()

    private lateinit var viewModel: GithubViewModel

    @Before
    fun setup() {
        viewModel = GithubViewModel().apply { controller.stubEnabled = true }
        GithubView.GithubViewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
        }
        activityRule.scenario.onActivity { it.setFragment(GithubView()) }
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
            GithubViewModel.Action.UpdateQuery(testQuery),
            viewModel.controller.stub.actions.last()
        )
    }

    @Test
    fun whenStateOffersLoadingNextPageThenProgressBarIsShown() {
        // when
        viewModel.controller.stub.setState(GithubViewModel.State(loadingNextPage = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))

        // when
        viewModel.controller.stub.setState(GithubViewModel.State(loadingNextPage = false))

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