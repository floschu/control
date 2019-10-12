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

class GithubSearchControllerTest {

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    private val githubApi: GithubApi = mockk()
    private lateinit var controller: GithubSearchController
    private lateinit var controllerStates: List<GithubSearchController.State>

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { githubApi.repos(any(), 1) } returns mockResultPage1
        coEvery { githubApi.repos(any(), 2) } returns mockResultPage2
    }

    private fun `given github search controller`(
        initialState: GithubSearchController.State = GithubSearchController.State()
    ) {
        controller = GithubSearchController(initialState, githubApi).apply { scope = testScopeRule }
        controllerStates = mutableListOf<GithubSearchController.State>().also { states ->
            testScopeRule.launch { controller.state.toList(states) }
        }
    }

    @Test
    fun `update query with non-empty text`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        `given github search controller`()

        // when
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        assertEquals(
            listOf(
                GithubSearchController.State(),
                GithubSearchController.State(query = query),
                GithubSearchController.State(query = query, loadingNextPage = true),
                GithubSearchController.State(query, mockReposPage1, 1, true),
                GithubSearchController.State(query, mockReposPage1, 1, false)
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
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        assertEquals(
            listOf(GithubSearchController.State(), GithubSearchController.State(query = query)),
            controllerStates
        )
    }

    @Test
    fun `load next page loads correct next page`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        `given github search controller`(GithubSearchController.State(query = query, repos = mockReposPage1))

        // when
        controller.action(GithubSearchController.Action.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.repos(any(), 2) }
        assertEquals(
            listOf(
                GithubSearchController.State(query = query, repos = mockReposPage1),
                GithubSearchController.State(query, mockReposPage1, 1, true),
                GithubSearchController.State(query, mockReposPage1 + mockReposPage2, 2, true),
                GithubSearchController.State(query, mockReposPage1 + mockReposPage2, 2, false)
            ),
            controllerStates
        )
    }

    @Test
    fun `load next page only when currently not loading`() = testScopeRule.runBlockingTest {
        // given
        val initialState = GithubSearchController.State(loadingNextPage = true)
        `given github search controller`(initialState)

        // when
        controller.action(GithubSearchController.Action.LoadNextPage)

        // then
        coVerify { githubApi.repos(any(), any()) wasNot Called }
        assertEquals(listOf(initialState), controllerStates)
    }

    @Test
    fun `error from api is correctly handled`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        coEvery { githubApi.repos(any(), any()) } throws Error()
        `given github search controller`()

        // when
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        coVerify(exactly = 1) { githubApi.repos(query, 1) }
        assertEquals(
            listOf(
                GithubSearchController.State(),
                GithubSearchController.State(query = query),
                GithubSearchController.State(query = query, loadingNextPage = true),
                GithubSearchController.State(query = query, loadingNextPage = false)
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
