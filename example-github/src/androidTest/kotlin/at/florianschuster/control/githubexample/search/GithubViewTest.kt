package at.florianschuster.control.githubexample.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.githubexample.TestActivity
import at.florianschuster.control.githubexample.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class GithubViewTest {

    @get:Rule
    val activityRule = activityScenarioRule<TestActivity>()

    private lateinit var controller: GithubControllerViewModel

    @Before
    fun setup() {
        controller = GithubControllerViewModel().apply { stubEnabled = true }
        GithubView.ControllerViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = controller as T
        }
        activityRule.scenario.onActivity { it.setFragment(GithubView()) }
    }

    @Test
    fun whenSearchEditTextInputThenCorrectControllerAction() {
        // given
        val testQuery = "test"

        // when
        onView(withId(R.id.searchEditText)).perform(typeText(testQuery))

        // then
        assertEquals(
            GithubController.Action.UpdateQuery(testQuery),
            controller.stub.actions.last()
        )
    }

    @Test
    fun whenStateOffersLoadingNextPageThenProgressBarIsShown() {
        // when
        controller.stub.setState(GithubController.State(loadingNextPage = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))

        // when
        controller.stub.setState(GithubController.State(loadingNextPage = false))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(not(isDisplayed())))
    }
}
