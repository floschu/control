package at.florianschuster.control.githubexample.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.bind
import at.florianschuster.control.distinctMap
import at.florianschuster.control.githubexample.R
import kotlinx.android.synthetic.main.view_github.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

internal class GithubView : Fragment(R.layout.view_github) {

    private val viewModel: GithubViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repoAdapter = RepoAdapter { repo ->
            startActivity(Intent(Intent.ACTION_VIEW, repo.webUri))
        }

        with(repoRecyclerView) {
            adapter = repoAdapter
            itemAnimator = null
        }

        // action
        searchEditText.textChanges()
            .debounce(500)
            .map { it.toString() }
            .map { GithubAction.UpdateQuery(it) }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        repoRecyclerView.scrollEvents()
            .sample(500)
            .filter { it.view.shouldLoadMore() }
            .map { GithubAction.LoadNextPage }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.controller.state.distinctMap(by = GithubState::repos)
            .bind(to = repoAdapter::submitList)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        viewModel.controller.state.distinctMap(by = GithubState::loadingNextPage)
            .bind(to = loadingProgressBar::isVisible::set)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }
}
