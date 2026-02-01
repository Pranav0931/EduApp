package com.hdaf.eduapp.presentation.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemLeaderboardBinding

/**
 * Adapter for displaying leaderboard entries.
 */
class LeaderboardAdapter(
    private val onItemClick: (LeaderboardEntry) -> Unit
) : ListAdapter<LeaderboardEntry, LeaderboardAdapter.ViewHolder>(LeaderboardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: LeaderboardEntry) {
            binding.apply {
                // Rank with special styling for top 3
                tvRank.text = "#${entry.rank}"
                when (entry.rank) {
                    1 -> {
                        tvRankBadge.text = "🥇"
                        tvRankBadge.visibility = android.view.View.VISIBLE
                        root.setCardBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.gold_light)
                        )
                    }
                    2 -> {
                        tvRankBadge.text = "🥈"
                        tvRankBadge.visibility = android.view.View.VISIBLE
                        root.setCardBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.silver_light)
                        )
                    }
                    3 -> {
                        tvRankBadge.text = "🥉"
                        tvRankBadge.visibility = android.view.View.VISIBLE
                        root.setCardBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.bronze_light)
                        )
                    }
                    else -> {
                        tvRankBadge.visibility = android.view.View.GONE
                        root.setCardBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.surface)
                        )
                    }
                }

                // Avatar
                if (entry.avatarUrl.isNotEmpty()) {
                    ivAvatar.load(entry.avatarUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_person)
                        error(R.drawable.ic_person)
                    }
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_person)
                }

                // User info
                tvUserName.text = entry.userName
                tvLevel.text = root.context.getString(R.string.level_format, entry.level)
                tvXp.text = "${entry.xp} XP"

                // Highlight current user
                if (entry.isCurrentUser) {
                    root.strokeWidth = root.resources.getDimensionPixelSize(R.dimen.card_stroke_selected)
                    root.strokeColor = ContextCompat.getColor(root.context, R.color.primary)
                } else {
                    root.strokeWidth = 0
                }

                root.setOnClickListener { onItemClick(entry) }

                // Accessibility
                root.contentDescription = buildString {
                    append("रैंक ${entry.rank}, ")
                    append(entry.userName)
                    append(", ${entry.xp} एक्सपी")
                    if (entry.isCurrentUser) append(", आप")
                }
            }
        }
    }

    private class LeaderboardDiffCallback : DiffUtil.ItemCallback<LeaderboardEntry>() {
        override fun areItemsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry) = 
            oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: LeaderboardEntry, newItem: LeaderboardEntry) = 
            oldItem == newItem
    }
}
