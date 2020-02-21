package at.florianschuster.control.githubexample.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import at.florianschuster.control.Controller
import at.florianschuster.control.StateAccessor
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.createController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

internal class GithubViewModel(
    initialState: State = State(),
    private val api: GithubApi = GithubApi(),
    controllerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

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

    val controller: Controller<Action, Mutation, State> = viewModelScope.createController(
        initialState = initialState, mutator = ::mutate, reducer = ::reduce,

        // viewModelScope uses Dispatchers.Main, we do not want to run on Main
        dispatcher = controllerDispatcher,

        controllerLog = ControllerLog.Custom { Log.d(this::class.java.simpleName, it) }
    )

    private fun mutate(action: Action, stateAccessor: StateAccessor<State>): Flow<Mutation> =
        when (action) {
            is Action.UpdateQuery -> flow {
                emit(Mutation.SetQuery(action.text))

                if (action.text.isNotEmpty()) {
                    emit(Mutation.SetLoadingNextPage(true))

                    val repos = api.safeSearch(stateAccessor().query, 1)
                    if (repos != null) emit(Mutation.SetRepos(repos))

                    emit(Mutation.SetLoadingNextPage(false))
                }
            }
            is Action.LoadNextPage -> when {
                stateAccessor().loadingNextPage -> emptyFlow()
                else -> flow {
                    val state = stateAccessor()

                    emit(Mutation.SetLoadingNextPage(true))

                    val repos = api.safeSearch(state.query, state.page + 1)
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

    private suspend fun GithubApi.safeSearch(
        query: String,
        page: Int
    ): List<Repo>? = runCatching { repos(query, page).items }
        .onFailure { Log.e("GithubApi.repos", "with query = $query, page = $page", it) }
        .getOrNull()
}