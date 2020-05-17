package at.florianschuster.control.androidgithub.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.androidgithub.Repo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_repo.view.*

internal class RepoAdapter(
    private val onItemClick: (Repo) -> Unit
) : ListAdapter<Repo, RepoViewHolder>(
    object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder =
        RepoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_repo,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int): Unit =
        holder.bind(getItem(position), onItemClick)
}

internal class RepoViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(repo: Repo, onItemClick: (Repo) -> Unit) {
        itemView.setOnClickListener { onItemClick(repo) }
        itemView.repoNameTextView.text = repo.name
        with(itemView.repoDescriptionTextView) {
            isVisible = repo.description != null
            repoDescriptionTextView.text = repo.description
        }
    }
}
