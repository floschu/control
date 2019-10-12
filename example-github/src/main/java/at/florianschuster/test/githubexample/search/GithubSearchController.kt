package at.florianschuster.test.githubexample.search

import at.florianschuster.test.android.ControllerViewModel
import at.florianschuster.test.githubexample.remote.GithubApi
import at.florianschuster.test.githubexample.remote.Repo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GithubSearchController(
    private val api: GithubApi = GithubApi.Factory()
) : ControllerViewModel<GithubSearchController.Action, GithubSearchController.Mutation, GithubSearchController.State>() {

    sealed class Action {
        data class UpdateQuery(val query: String) : Action()
        object LoadNextPage : Action()
    }

    sealed class Mutation {
        data class SetQuery(val query: String) : Mutation()
        data class SetRepos(val repos: List<Repo>) : Mutation()
        data class AppendRepos(val repos: List<Repo>) : Mutation()
        data class SetLoadingNextPage(val loading: Boolean) : Mutation()
    }

    data class State(
        val query: String = "",
        val repos: List<Repo> = emptyList(),
        val page: Int = 1,
        val loadingNextPage: Boolean = false
    )

    override val initialState: State = State()

    override fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.UpdateQuery -> flow {
            emit(Mutation.SetQuery(action.query))
            if (action.query.isEmpty()) return@flow
            emit(Mutation.SetLoadingNextPage(true))
            emitAll(flowSearch(action.query, 1).map { Mutation.SetRepos(it) })
            emit(Mutation.SetLoadingNextPage(false))
        }
        is Action.LoadNextPage -> {
            if (currentState.loadingNextPage) emptyFlow()
            else flow {
                emit(Mutation.SetLoadingNextPage(true))
                val repos = coroutineSearch(currentState.query, currentState.page + 1)
                emit(Mutation.AppendRepos(repos))
                emit(Mutation.SetLoadingNextPage(false))
            }
        }
    }

    override fun reduce(previousState: State, mutation: Mutation): State =
        when (mutation) {
            is Mutation.SetQuery -> previousState.copy(query = mutation.query)
            is Mutation.SetRepos -> previousState.copy(repos = mutation.repos, page = 1)
            is Mutation.AppendRepos -> previousState.copy(
                repos = previousState.repos + mutation.repos,
                page = previousState.page + 1
            )
            is Mutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = mutation.loading)
        }

    /**
     * Search with [Flow]
     */
    private fun flowSearch(query: String, page: Int): Flow<List<Repo>> =
        flow { emit(api.repos(query, page)) }
            .map { it.items }
            .catch { e ->
                println("Search Error: $e")
                emit(emptyList())
            }

    /**
     * Search with [suspend] function.
     */
    private suspend fun coroutineSearch(query: String, page: Int): List<Repo> =
        try {
            api.repos(query, page).items
        } catch (e: Exception) {
            println("Search Error: $e")
            emptyList()
        }
}
