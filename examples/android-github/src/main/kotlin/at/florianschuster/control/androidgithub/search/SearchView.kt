package at.florianschuster.control.androidgithub.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.androidgithub.databinding.ViewSearchBinding
import at.florianschuster.control.androidgithub.showSnackBar
import at.florianschuster.control.androidgithub.viewBinding
import at.florianschuster.control.bind
import at.florianschuster.control.distinctMap
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

internal class SearchView : Fragment(R.layout.view_search) {

    private val binding by viewBinding(ViewSearchBinding::bind)
    private val viewModel by viewModels<SearchViewModel> { SearchViewModel.Factory }

    private val repoAdapter = SearchAdapter { repo ->
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.webUrl)))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.repoRecyclerView) {
            adapter = repoAdapter
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }

        // action
        binding.searchEditText.textChanges()
            .debounce(SearchDebounceMilliseconds)
            .map { it.toString() }
            .map { SearchViewModel.Action.UpdateQuery(it) }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        binding.repoRecyclerView.scrollEvents()
            .sample(500)
            .filter { it.view.shouldLoadMore() }
            .map { SearchViewModel.Action.LoadNextPage }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.controller.state.distinctMap(by = SearchViewModel.State::repos)
            .bind(to = repoAdapter::submitList)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        viewModel.controller.state.distinctMap(by = SearchViewModel.State::loadingNextPage)
            .bind(to = binding.loadingProgressBar::isVisible::set)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // effect
        viewModel.controller.effects.onEach { effect ->
            when (effect) {
                is SearchViewModel.Effect.NetworkError -> {
                    binding.root.showSnackBar(R.string.info_network_error)
                }
            }
        }.launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }

    companion object {
        const val SearchDebounceMilliseconds = 500L
    }
}
