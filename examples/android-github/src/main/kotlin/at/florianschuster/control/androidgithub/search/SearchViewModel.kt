package at.florianschuster.control.androidgithub.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.ControllerEvent
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.EffectController
import at.florianschuster.control.androidgithub.GithubApi
import at.florianschuster.control.androidgithub.model.Repository
import at.florianschuster.control.createEffectController
import at.florianschuster.control.takeUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class SearchViewModel(
    api: GithubApi = GithubApi(),
) : ViewModel() {

    val controller = viewModelScope.createSearchController(
        initialState = SearchState(),
        api = api
    )

    companion object {
        internal var Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel() as T
        }
    }
}

internal sealed interface SearchAction {
    data class UpdateQuery(val text: String) : SearchAction
    data object LoadNextPage : SearchAction
}

private sealed interface SearchMutation {
    data class SetQuery(val query: String) : SearchMutation
    data class SetRepos(val repos: List<Repository>) : SearchMutation
    data class AppendRepos(val repos: List<Repository>) : SearchMutation
    data class SetLoadingNextPage(val loading: Boolean) : SearchMutation
}

internal data class SearchState(
    val query: String = "",
    val repos: List<Repository> = emptyList(),
    val page: Int = 1,
    val loadingNextPage: Boolean = false
)

internal sealed interface SearchEffect {
    data object NotifyNetworkError : SearchEffect
}

internal fun CoroutineScope.createSearchController(
    initialState: SearchState,
    api: GithubApi
): EffectController<SearchAction, SearchState, SearchEffect> = createEffectController(
    initialState = initialState,

    mutator = { action ->
        when (action) {
            is SearchAction.UpdateQuery -> flow {
                emit(SearchMutation.SetQuery(action.text))

                if (action.text.isNotEmpty()) {
                    emit(SearchMutation.SetLoadingNextPage(true))

                    emitAll(
                        flow { emit(api.search(action.text, 1)) }
                            .catch { error ->
                                emitEffect(SearchEffect.NotifyNetworkError)
                                Log.w("GithubViewModel", error)
                                emit(emptyList())
                            }
                            .filter { repos -> repos.isNotEmpty() }
                            .map { repos -> SearchMutation.SetRepos(repos) }
                            .takeUntil(actions.filterIsInstance<SearchAction.UpdateQuery>())
                    )

                    emit(SearchMutation.SetLoadingNextPage(false))
                }
            }
            is SearchAction.LoadNextPage -> when {
                currentState.loadingNextPage -> emptyFlow()
                else -> flow {
                    emit(SearchMutation.SetLoadingNextPage(true))

                    val repos = kotlin.runCatching {
                        api.search(currentState.query, currentState.page + 1)
                    }.getOrElse { error ->
                        emitEffect(SearchEffect.NotifyNetworkError)
                        Log.w("GithubViewModel", error)
                        emptyList()
                    }
                    emit(SearchMutation.AppendRepos(repos))

                    emit(SearchMutation.SetLoadingNextPage(false))
                }
            }
        }
    },

    reducer = { mutation, previousState ->
        when (mutation) {
            is SearchMutation.SetQuery -> previousState.copy(
                query = mutation.query
            )
            is SearchMutation.SetRepos -> previousState.copy(
                repos = mutation.repos,
                page = 1
            )
            is SearchMutation.AppendRepos -> previousState.copy(
                repos = previousState.repos + mutation.repos,
                page = previousState.page + 1
            )
            is SearchMutation.SetLoadingNextPage -> previousState.copy(
                loadingNextPage = mutation.loading
            )
        }
    },

    controllerLog = ControllerLog.Custom { message ->
        if (event is ControllerEvent.State) Log.d("GithubViewModel", message)
    }
)
