package at.florianschuster.control.githubexample.search

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.bind
import at.florianschuster.control.changesFrom
import at.florianschuster.control.githubexample.R
import kotlinx.android.synthetic.main.activity_github.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import ru.ldralighieri.corbind.recyclerview.scrollEvents
import ru.ldralighieri.corbind.widget.textChanges

class GithubSearchActivity : AppCompatActivity(R.layout.activity_github) {
    private val controller: GithubController by viewModels()
    private val adapter = RepoAdapter()

    init {
        lifecycleScope.launchWhenCreated {
            repoRecyclerView.adapter = adapter
            adapter.onClick = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url))) }

            // action
            searchEditText.textChanges()
                .drop(1)
                .debounce(300)
                .map { it.toString() }
                .map { GithubController.Action.UpdateQuery(it) }
                .bind(to = controller.action)
                .launchIn(lifecycleScope)

            repoRecyclerView.scrollEvents()
                .sample(500)
                .filter { it.view.shouldLoadMore() }
                .map { GithubController.Action.LoadNextPage }
                .bind(to = controller.action)
                .launchIn(lifecycleScope)

            // state
            controller.state.changesFrom { it.repos }
                .bind(to = adapter::submitList)
                .launchIn(lifecycleScope)

            controller.state.changesFrom { it.loadingNextPage }
                .map { if (it) View.VISIBLE else View.GONE }
                .bind(to = loadingProgressBar::setVisibility)
                .launchIn(lifecycleScope)
        }
    }

    private fun RecyclerView.shouldLoadMore(threshold: Int = 8): Boolean {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return false
        return layoutManager.findLastVisibleItemPosition() + threshold > layoutManager.itemCount
    }
}
