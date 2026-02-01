package com.hdaf.eduapp.presentation.chapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hdaf.eduapp.databinding.ItemChapterBinding
import com.hdaf.eduapp.domain.model.Chapter

/**
 * Adapter for displaying chapters in a list.
 */
class ChapterAdapter(
    private val onChapterClick: (Chapter) -> Unit,
    private val onDownloadClick: (Chapter) -> Unit
) : ListAdapter<Chapter, ChapterAdapter.ViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: Chapter, position: Int) {
            binding.apply {
                // Use the actual view ID from item_chapter.xml
                chapterName.text = "$position. ${chapter.title}"

                root.setOnClickListener { onChapterClick(chapter) }

                // Accessibility
                root.contentDescription = buildString {
                    append("अध्याय $position, ")
                    append(chapter.title)
                    append(", ${chapter.progress} प्रतिशत पूर्ण")
                    if (chapter.isCompleted) append(", पूर्ण")
                    if (chapter.isDownloaded) append(", ऑफ़लाइन उपलब्ध")
                }
            }
        }
    }

    private class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter) = oldItem == newItem
    }
}
