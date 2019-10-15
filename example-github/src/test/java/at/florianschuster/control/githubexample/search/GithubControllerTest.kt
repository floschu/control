package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import at.florianschuster.control.githubexample.remote.Result
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GithubControllerTest {

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    private val githubApi: GithubApi = mockk()
    private lateinit var controller: GithubController
    private lateinit var controllerStates: List<GithubController.State>

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { githubApi.repos(any(), 1) } returns mockResultPage1
        coEvery { githubApi.repos(any(), 2) } returns mockResultPage2
    }

    private fun `given github search controller`(
        initialState: GithubController.State = GithubController.State()
    ) {
        controller = GithubController(initialState, githubApi).apply { scope = testScopeRule }
        controllerStates = mutableListOf<GithubController.State>().also { states ->
            testScopeRule.launch { controller.state.toList(states) }
        }
    }

    @Test
    fun `update query with non-empty text`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        assertEquals(
            listOf(
                GithubController.State(),
                GithubController.State(query = query),
                GithubController.State(query = query, loadingNextPage = true),
                GithubController.State(query, mockReposPage1, 1, true),
                GithubController.State(query, mockReposPage1, 1, false)
            ),
            controllerStates
        )
    }

    @Test
    fun `update query with empty text`() = testScopeRule.runBlockingTest {
        // given
        val query = ""
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        assertEquals(
            listOf(GithubController.State(), GithubController.State(query = query)),
            controllerStates
        )
    }

    @Test
    fun `load next page loads correct next page`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        `given github search controller`(GithubController.State(query = query, repos = mockReposPage1))

        // when
        controller.action(GithubController.Action.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.repos(any(), 2) }
        assertEquals(
            listOf(
                GithubController.State(query = query, repos = mockReposPage1),
                GithubController.State(query, mockReposPage1, 1, true),
                GithubController.State(query, mockReposPage1 + mockReposPage2, 2, true),
                GithubController.State(query, mockReposPage1 + mockReposPage2, 2, false)
            ),
            controllerStates
        )
    }

    @Test
    fun `load next page only when currently not loading`() = testScopeRule.runBlockingTest {
        // given
        val initialState = GithubController.State(loadingNextPage = true)
        `given github search controller`(initialState)

        // when
        controller.action(GithubController.Action.LoadNextPage)

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        assertEquals(listOf(initialState), controllerStates)
    }

    @Test
    fun `error from api is correctly handled`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        coEvery { githubApi.repos(any(), any()) } throws Exception()
        `given github search controller`()

        // when
        controller.action(GithubController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        assertEquals(
            listOf(
                GithubController.State(),
                GithubController.State(query = query),
                GithubController.State(query = query, loadingNextPage = true),
                GithubController.State(query = query, loadingNextPage = false)
            ),
            controllerStates
        )
    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..2).map { Repo(it, "$it") }
        private val mockResultPage1 = Result(items = mockReposPage1)

        private val mockReposPage2: List<Repo> = (3..4).map { Repo(it, "$it") }
        private val mockResultPage2 = Result(items = mockReposPage2)
    }
}
