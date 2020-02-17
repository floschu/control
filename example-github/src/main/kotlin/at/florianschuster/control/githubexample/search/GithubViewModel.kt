package at.florianschuster.control.githubexample.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import at.florianschuster.control.Controller
import at.florianschuster.control.store.Store
import at.florianschuster.control.store.StoreLogger
import at.florianschuster.control.store.createStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

internal class GithubViewModel(
    initialState: State = State(),
    private val api: GithubApi = GithubApi(),
    storeDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(), Controller<GithubViewModel.Action, GithubViewModel.State> {

    sealed class Action {
        data class UpdateQuery(val text: String) : Action()
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

    override val store: Store<Action, Mutation, State> = viewModelScope.createStore(

        // viewModelScope uses Dispatchers.Main, we do not want to run on Main
        dispatcher = storeDispatcher,

        initialState = initialState,
        mutator = ::mutate,
        reducer = ::reduce,

        tag = "github_vm",
        storeLogger = StoreLogger.Custom { tag, message -> Log.d(tag, message) }
    )

    private fun mutate(action: Action): Flow<Mutation> = when (action) {
        is Action.UpdateQuery -> flow {
            emit(Mutation.SetQuery(action.text))
            if (action.text.isNotEmpty()) {
                emit(Mutation.SetLoadingNextPage(true))
                val repos = api.safeSearch(currentState.query, 1)
                if (repos != null) emit(Mutation.SetRepos(repos))
                emit(Mutation.SetLoadingNextPage(false))
            }
        }
        is Action.LoadNextPage -> {
            if (currentState.loadingNextPage) emptyFlow()
            else flow {
                emit(Mutation.SetLoadingNextPage(true))
                val repos = api.safeSearch(currentState.query, currentState.page + 1)
                if (repos != null) emit(Mutation.AppendRepos(repos))
                emit(Mutation.SetLoadingNextPage(false))
            }
        }
    }

    private fun reduce(previousState: State, mutation: Mutation): State = when (mutation) {
        is Mutation.SetQuery -> previousState.copy(query = mutation.query)
        is Mutation.SetRepos -> previousState.copy(repos = mutation.repos, page = 1)
        is Mutation.AppendRepos -> previousState.copy(
            repos = previousState.repos + mutation.repos,
            page = previousState.page + 1
        )
        is Mutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = mutation.loading)
    }
}

private suspend fun GithubApi.safeSearch(
    query: String,
    page: Int
): List<Repo>? = runCatching { repos(query, page).items }
    .onFailure { Log.e("GithubApi.repos", "with query = $query, page = $page", it) }
    .getOrNull()