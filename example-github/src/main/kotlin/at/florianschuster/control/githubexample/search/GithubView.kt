package at.florianschuster.control.githubexample.search

import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.bind
import at.florianschuster.control.changesFrom
import at.florianschuster.control.githubexample.R
import kotlinx.android.synthetic.main.view_github.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

class GithubView : Fragment(R.layout.view_github) {
    private val controller: GithubControllerViewModel by viewModels { ControllerViewModelFactory }
    private val adapter = RepoAdapter()

    init {
        lifecycleScope.launchWhenCreated {
            repoRecyclerView.adapter = adapter
            repoRecyclerView.itemAnimator = null

            adapter.onClick = { startActivity(Intent(Intent.ACTION_VIEW, it.webUri)) }

            // action
            searchEditText.textChanges()
                .drop(1)
                .debounce(500)
                .map { it.toString() }
                .map { GithubController.Action.UpdateQuery(it) }
                .bind(to = controller.action)
                .launchIn(scope = lifecycleScope)

            repoRecyclerView.scrollEvents()
                .sample(500)
                .filter { it.view.shouldLoadMore() }
                .map { GithubController.Action.LoadNextPage }
                .bind(to = controller.action)
                .launchIn(scope = lifecycleScope)

            // state
            controller.state.changesFrom { it.repos }
                .bind(to = adapter::submitList)
                .launchIn(scope = lifecycleScope)

            controller.state.changesFrom { it.loadingNextPage }
                .map { if (it) View.VISIBLE else View.GONE }
                .bind(to = loadingProgressBar::setVisibility)
                .launchIn(scope = lifecycleScope)
        }
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }

    companion object {
        internal var ControllerViewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GithubControllerViewModel() as T
        }
    }
}
