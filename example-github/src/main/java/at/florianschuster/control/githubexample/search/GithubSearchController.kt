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

class GithubController(
    private val api: GithubApi = GithubApi.create(),
    override val initialState: State = State()
) : ControllerViewModel<Action, Mutation, State>() {

    override fun mutate(incomingAction: Action): Flow<Mutation> = when (incomingAction) {
        is Action.UpdateQuery -> flow {
            emit(Mutation.SetQuery(incomingAction.query))
            if (incomingAction.query.isEmpty()) return@flow
            emit(Mutation.SetLoadingNextPage(true))
            val repos = search(incomingAction.query, 1)
            emit(Mutation.SetRepos(repos))
            emit(Mutation.SetLoadingNextPage(false))
        }
        is Action.LoadNextPage -> {
            if (currentState.loadingNextPage) emptyFlow()
            else flow {
                emit(
                    Mutation.SetLoadingNextPage(
                        true
                    )
                )
                val searchResult = search(currentState.query, currentState.page + 1)
                emit(
                    Mutation.AppendRepos(
                        searchResult
                    )
                )
                emit(
                    Mutation.SetLoadingNextPage(
                        false
                    )
                )
            }
        }
    }

    override fun reduce(previousState: State, incomingMutation: Mutation): State =
        when (incomingMutation) {
            is Mutation.SetQuery -> previousState.copy(query = incomingMutation.query)
            is Mutation.SetRepos -> previousState.copy(repos = incomingMutation.repos, page = 1)
            is Mutation.AppendRepos -> previousState.copy(
                repos = previousState.repos + incomingMutation.repos,
                page = previousState.page + 1
            )
            is Mutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = incomingMutation.loading)
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
