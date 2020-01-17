package at.florianschuster.control.githubexample.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.Proxy
import at.florianschuster.control.LogConfiguration
import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

sealed class GithubAction {
    data class UpdateQuery(val text: String) : GithubAction()
    object LoadNextPage : GithubAction()
}

data class GithubState(
    val query: String = "",
    val repos: List<Repo> = emptyList(),
    val page: Int = 1,
    val loadingNextPage: Boolean = false
)

class GithubViewModel(
    initialState: GithubState = GithubState(),
    private val api: GithubApi = GithubApi()
) : ViewModel(), Proxy<GithubAction, GithubState> {

    sealed class Mutation {
        data class SetQuery(val query: String) : Mutation()
        data class SetRepos(val repos: List<Repo>) : Mutation()
        data class AppendRepos(val repos: List<Repo>) : Mutation()
        data class SetLoadingNextPage(val loading: Boolean) : Mutation()
    }

    override val controller: Controller<GithubAction, Mutation, GithubState> = Controller(
        initialState = initialState,
        scope = viewModelScope,
        mutator = ::mutate,
        reducer = ::reduce,
        logConfiguration = LogConfiguration.Custom(tag = "GithubViewModel", logger = ::println)
    )

    private fun mutate(action: GithubAction): Flow<Mutation> = when (action) {
        is GithubAction.UpdateQuery -> flow {
            emit(Mutation.SetQuery(action.text))
            if (action.text.isNotEmpty()) {
                emit(Mutation.SetLoadingNextPage(true))
                val repos = search(currentState.query, 1)
                if (repos != null) emit(Mutation.SetRepos(repos))
                emit(Mutation.SetLoadingNextPage(false))
            }
        }
        is GithubAction.LoadNextPage -> {
            if (currentState.loadingNextPage) emptyFlow()
            else flow {
                emit(Mutation.SetLoadingNextPage(true))
                val repos = search(currentState.query, currentState.page + 1)
                if (repos != null) emit(Mutation.AppendRepos(repos))
                emit(Mutation.SetLoadingNextPage(false))
            }
        }
    }

    private fun reduce(previousState: GithubState, mutation: Mutation): GithubState =
        when (mutation) {
            is Mutation.SetQuery -> previousState.copy(query = mutation.query)
            is Mutation.SetRepos -> previousState.copy(repos = mutation.repos, page = 1)
            is Mutation.AppendRepos -> previousState.copy(
                repos = previousState.repos + mutation.repos,
                page = previousState.page + 1
            )
            is Mutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = mutation.loading)
        }

    private suspend fun search(query: String, page: Int): List<Repo>? = try {
        api.repos(query, page).items
    } catch (e: Exception) {
        println("Search Error: $e")
        null
    }
}