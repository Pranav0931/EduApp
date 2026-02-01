package com.hdaf.eduapp.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemBadgeBinding
import com.hdaf.eduapp.domain.model.Badge

/**
 * Adapter for displaying user badges in a grid.
 */
class BadgeAdapter(
    private val onBadgeClick: (Badge) -> Unit
) : ListAdapter<Badge, BadgeAdapter.ViewHolder>(BadgeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBadgeBinding.inflate(
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
        private val binding: ItemBadgeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: Badge) {
            binding.apply {
                txtBadgeName.text = badge.name

                // Load badge icon
                if (badge.iconUrl.isNotEmpty()) {
                    imgBadgeIcon.load(badge.iconUrl) {
                        placeholder(R.drawable.ic_badge_placeholder)
                        error(R.drawable.ic_badge_placeholder)
                    }
                } else {
                    imgBadgeIcon.setImageResource(R.drawable.ic_badge_placeholder)
                }

                // Locked state
                imgBadgeIcon.alpha = if (badge.isUnlocked) 1.0f else 0.4f
                imgLockOverlay.visibility = if (badge.isUnlocked) android.view.View.GONE else android.view.View.VISIBLE

                root.setOnClickListener { onBadgeClick(badge) }

                // Accessibility
                root.contentDescription = buildString {
                    append(badge.name)
                    if (!badge.isUnlocked) append(", लॉक")
                }
            }
        }
    }

    private class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Badge, newItem: Badge) = oldItem == newItem
    }
}
