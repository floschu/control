package at.florianschuster.control.androidgithub.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.ControllerEvent
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.model.Repository
import at.florianschuster.control.createEffectController
import at.florianschuster.control.takeUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class SearchViewModel(
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
        data class SetRepos(val repos: List<Repository>) : Mutation()
        data class AppendRepos(val repos: List<Repository>) : Mutation()
        data class SetLoadingNextPage(val loading: Boolean) : Mutation()
    }

    data class State(
        val query: String = "",
        val repos: List<Repository> = emptyList(),
        val page: Int = 1,
        val loadingNextPage: Boolean = false
    )

    sealed class Effect {
        object NetworkError : Effect()
    }

    val controller = viewModelScope.createEffectController<Action, Mutation, State, Effect>(
        initialState = initialState,

        mutator = { action ->
            when (action) {
                is Action.UpdateQuery -> flow {
                    emit(Mutation.SetQuery(action.text))
                    if (action.text.isNotEmpty()) {
                        emit(Mutation.SetLoadingNextPage(true))

                        // flow search
                        emitAll(
                            flow { emit(api.search(currentState.query, 1)) }
                                .catch { error ->
                                    emitEffect(Effect.NetworkError)
                                    Log.w("GithubViewModel", error)
                                    emit(emptyList())
                                }
                                .filter { it.isNotEmpty() }
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

                        // suspending search
                        val repos = kotlin.runCatching {
                            api.search(state.query, state.page + 1)
                        }.getOrElse { error ->
                            emitEffect(Effect.NetworkError)
                            Log.w("GithubViewModel", error)
                            emptyList()
                        }
                        emit(Mutation.AppendRepos(repos))

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

    companion object {
        internal var Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = SearchViewModel() as T
        }
    }
}
