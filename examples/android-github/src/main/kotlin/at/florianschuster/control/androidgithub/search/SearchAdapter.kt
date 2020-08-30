package at.florianschuster.control.androidgithub.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.control.androidgithub.R
import at.florianschuster.control.androidgithub.databinding.ItemRepoBinding
import at.florianschuster.control.androidgithub.model.Repository
import coil.load
import coil.transform.RoundedCornersTransformation

internal class SearchAdapter(
    private val onItemClick: (Repository) -> Unit
) : ListAdapter<Repository, RepoViewHolder>(
    object : DiffUtil.ItemCallback<Repository>() {
        override fun areItemsTheSame(oldItem: Repository, newItem: Repository): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Repository, newItem: Repository): Boolean =
            oldItem == newItem
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

    private val resources = itemView.resources

    fun bind(repo: Repository, onItemClick: (Repository) -> Unit) {
        binding.root.setOnClickListener { onItemClick(repo) }
        binding.ownerIconImageView.load(repo.owner.avatarUrl) {
            crossfade(true)
            transformations(
                RoundedCornersTransformation(
                    resources.getDimensionPixelSize(R.dimen.dimen_8).toFloat()
                )
            )
        }
        binding.repoNameTextView.text = repo.fullName
        with(binding.repoDescriptionTextView) {
            isVisible = repo.description != null
            text = repo.description
        }
        binding.repoLastUpdatedTextView.text = resources.getString(
            R.string.label_last_updated,
            repo.lastUpdated
        )
    }
}
