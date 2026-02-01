package com.hdaf.eduapp.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hdaf.eduapp.databinding.ItemRecentChapterBinding
import com.hdaf.eduapp.domain.model.Chapter

/**
 * Adapter for displaying recently accessed chapters in a vertical list.
 * Shows chapter progress, book name, and allows continuing from where the user left off.
 */
class RecentChapterAdapter(
    private val onChapterClick: (Chapter) -> Unit,
    private val onContinueClick: (Chapter) -> Unit
) : ListAdapter<Chapter, RecentChapterAdapter.ViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentChapterBinding.inflate(
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
        private val binding: ItemRecentChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: Chapter) {
            binding.apply {
                tvChapterTitle.text = chapter.title
                tvBookTitle.text = chapter.bookTitle ?: ""
                
                // Set progress
                val progress = chapter.progress
                progressChapter.progress = progress
                tvProgress.text = "${progress}% पूर्ण"
                
                // Load chapter image if available
                chapter.thumbnailUrl?.let { url ->
                    ivChapter.load(url) {
                        crossfade(true)
                    }
                }
                
                // Click listeners
                root.setOnClickListener { onChapterClick(chapter) }
                btnContinue.setOnClickListener { onContinueClick(chapter) }
                
                // Accessibility
                root.contentDescription = buildString {
                    append(chapter.title)
                    append(", ")
                    append(chapter.bookTitle ?: "")
                    append(", ")
                    append("$progress प्रतिशत पूर्ण")
                }
            }
        }
    }

    private class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem == newItem
        }
    }
}
