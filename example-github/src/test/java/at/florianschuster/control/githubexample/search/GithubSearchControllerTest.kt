package at.florianschuster.control.githubexample.search

import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import at.florianschuster.control.githubexample.remote.Result
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GithubSearchControllerTest { // todo

    @get:Rule
    val testScopeRule = CoroutineScopeRule()

    private lateinit var githubApi: GithubApi
    private lateinit var controller: GithubSearchController

    @Before
    fun setup() {
        githubApi = object : GithubApi {
            override suspend fun repos(query: String, page: Int): Result {
                delay(500)
                return Result(listOf(
                    Repo(0, "floschu/control"),
                    Repo(1, "floschu/Reaktor"),
                    Repo(2, "floschu/Watchables"),
                    Repo(3, "floschu/Playables")
                ))
            }
        }
        controller = GithubSearchController(api = githubApi).apply { scope = testScopeRule }
    }

    @Test
    fun addition_isCorrect() {

    }
}
