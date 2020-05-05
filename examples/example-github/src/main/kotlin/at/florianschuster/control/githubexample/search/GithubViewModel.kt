package at.florianschuster.control.githubexample.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.florianschuster.control.Controller
import at.florianschuster.control.ControllerEvent
import at.florianschuster.control.ControllerLog
import at.florianschuster.control.createController
import at.florianschuster.control.githubexample.GithubApi
import at.florianschuster.control.githubexample.Repo
import at.florianschuster.control.takeUntil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class GithubViewModel(
    initialState: GithubState = GithubState(),
    api: GithubApi = GithubApi(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    controller: GithubController = scope.createGithubController(initialState, api)
) : ViewModel(), GithubController by controller {

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

internal typealias GithubController = Controller<GithubAction, GithubMutation, GithubState>

internal sealed class GithubAction {
    data class UpdateQuery(val text: String) : GithubAction()
    object LoadNextPage : GithubAction()
}

internal sealed class GithubMutation {
    data class SetQuery(val query: String) : GithubMutation()
    data class SetRepos(val repos: List<Repo>) : GithubMutation()
    data class AppendRepos(val repos: List<Repo>) : GithubMutation()
    data class SetLoadingNextPage(val loading: Boolean) : GithubMutation()
}

internal data class GithubState(
    val query: String = "",
    val repos: List<Repo> = emptyList(),
    val page: Int = 1,
    val loadingNextPage: Boolean = false
)

internal fun CoroutineScope.createGithubController(
    initialState: GithubState,
    api: GithubApi
): GithubController = createController(
    initialState = initialState,

    mutator = { action ->
        when (action) {
            is GithubAction.UpdateQuery -> flow {
                emit(GithubMutation.SetQuery(action.text))
                if (action.text.isNotEmpty()) {
                    emit(GithubMutation.SetLoadingNextPage(true))
                    emitAll(
                        flow { emit(api.catchingSearch(currentState.query, 1)) }
                            .filterNotNull()
                            .map { GithubMutation.SetRepos(it) }
                            .takeUntil(actions.filterIsInstance<GithubAction.UpdateQuery>())
                    )
                    emit(GithubMutation.SetLoadingNextPage(false))
                }
            }
            is GithubAction.LoadNextPage -> when {
                currentState.loadingNextPage -> emptyFlow()
                else -> flow {
                    val state = currentState
                    emit(GithubMutation.SetLoadingNextPage(true))
                    emitAll(
                        flow { emit(api.catchingSearch(state.query, state.page + 1)) }
                            .filterNotNull()
                            .map { GithubMutation.AppendRepos(it) }
                            .takeUntil(actions.filterIsInstance<GithubAction.UpdateQuery>())
                    )
                    emit(GithubMutation.SetLoadingNextPage(false))
                }
            }
        }
    },

    reducer = { mutation, previousState ->
        when (mutation) {
            is GithubMutation.SetQuery -> previousState.copy(query = mutation.query)
            is GithubMutation.SetRepos -> previousState.copy(repos = mutation.repos, page = 1)
            is GithubMutation.AppendRepos -> previousState.copy(
                repos = previousState.repos + mutation.repos,
                page = previousState.page + 1
            )
            is GithubMutation.SetLoadingNextPage -> previousState.copy(loadingNextPage = mutation.loading)
        }
    },

    controllerLog = ControllerLog.Custom { message ->
        if (event is ControllerEvent.State) Log.d("GithubViewModel", message)
    }
)

private suspend fun GithubApi.catchingSearch(
    query: String,
    page: Int
): List<Repo>? = runCatching { repos(query, page).items }
    .onFailure { Log.e("GithubApi.repos", "with query = $query, page = $page", it) }
    .getOrNull()
