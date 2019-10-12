package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import at.florianschuster.control.githubexample.remote.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
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

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        controller = GithubSearchController(api = githubApi).apply { scope = testScopeRule }
    }

    @Test
    fun `update query`() = testScopeRule.runBlockingTest {
        // given
        val query = "test"
        coEvery { githubApi.repos(query, 1) } returns mockResultPage1

        // when
        controller.action(GithubSearchController.Action.UpdateQuery(query))

        // then
        coVerify { githubApi.repos(query, 1) }
        assertEquals(
            GithubSearchController.State(query, mockReposPage1, 1, false),
            controller.currentState
        )
    }

    companion object {
        private val mockReposPage1: List<Repo> = (0..5).map { Repo(it, "$it") }
        private val mockResultPage1 = Result(items = mockReposPage1)
        
        private val mockReposPage2: List<Repo> = (6..10).map { Repo(it, "$it") }
        private val mockResultPage2 = Result(items = mockReposPage2)
    }
}
