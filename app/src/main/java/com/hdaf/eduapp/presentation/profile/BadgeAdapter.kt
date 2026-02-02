package com.hdaf.eduapp.presentation.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemBadgeBinding
import com.hdaf.eduapp.domain.model.Badge

/**
 * Adapter for displaying user badges in a grid.
 * Fully accessible with TalkBack support.
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
        holder.bind(getItem(position), position, itemCount)
    }

    inner class ViewHolder(
        private val binding: ItemBadgeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: Badge, position: Int, total: Int) {
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
                imgLockOverlay.visibility = if (badge.isUnlocked) View.GONE else View.VISIBLE

                root.setOnClickListener { onBadgeClick(badge) }

                // Enhanced accessibility with position info
                val statusText = if (badge.isUnlocked) "अनलॉक, प्राप्त किया" else "लॉक है"
                root.contentDescription = buildString {
                    append("बैज ${position + 1} में से $total: ")
                    append("${badge.name}, ")
                    append(statusText)
                    append(". ${badge.description}")
                    if (!badge.isUnlocked) {
                        append(". विवरण के लिए डबल टैप करें")
                    }
                }
                
                // Set accessibility delegate for better navigation
                ViewCompat.setAccessibilityDelegate(root, object : AccessibilityDelegateCompat() {
                    override fun onInitializeAccessibilityNodeInfo(
                        host: View,
                        info: AccessibilityNodeInfoCompat
                    ) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.addAction(
                            AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                                AccessibilityNodeInfoCompat.ACTION_CLICK,
                                "बैज विवरण देखें"
                            )
                        )
                        if (!badge.isUnlocked) {
                            info.stateDescription = "लॉक"
                        }
                    }
                })
            }
        }
    }

    private class BadgeDiffCallback : DiffUtil.ItemCallback<Badge>() {
        override fun areItemsTheSame(oldItem: Badge, newItem: Badge) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Badge, newItem: Badge) = oldItem == newItem
    }
}
