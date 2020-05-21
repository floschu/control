package at.florianschuster.control.androidgithub.search

import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.Repo
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
import kotlin.test.assertFalse

internal class GithubViewModelTest {

    @get:Rule
    val testCoroutineScope = TestCoroutineScopeRule()

    private val githubApi: GithubApi = mockk {
        coEvery { search(any(), 1) } returns mockReposPage1
        coEvery { search(any(), 2) } returns mockReposPage2
    }
    private lateinit var sut: GithubViewModel
    private lateinit var states: TestFlow<GithubViewModel.State>

    private fun `given github search controller`(
        initialState: GithubViewModel.State = GithubViewModel.State()
    ) {
        sut = GithubViewModel(initialState, githubApi, testCoroutineScope.dispatcher)
        states = sut.controller.state.testIn(testCoroutineScope)
    }

    @Test
    fun `update query with non-empty text`() {
        // given
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.search(query, 1) }
        states expect emissions(
            GithubViewModel.State(),
            GithubViewModel.State(query = query),
            GithubViewModel.State(query = query, loadingNextPage = true),
            GithubViewModel.State(query, mockReposPage1, 1, true),
            GithubViewModel.State(query, mockReposPage1, 1, false)
        )
    }

    @Test
    fun `update query with empty text`() {
        // given
        val emptyQuery = ""
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(emptyQuery))

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        states expect lastEmission(GithubViewModel.State(query = emptyQuery))
    }

    @Test
    fun `load next page loads correct next page`() {
        // given
        `given github search controller`(
            GithubViewModel.State(query = query, repos = mockReposPage1)
        )

        // when
        sut.controller.dispatch(GithubViewModel.Action.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.search(any(), 2) }
        states expect emissions(
            GithubViewModel.State(query = query, repos = mockReposPage1),
            GithubViewModel.State(query, mockReposPage1, 1, true),
            GithubViewModel.State(query, mockReposPage1 + mockReposPage2, 2, true),
            GithubViewModel.State(query, mockReposPage1 + mockReposPage2, 2, false)
        )
    }

    @Test
    fun `load next page only when currently not loading`() {
        // given
        val initialState = GithubViewModel.State(loadingNextPage = true)
        `given github search controller`(initialState)

        // when
        sut.controller.dispatch(GithubViewModel.Action.LoadNextPage)

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        states expect emissionCount(1)
        states expect emissions(initialState)
    }

    @Test
    fun `empty list from github api is correctly handled`() {
        // given
        coEvery { githubApi.search(any(), any()) } returns emptyList()
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.search(query, 1) }
        states expect emissions(
            GithubViewModel.State(),
            GithubViewModel.State(query = query),
            GithubViewModel.State(query = query, loadingNextPage = true),
            GithubViewModel.State(query = query, loadingNextPage = false)
        )
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
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))
        testCoroutineScope.advanceTimeBy(500) // updated before last query can finish
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(secondQuery))
        testCoroutineScope.advanceUntilIdle()

        // then
        coVerifyOrder {
            githubApi.search(query, 1)
            githubApi.search(secondQuery, 1)
        }
        assertFalse(states.emissions.any { it.repos == mockReposPage1 })
        states expect lastEmission(
            GithubViewModel.State(query = secondQuery, repos = mockReposPage2)
        )
    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..2).map { Repo(it, "$it", "") }
        private val mockReposPage2: List<Repo> = (3..4).map { Repo(it, "$it", "") }

        private const val query = "control"
        private const val secondQuery = "controlAgain"
    }
}
