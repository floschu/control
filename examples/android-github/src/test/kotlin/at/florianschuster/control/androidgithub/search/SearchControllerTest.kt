package at.florianschuster.control.androidgithub.search

import at.florianschuster.control.EffectController
import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.model.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchControllerTest {

    private lateinit var githubApi: GithubApi
    private lateinit var sut: EffectController<SearchAction, SearchState, SearchEffect>
    private lateinit var states: List<SearchState>
    private lateinit var effects: List<SearchEffect>

    private fun CoroutineScope.givenControllerIsCreated(
        initialState: SearchState = SearchState()
    ) {
        githubApi = mockk {
            coEvery { search(any(), 1) } returns MockReposPage1
            coEvery { search(any(), 2) } returns MockReposPage2
        }
        sut = createSearchController(initialState, githubApi)
        states = sut.state.testIn(this)
        effects = sut.effects.testIn(this)
    }

    private fun runTestAndCleanup(
        body: TestScope.() -> Unit
    ) = runTest(UnconfinedTestDispatcher()) {
        try {
            body()
        } finally {
            coroutineContext.cancelChildren() // cancel states/effects
        }
    }

    @Test
    fun `UpdateQuery - with non-empty text`() = runTestAndCleanup {
        // given
        givenControllerIsCreated()

        // when
        sut.dispatch(SearchAction.UpdateQuery(MockQuery))

        // then
        coVerify(exactly = 1) { githubApi.search(MockQuery, 1) }
        assertEquals(listOf(false, true, false), states.map(SearchState::loadingNextPage))
        assertEquals(
            SearchState(
                query = MockQuery,
                repos = MockReposPage1,
                page = 1,
                loadingNextPage = false
            ),
            states.last()
        )
    }

    @Test
    fun `UpdateQuery - with empty text`() = runTestAndCleanup {
        // given
        val emptyQuery = ""
        givenControllerIsCreated()

        // when
        sut.dispatch(SearchAction.UpdateQuery(emptyQuery))

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        assertEquals(states.last(), SearchState(query = emptyQuery))
    }

    @Test
    fun `LoadNextPage - loads correct next page`() = runTestAndCleanup {
        // given
        givenControllerIsCreated(
            SearchState(query = MockQuery, repos = MockReposPage1)
        )

        // when
        sut.dispatch(SearchAction.LoadNextPage)

        // then
        coVerify(exactly = 1) { githubApi.search(any(), 2) }
        with(sut.state.value) {
            assertEquals(MockQuery, query)
            assertEquals(MockReposPage1 + MockReposPage2, repos)
            assertFalse(loadingNextPage)
        }
    }

    @Test
    fun `LoadNextPage - only when currently not loading`() = runTestAndCleanup {
        // given
        val initialState = SearchState(loadingNextPage = true)
        givenControllerIsCreated(initialState)

        // when
        sut.dispatch(SearchAction.LoadNextPage)

        // then
        coVerify(exactly = 0) { githubApi.search(any(), any()) }
        assertEquals(listOf(initialState), states)
    }

    @Test
    fun `UpdateQuery - exception from api is correctly handled`() =
        runTestAndCleanup {
            // given
            givenControllerIsCreated()
            coEvery { githubApi.search(any(), any()) } throws IllegalStateException()

            // when
            sut.dispatch(SearchAction.UpdateQuery(MockQuery))

            // then
            coVerify(exactly = 1) { githubApi.search(MockQuery, 1) }
            assertEquals(
                SearchState(query = MockQuery, loadingNextPage = false),
                states.last()
            )
            assertEquals(SearchEffect.NotifyNetworkError, effects.single())
        }

    @Test
    fun `UpdateQuery - search resets previous`() = runTestAndCleanup {
        // given
        givenControllerIsCreated()
        coEvery { githubApi.search(MockQuery, 1) } coAnswers {
            delay(1000)
            MockReposPage1
        }
        coEvery { githubApi.search(MockSecondQuery, 1) } coAnswers {
            delay(1000)
            MockReposPage2
        }

        // when
        sut.dispatch(SearchAction.UpdateQuery(MockQuery))
        advanceTimeBy(500) // updated before last query can finish
        sut.dispatch(SearchAction.UpdateQuery(MockSecondQuery))
        advanceUntilIdle()

        // then
        coVerifyOrder {
            githubApi.search(MockQuery, 1)
            githubApi.search(MockSecondQuery, 1)
        }
        assertFalse(states.any { it.repos == MockReposPage1 })
        assertEquals(
            SearchState(query = MockSecondQuery, repos = MockReposPage2),
            states.last()
        )
    }

    companion object {
        private val MockReposPage1: List<Repository> = (0..2).map {
            Repository(it, "$it", "", Repository.Owner(""), "", "")
        }
        private val MockReposPage2: List<Repository> = (3..4).map {
            Repository(it, "$it", "", Repository.Owner(""), "", "")
        }

        private const val MockQuery = "control"
        private const val MockSecondQuery = "controlAgain"
    }
}

private fun <T> Flow<T>.testIn(scope: CoroutineScope): List<T> {
    val emissions = mutableListOf<T>()
    scope.launch { toList(emissions) }
    return emissions
}
