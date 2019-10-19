package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import at.florianschuster.control.githubexample.remote.Result
import at.florianschuster.control.test.TestCollector
import at.florianschuster.control.test.TestCoroutineScopeRule
import at.florianschuster.control.test.emissions
import at.florianschuster.control.test.emissionCount
import at.florianschuster.control.test.expect
import at.florianschuster.control.test.test
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GithubControllerTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    private val githubApi: GithubApi = mockk {
        coEvery { repos(any(), 1) } returns mockResultPage1
        coEvery { repos(any(), 2) } returns mockResultPage2
    }
    private lateinit var controller: GithubController
    private lateinit var stateCollector: TestCollector<GithubController.State>

    @Before
    fun setup() = MockKAnnotations.init(this)

    private fun `given github search controller`(
        initialState: GithubController.State = GithubController.State()
    ) {
        controller = GithubController(initialState, githubApi).apply { scope = testScopeRule }
        stateCollector = controller.state.test(testScopeRule)
    }

    @Test
    fun `update query with non-empty text`() {
        // given
        val query = "control"
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        stateCollector expect emissions(
            GithubController.State(),
            GithubController.State(query = query),
            GithubController.State(query = query, loadingNextPage = true),
            GithubController.State(query, mockReposPage1, 1, true),
            GithubController.State(query, mockReposPage1, 1, false)
        )
    }

    @Test
    fun `update query with empty text`() {
        // given
        val query = ""
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        stateCollector expect emissions(
            GithubController.State(),
            GithubController.State(query = query)
        )
    }

    @Test
    fun `load next page loads correct next page`() {
        // given
        val query = "control"
        `given github search controller`(
            GithubController.State(query = query, repos = mockReposPage1)
        )

        // when
        controller.action(GithubController.Action.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.repos(any(), 2) }
        stateCollector expect emissions(
            GithubController.State(query = query, repos = mockReposPage1),
            GithubController.State(query, mockReposPage1, 1, true),
            GithubController.State(query, mockReposPage1 + mockReposPage2, 2, true),
            GithubController.State(query, mockReposPage1 + mockReposPage2, 2, false)
        )
    }

    @Test
    fun `load next page only when currently not loading`() {
        // given
        val initialState = GithubController.State(loadingNextPage = true)
        `given github search controller`(initialState)

        // when
        controller.action(GithubController.Action.LoadNextPage)

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        stateCollector expect emissionCount(1)
        stateCollector expect emissions(initialState)
    }

    @Test
    fun `error from api is correctly handled`() {
        // given
        val query = "control"
        coEvery { githubApi.repos(any(), any()) } throws Exception()
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        stateCollector expect emissions(
            GithubController.State(),
            GithubController.State(query = query),
            GithubController.State(query = query, loadingNextPage = true),
            GithubController.State(query = query, loadingNextPage = false)
        )
    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..2).map { Repo(it, "$it") }
        private val mockResultPage1 = Result(items = mockReposPage1)

        private val mockReposPage2: List<Repo> = (3..4).map { Repo(it, "$it") }
        private val mockResultPage2 = Result(items = mockReposPage2)
    }
}
