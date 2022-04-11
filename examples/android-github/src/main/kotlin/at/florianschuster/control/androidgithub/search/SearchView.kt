package at.florianschuster.control.androidgithub.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.androidgithub.databinding.ViewSearchBinding
import at.florianschuster.control.androidgithub.viewBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

internal class SearchView : Fragment(R.layout.view_search) {

    private val binding by viewBinding(ViewSearchBinding::bind)
    private val viewModel by viewModels<SearchViewModel> { SearchViewModel.Factory }

    private val searchAdapter: SearchAdapter?
        get() = binding.repoRecyclerView.adapter as? SearchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.repoRecyclerView) {
            adapter = SearchAdapter { repo ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.webUrl)))
            }
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bindViewModel()
            }
        }
    }

    private fun CoroutineScope.bindViewModel() {
        // action
        binding.searchEditText.textChanges()
            .debounce(SearchDebounceMilliseconds)
            .map { it.toString() }
            .map { SearchAction.UpdateQuery(it) }
            .onEach { viewModel.controller.dispatch(it) }
            .launchIn(scope = this)

        binding.repoRecyclerView.scrollEvents()
            .sample(500)
            .filter { it.view.shouldLoadMore() }
            .map { SearchAction.LoadNextPage }
            .onEach { viewModel.controller.dispatch(it) }
            .launchIn(scope = this)

        // state
        viewModel.controller.state.onEach { state ->
            binding.loadingProgressBar.isVisible = state.loadingNextPage
            searchAdapter?.submitList(state.repos)
        }.launchIn(scope = this)

        // effect
        viewModel.controller.effects.onEach { effect ->
            when (effect) {
                is SearchEffect.NotifyNetworkError -> {
                    Snackbar
                        .make(binding.root, R.string.info_network_error, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }.launchIn(scope = this)
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }

    companion object {
        const val SearchDebounceMilliseconds = 500L
    }
}
