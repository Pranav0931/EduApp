package com.hdaf.eduapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.models.BookModel;

import java.util.List;

/**
 * RecyclerView adapter for displaying books list.
 */
public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private final List<BookModel> books;
    private final OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(BookModel book, int position);
    }

    public BooksAdapter(List<BookModel> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookModel book = books.get(position);
        holder.bind(book, position);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final TextView bookName;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            bookName = itemView.findViewById(R.id.bookName);
        }

        void bind(BookModel book, int position) {
            // Format: "1. BOOK NAME"
            String displayText = (position + 1) + ". " + book.getName();
            bookName.setText(displayText);

            // Set content description for accessibility
            itemView.setContentDescription(
                    itemView.getContext().getString(R.string.book_item_description, book.getName()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book, position);
                }
            });
        }
    }
}
