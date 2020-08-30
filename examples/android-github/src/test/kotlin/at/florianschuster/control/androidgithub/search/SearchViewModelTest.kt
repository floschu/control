package at.florianschuster.control.androidgithub.search

import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.model.Repository
import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.TestFlow
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.testIn
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFalse

internal class SearchViewModelTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    private val githubApi: GithubApi = mockk {
        coEvery { search(any(), 1) } returns mockReposPage1
        coEvery { search(any(), 2) } returns mockReposPage2
    }
    private lateinit var sut: SearchViewModel
    private lateinit var states: TestFlow<SearchViewModel.State>
    private lateinit var effects: TestFlow<SearchViewModel.Effect>

    private fun `given ViewModel is created`(
        initialState: SearchViewModel.State = SearchViewModel.State()
    ) {
        sut = SearchViewModel(initialState, githubApi, testCoroutineScope.dispatcher)
        states = sut.controller.state.testIn(testCoroutineScope)
        effects = sut.controller.effects.testIn(testCoroutineScope)
    }

    @Test
    fun `update query with non-empty text`() {
        // given
        `given ViewModel is created`()

        // when
        sut.controller.dispatch(SearchViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.search(query, 1) }
        states expect emissions(
            SearchViewModel.State(),
            SearchViewModel.State(query = query),
            SearchViewModel.State(query = query, loadingNextPage = true),
            SearchViewModel.State(query, mockReposPage1, 1, true),
            SearchViewModel.State(query, mockReposPage1, 1, false)
        )
    }

    @Test
    fun `update query with empty text`() {
        // given
        val emptyQuery = ""
        `given ViewModel is created`()

        // when
        sut.controller.dispatch(SearchViewModel.Action.UpdateQuery(emptyQuery))

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        states expect lastEmission(SearchViewModel.State(query = emptyQuery))
    }

    @Test
    fun `load next page loads correct next page`() {
        // given
        `given ViewModel is created`(
            SearchViewModel.State(query = query, repos = mockReposPage1)
        )

        // when
        sut.controller.dispatch(SearchViewModel.Action.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.search(any(), 2) }
        states expect emissions(
            SearchViewModel.State(query = query, repos = mockReposPage1),
            SearchViewModel.State(query, mockReposPage1, 1, true),
            SearchViewModel.State(query, mockReposPage1 + mockReposPage2, 2, true),
            SearchViewModel.State(query, mockReposPage1 + mockReposPage2, 2, false)
        )
    }

    @Test
    fun `load next page only when currently not loading`() {
        // given
        val initialState = SearchViewModel.State(loadingNextPage = true)
        `given ViewModel is created`(initialState)

        // when
        sut.controller.dispatch(SearchViewModel.Action.LoadNextPage)

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        states expect emissionCount(1)
        states expect emissions(initialState)
    }

    @Test
    fun `empty list from github api is correctly handled`() {
        // given
        coEvery { githubApi.search(any(), any()) } throws IOException()
        `given ViewModel is created`()

        // when
        sut.controller.dispatch(SearchViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.search(query, 1) }
        states expect emissions(
            SearchViewModel.State(),
            SearchViewModel.State(query = query),
            SearchViewModel.State(query = query, loadingNextPage = true),
            SearchViewModel.State(query = query, loadingNextPage = false)
        )
        effects expect emissions(SearchViewModel.Effect.NetworkError)
    }

    @Test
    fun `updating query during search resets search`() {
        // given
        coEvery { githubApi.search(query, 1) } coAnswers {
            delay(1000)
            mockReposPage1
        }
        coEvery { githubApi.search(secondQuery, 1) } coAnswers {
            delay(1000)
            mockReposPage2
        }
        `given ViewModel is created`()

        // when
        sut.controller.dispatch(SearchViewModel.Action.UpdateQuery(query))
        testCoroutineScope.advanceTimeBy(500) // updated before last query can finish
        sut.controller.dispatch(SearchViewModel.Action.UpdateQuery(secondQuery))
        testCoroutineScope.advanceUntilIdle()

        // then
        coVerifyOrder {
            githubApi.search(query, 1)
            githubApi.search(secondQuery, 1)
        }
        assertFalse(states.emissions.any { it.repos == mockReposPage1 })
        states expect lastEmission(
            SearchViewModel.State(query = secondQuery, repos = mockReposPage2)
        )
    }

    companion object {
        private val mockReposPage1: List<Repository> = (0..2).map {
            Repository(it, "$it", "", Repository.Owner(""), "", "")
        }
        private val mockReposPage2: List<Repository> = (3..4).map {
            Repository(it, "$it", "", Repository.Owner(""), "", "")
        }

        private const val query = "control"
        private const val secondQuery = "controlAgain"
    }
}
