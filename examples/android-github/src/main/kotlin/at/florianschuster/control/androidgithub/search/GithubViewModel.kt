package at.florianschuster.control.androidgithub.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.ControllerEvent
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.Repo
import at.florianschuster.control.createEffectController
import at.florianschuster.control.takeUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

    sealed class Effect {
        object SearchError : Effect()
    }

    val controller = viewModelScope.createEffectController<Action, Mutation, State, Effect>(
        initialState = initialState,

        mutator = { action ->
            when (action) {
                is Action.UpdateQuery -> flow {
                    emit(Mutation.SetQuery(action.text))
                    if (action.text.isNotEmpty()) {
                        emit(Mutation.SetLoadingNextPage(true))
                        emitAll(
                            flow { emit(api.search(currentState.query, 1)) }
                                .catch { error ->
                                    Log.w("GithubViewModel.Query", error)
                                    emitEffect(Effect.SearchError)
                                }
                                .map { Mutation.SetRepos(it) }
                                .takeUntil(actions.filterIsInstance<Action.UpdateQuery>())
                        )
                        emit(Mutation.SetLoadingNextPage(false))
                    }
                }
                is Action.LoadNextPage -> when {
                    currentState.loadingNextPage -> emptyFlow()
                    else -> flow {
                        val state = currentState
                        emit(Mutation.SetLoadingNextPage(true))
                        emitAll(
                            flow { emit(api.search(state.query, state.page + 1)) }
                                .catch { error ->
                                    Log.w("GithubViewModel.Query", error)
                                    emitEffect(Effect.SearchError)
                                }
                                .map { Mutation.AppendRepos(it) }
                                .takeUntil(actions.filterIsInstance<Action.UpdateQuery>())
                        )
                        emit(Mutation.SetLoadingNextPage(false))
                    }
                }
            }
        },

        reducer = { mutation, previousState ->
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

        controllerLog = ControllerLog.Custom { message ->
            if (event is ControllerEvent.State) Log.d("GithubViewModel", message)
        }
    )
}
