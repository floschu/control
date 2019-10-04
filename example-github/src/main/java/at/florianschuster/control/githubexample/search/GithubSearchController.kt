package at.florianschuster.control.githubexample.search

import at.florianschuster.control.android.ControllerViewModel
import at.florianschuster.control.githubexample.remote.GithubApi
import at.florianschuster.control.githubexample.remote.Repo
import java.lang.Exception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class GithubController(
    private val api: GithubApi = GithubApi.create()
) : ControllerViewModel<GithubController.Action, GithubController.Mutation, GithubController.State>() {

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
            val repos = search(action.query, 1)
            emit(Mutation.SetRepos(repos))
            emit(Mutation.SetLoadingNextPage(false))
        }
        is Action.LoadNextPage -> {
            if (currentState.loadingNextPage) emptyFlow()
            else flow {
                emit(Mutation.SetLoadingNextPage(true))
                val searchResult = search(currentState.query, currentState.page + 1)
                emit(Mutation.AppendRepos(searchResult))
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

    private suspend fun search(query: String, page: Int) = withContext(Dispatchers.IO) {
        try {
            api.repos(query, page).items
        } catch (e: Exception) {
            println("Search Error: $e")
            emptyList<Repo>()
        }
    }
}
