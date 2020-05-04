package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import at.florianschuster.control.githubexample.Result
import at.florianschuster.test.coroutines.TestCoroutineScopeRule
import at.florianschuster.test.flow.TestFlow
import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.emissions
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.lastEmission
import at.florianschuster.test.flow.testIn
import io.mockk.Called
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
        coEvery { repos(any(), 1) } returns mockResultPage1
        coEvery { repos(any(), 2) } returns mockResultPage2
    }
    private lateinit var sut: GithubViewModel
    private lateinit var states: TestFlow<GithubViewModel.State>

    private fun `given github search controller`(
        initialState: GithubViewModel.State = GithubViewModel.State()
    ) {
        sut = GithubViewModel(initialState, testCoroutineScope.dispatcher, githubApi)
        states = sut.controller.state.testIn(testCoroutineScope)
    }

    @Test
    fun `update query with non-empty text`() {
        // given
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
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
        coVerify { githubApi.repos(any(), any()) wasNot Called }
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
        coVerify(exactly = 1) { githubApi.repos(any(), 2) }
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
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        states expect emissionCount(1)
        states expect emissions(initialState)
    }

    @Test
    fun `error from api is correctly handled`() {
        // given
        coEvery { githubApi.repos(any(), any()) } throws Exception()
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
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
        coEvery { githubApi.repos(query, 1) } coAnswers {
            delay(1000)
            mockResultPage1
        }
        coEvery { githubApi.repos(secondQuery, 1) } coAnswers {
            delay(1000)
            mockResultPage2
        }
        `given github search controller`()

        // when
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(query))
        testCoroutineScope.advanceTimeBy(500) // updated before last query can finish
        sut.controller.dispatch(GithubViewModel.Action.UpdateQuery(secondQuery))
        testCoroutineScope.advanceUntilIdle()

        // then
        coVerifyOrder {
            githubApi.repos(query, 1)
            githubApi.repos(secondQuery, 1)
        }
        assertFalse(states.emissions.any { it.repos == mockReposPage1 })
        states expect lastEmission(
            GithubViewModel.State(query = secondQuery, repos = mockReposPage2)
        )
    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..2).map { Repo(it, "$it", "") }
        private val mockResultPage1 = Result(items = mockReposPage1)

        private val mockReposPage2: List<Repo> = (3..4).map { Repo(it, "$it", "") }
        private val mockResultPage2 = Result(items = mockReposPage2)

        private const val query = "control"
        private const val secondQuery = "controlAgain"
    }
}
