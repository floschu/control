package at.florianschuster.control.androidgithub.search

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.toStub
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class GithubViewTest {

    private lateinit var viewModel: GithubViewModel

    @Before
    fun setup() {
        GithubView.GithubViewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GithubViewModel().apply { controller.toStub() } as T
            }
        }
        launchFragmentInContainer<GithubView>(themeResId = R.style.Theme_MaterialComponents)
    }

    @Test
    fun whenSearchEditTextInputThenCorrectControllerAction() {
        // given
        val testQuery = "test"

        // when
        onView(withId(R.id.searchEditText)).perform(replaceText(testQuery))
        onView(isRoot()).perform(idleFor(GithubView.SearchDebounceMilliseconds))

        // then
        assertEquals(
            GithubViewModel.Action.UpdateQuery(testQuery),
            viewModel.controller.toStub().dispatchedActions.last()
        )
    }

    @Test
    fun whenStateOffersLoadingNextPageThenProgressBarIsShown() {
        // when
        viewModel.controller.toStub().emitState(GithubViewModel.State(loadingNextPage = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))

        // when
        viewModel.controller.toStub().emitState(GithubViewModel.State(loadingNextPage = false))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(not(isDisplayed())))
    }
}

@Suppress("SameParameterValue")
private fun idleFor(millis: Long): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isRoot()
    override fun getDescription(): String = "idle for $millis ms"

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadForAtLeast(millis)
    }
}