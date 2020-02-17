package at.florianschuster.control.githubexample.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.bind
import at.florianschuster.control.githubexample.R
import kotlinx.android.synthetic.main.view_github.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

class GithubView : Fragment(R.layout.view_github) {

    private val viewModel: GithubViewModel by viewModels { GithubViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repoAdapter = RepoAdapter().apply {
            onClick = { startActivity(Intent(Intent.ACTION_VIEW, it.webUri)) }
        }

        with(repoRecyclerView) {
            adapter = repoAdapter
            itemAnimator = null
        }

        // action
        searchEditText.textChanges()
            .debounce(500)
            .map { it.toString() }
            .map { GithubViewModel.Action.UpdateQuery(it) }
            .bind(to = viewModel::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        repoRecyclerView.scrollEvents()
            .sample(500)
            .filter { it.view.shouldLoadMore() }
            .map { GithubViewModel.Action.LoadNextPage }
            .bind(to = viewModel::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.state.map { it.repos }
            .distinctUntilChanged()
            .bind(to = repoAdapter::submitList)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        viewModel.state.map { it.loadingNextPage }
            .distinctUntilChanged()
            .map { if (it) View.VISIBLE else View.GONE }
            .bind(to = loadingProgressBar::setVisibility)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }

    companion object {
        internal var GithubViewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GithubViewModel() as T
        }
    }
}
