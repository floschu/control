package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import at.florianschuster.control.githubexample.remote.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GithubSearchControllerTest {

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    @MockK
    private lateinit var githubApi: GithubApi

    private lateinit var controller: GithubSearchController
    private val controllerStates = mutableListOf<GithubSearchController.State>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        controller = GithubSearchController(api = githubApi).apply { scope = testScopeRule }
        testScopeRule.launch { controller.state.toList(controllerStates) }
    }

    @After
    fun after() {
        controllerStates.clear()
    }

    @Test
    fun `update query with non-empty text`() = testScopeRule.runBlockingTest {
        // given
        val query = "control"
        coEvery { githubApi.repos(query, 1) } returns mockResultPage1

        // when
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        coVerify { githubApi.repos(query, 1) }
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

        // when
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        assertEquals(
            listOf(GithubSearchController.State(), GithubSearchController.State(query = query)),
            controllerStates
        )
    }

    @Test
    fun `load next page only when currently not loading`() = testScopeRule.runBlockingTest {

    }

    @Test
    fun `load next page loads correct next page`() = testScopeRule.runBlockingTest {

    }

    @Test
    fun `error from api is correctly handled`() = testScopeRule.runBlockingTest {

    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..5).map { Repo(it, "$it") }
        private val mockResultPage1 = Result(items = mockReposPage1)

        private val mockReposPage2: List<Repo> = (6..10).map { Repo(it, "$it") }
        private val mockResultPage2 = Result(items = mockReposPage2)
    }
}
