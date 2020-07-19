package at.florianschuster.control.androidgithub.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.androidgithub.Repo
import at.florianschuster.control.androidgithub.databinding.ItemRepoBinding

internal class RepoAdapter(
    private val onItemClick: (Repo) -> Unit
) : ListAdapter<Repo, RepoViewHolder>(
    object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepoViewHolder = RepoViewHolder(
        ItemRepoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(
        holder: RepoViewHolder,
        position: Int
    ): Unit = holder.bind(getItem(position), onItemClick)
}

internal class RepoViewHolder(
    private val binding: ItemRepoBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(repo: Repo, onItemClick: (Repo) -> Unit) {
        binding.root.setOnClickListener { onItemClick(repo) }
        binding.repoNameTextView.text = repo.name
        with(binding.repoDescriptionTextView) {
            isVisible = repo.description != null
            text = repo.description
        }
    }
}
