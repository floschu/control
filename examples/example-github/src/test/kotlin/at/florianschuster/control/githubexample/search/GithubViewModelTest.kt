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
    private lateinit var states: TestFlow<GithubState>

    private fun `given github search controller`(
        initialState: GithubState = GithubState()
    ) {
        sut = GithubViewModel(initialState, githubApi, testCoroutineScope)
        states = sut.state.testIn(testCoroutineScope)
    }

    @Test
    fun `update query with non-empty text`() {
        // given
        `given github search controller`()

        // when
        sut.dispatch(GithubAction.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        states expect emissions(
            GithubState(),
            GithubState(query = query),
            GithubState(query = query, loadingNextPage = true),
            GithubState(query, mockReposPage1, 1, true),
            GithubState(query, mockReposPage1, 1, false)
        )
    }

    @Test
    fun `update query with empty text`() {
        // given
        val emptyQuery = ""
        `given github search controller`()

        // when
        sut.dispatch(GithubAction.UpdateQuery(emptyQuery))

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        states expect lastEmission(GithubState(query = emptyQuery))
    }

    @Test
    fun `load next page loads correct next page`() {
        // given
        `given github search controller`(
            GithubState(query = query, repos = mockReposPage1)
        )

        // when
        sut.dispatch(GithubAction.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.repos(any(), 2) }
        states expect emissions(
            GithubState(query = query, repos = mockReposPage1),
            GithubState(query, mockReposPage1, 1, true),
            GithubState(query, mockReposPage1 + mockReposPage2, 2, true),
            GithubState(query, mockReposPage1 + mockReposPage2, 2, false)
        )
    }

    @Test
    fun `load next page only when currently not loading`() {
        // given
        val initialState = GithubState(loadingNextPage = true)
        `given github search controller`(initialState)

        // when
        sut.dispatch(GithubAction.LoadNextPage)

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
        sut.dispatch(GithubAction.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        states expect emissions(
            GithubState(),
            GithubState(query = query),
            GithubState(query = query, loadingNextPage = true),
            GithubState(query = query, loadingNextPage = false)
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
        sut.dispatch(GithubAction.UpdateQuery(query))
        testCoroutineScope.advanceTimeBy(500) // updated before last query can finish
        sut.dispatch(GithubAction.UpdateQuery(secondQuery))
        testCoroutineScope.advanceUntilIdle()

        // then
        coVerifyOrder {
            githubApi.repos(query, 1)
            githubApi.repos(secondQuery, 1)
        }
        assertFalse(states.emissions.any { it.repos == mockReposPage1 })
        states expect lastEmission(
            GithubState(query = secondQuery, repos = mockReposPage2)
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
