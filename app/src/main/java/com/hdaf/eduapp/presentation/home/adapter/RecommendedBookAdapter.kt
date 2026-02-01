package com.hdaf.eduapp.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hdaf.eduapp.databinding.ItemBookCardBinding
import com.hdaf.eduapp.domain.model.Book

/**
 * Adapter for displaying recommended books in a horizontal scrollable list.
 * Shows book cover, title, class level, and completion progress.
 */
class RecommendedBookAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, RecommendedBookAdapter.ViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookCardBinding.inflate(
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
        private val binding: ItemBookCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                tvBookTitle.text = book.title
                tvClass.text = "कक्षा ${book.classLevel}"
                
                // Set progress
                val progress = book.progress
                progressBook.progress = progress
                tvProgress.text = "${progress}%"
                
                // Load book cover if available
                book.coverUrl?.let { url ->
                    ivBookCover.load(url) {
                        crossfade(true)
                    }
                }
                
                // Click listener
                root.setOnClickListener { onBookClick(book) }
                
                // Accessibility
                root.contentDescription = buildString {
                    append(book.title)
                    append(", कक्षा ")
                    append(book.classLevel)
                    append(", ")
                    append("$progress प्रतिशत पूर्ण")
                }
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}
