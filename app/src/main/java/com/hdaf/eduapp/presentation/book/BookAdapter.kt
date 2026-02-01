package com.hdaf.eduapp.presentation.book

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemBookGridBinding
import com.hdaf.eduapp.domain.model.Book

/**
 * Adapter for displaying books in a grid layout.
 */
class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onDownloadClick: (Book) -> Unit
) : ListAdapter<Book, BookAdapter.ViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookGridBinding.inflate(
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
        private val binding: ItemBookGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                tvBookTitle.text = book.title
                tvSubject.text = book.subject
                tvProgress.text = "${book.progress}%"
                progressBook.progress = book.progress

                // Load cover image
                book.coverUrl?.let { url ->
                    ivBookCover.load(url) {
                        crossfade(true)
                        placeholder(R.drawable.ic_book)
                        error(R.drawable.ic_book)
                    }
                } ?: ivBookCover.setImageResource(R.drawable.ic_book)

                // Download indicator
                ivDownload.setImageResource(
                    if (book.isDownloaded) R.drawable.ic_downloaded
                    else R.drawable.ic_download
                )

                // Click listeners
                root.setOnClickListener { onBookClick(book) }
                ivDownload.setOnClickListener { 
                    if (!book.isDownloaded) onDownloadClick(book) 
                }

                // Accessibility
                root.contentDescription = buildString {
                    append(book.title)
                    append(", ")
                    append(book.subject)
                    append(", ")
                    append("${book.progress} प्रतिशत पूर्ण")
                    if (book.isDownloaded) append(", ऑफ़लाइन उपलब्ध")
                }
            }
        }
    }

    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
