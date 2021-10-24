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
import androidx.test.espresso.matcher.ViewMatchers.withText
import at.florianschuster.control.EffectControllerStub
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.toStub
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

internal class SearchViewTest {

    private lateinit var stub: EffectControllerStub<SearchViewModel.Action, SearchViewModel.State, SearchViewModel.Effect>

    @Before
    fun setup() {
        SearchViewModel.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val viewModel = SearchViewModel()
                stub = viewModel.controller.toStub()
                return viewModel as T
            }
        }
        launchFragmentInContainer<SearchView>(themeResId = R.style.Theme_MaterialComponents)
    }

    @Test
    fun whenSearchEditTextInput_ThenCorrectControllerAction() {
        // given
        val testQuery = "test"

        // when
        onView(withId(R.id.searchEditText)).perform(replaceText(testQuery))
        onView(isRoot()).perform(idleFor(SearchView.SearchDebounceMilliseconds))

        // then
        assertEquals(
            SearchViewModel.Action.UpdateQuery(testQuery),
            stub.dispatchedActions.last()
        )
    }

    @Test
    fun whenStateOffersLoadingNextPage_ThenProgressBarIsShown() {
        // when
        stub.emitState(SearchViewModel.State(loadingNextPage = true))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(isDisplayed()))

        // when
        stub.emitState(SearchViewModel.State(loadingNextPage = false))

        // then
        onView(withId(R.id.loadingProgressBar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun whenNetworkErrorEffect_ThenSnackbarIsShown() {
        // when
        stub.emitEffect(SearchViewModel.Effect.NotifyNetworkError)

        // then
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.info_network_error)))
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