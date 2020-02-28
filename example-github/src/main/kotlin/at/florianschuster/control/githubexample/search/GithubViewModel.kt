package at.florianschuster.control.githubexample.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.ComplexMutator
import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.createController
import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import at.florianschuster.control.takeUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
        initialState = initialState,

        mutator = ComplexMutator { action, stateAccessor, actionFlow ->
            when (action) {
                is Action.UpdateQuery -> flow {
                    emit(Mutation.SetQuery(action.text))

                    if (action.text.isNotEmpty()) {
                        emit(Mutation.SetLoadingNextPage(true))

                        val repos = api.search(stateAccessor().query, 1)
                            .map { Mutation.SetRepos(it) }
                            .takeUntil(actionFlow.filterIsInstance<Action.UpdateQuery>())
                        emitAll(repos)

                        emit(Mutation.SetLoadingNextPage(false))
                    }
                }
                is Action.LoadNextPage -> when {
                    stateAccessor().loadingNextPage -> emptyFlow()
                    else -> flow {
                        val state = stateAccessor()

                        emit(Mutation.SetLoadingNextPage(true))

                        val repos = api.search(state.query, state.page + 1)
                            .map { Mutation.AppendRepos(it) }
                            .takeUntil(actionFlow.filterIsInstance<Action.UpdateQuery>())
                        emitAll(repos)

                        emit(Mutation.SetLoadingNextPage(false))
                    }
                }
            }
        },

        reducer = { previousState, mutation ->
            when (mutation) {
                is Mutation.SetQuery -> previousState.copy(query = mutation.query)
                is Mutation.SetRepos -> previousState.copy(repos = mutation.repos, page = 1)
                is Mutation.AppendRepos -> previousState.copy(
                    repos = previousState.repos + mutation.repos,
                    page = previousState.page + 1
                )
                is Mutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = mutation.loading)
            }
        },

        // viewModelScope uses Dispatchers.Main, we do not want to run on Main
        dispatcher = controllerDispatcher,

        controllerLog = ControllerLog.Custom { Log.d(this::class.java.simpleName, it) }
    )

    private suspend fun GithubApi.search(query: String, page: Int): Flow<List<Repo>> = flow {
        val repos = repos(query, page)
        emit(repos.items)
    }.catch { e -> Log.e("GithubApi.repos", "with query = $query, page = $page", e) }
}