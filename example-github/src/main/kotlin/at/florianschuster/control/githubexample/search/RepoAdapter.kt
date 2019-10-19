package at.florianschuster.control.githubexample.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.githubexample.R
import at.florianschuster.control.githubexample.remote.Repo
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_repo.view.*

class RepoAdapter : ListAdapter<Repo, RepoViewHolder>(
    object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean = oldItem == newItem
    }
) {
    var onClick: (Repo) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder =
        RepoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_repo,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int): Unit =
        holder.bind(getItem(position), onClick)
}

class RepoViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun bind(repo: Repo, onClick: (Repo) -> Unit) {
        itemView.setOnClickListener { onClick(repo) }
        itemView.repoNameTextView.text = repo.name
    }
}
