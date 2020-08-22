package at.florianschuster.control.androidgithub.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.bind
import at.florianschuster.control.distinctMap
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.androidgithub.databinding.ViewGithubBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import reactivecircus.flowbinding.android.widget.editorActionEvents
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.recyclerview.scrollEvents

internal class GithubView : Fragment(R.layout.view_github) {

    private var binding: ViewGithubBinding? = null
    private val requireBinding: ViewGithubBinding get() = requireNotNull(binding)

    private val viewModel: GithubViewModel by viewModels { GithubViewModelFactory }

    private val repoAdapter = RepoAdapter { repo ->
        startActivity(Intent(Intent.ACTION_VIEW, repo.webUri))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ViewGithubBinding.bind(view)

        with(requireBinding.repoRecyclerView) {
            adapter = repoAdapter
            itemAnimator = null
        }

        // action
        requireBinding.searchEditText.textChanges()
            .debounce(SearchDebounceMilliseconds)
            .map { it.toString() }
            .map { GithubViewModel.Action.UpdateQuery(it) }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        requireBinding.repoRecyclerView.scrollEvents()
            .sample(500)
            .filter { it.view.shouldLoadMore() }
            .map { GithubViewModel.Action.LoadNextPage }
            .bind(to = viewModel.controller::dispatch)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // state
        viewModel.controller.state.distinctMap(by = GithubViewModel.State::repos)
            .bind(to = repoAdapter::submitList)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        viewModel.controller.state.distinctMap(by = GithubViewModel.State::loadingNextPage)
            .bind(to = requireBinding.loadingProgressBar::isVisible::set)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)

        // effects
        viewModel.controller.effects
            .filterIsInstance<GithubViewModel.Effect.SearchError>()
            .map { R.string.error_network }
            .bind(to = ::showSnackbar)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 12): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }

    private fun showSnackbar(@StringRes messageResource: Int) {
        Snackbar.make(requireView(), messageResource, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val SearchDebounceMilliseconds = 500L

        internal var GithubViewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = GithubViewModel() as T
        }
    }
}
