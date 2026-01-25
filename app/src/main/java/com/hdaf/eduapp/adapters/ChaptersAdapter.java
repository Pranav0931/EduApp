package com.hdaf.eduapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.models.ChapterModel;

import java.util.List;

/**
 * RecyclerView adapter for displaying chapters list.
 */
public class ChaptersAdapter extends RecyclerView.Adapter<ChaptersAdapter.ChapterViewHolder> {

    private final List<ChapterModel> chapters;
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(ChapterModel chapter, int position);
    }

    public ChaptersAdapter(List<ChapterModel> chapters, OnChapterClickListener listener) {
        this.chapters = chapters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        ChapterModel chapter = chapters.get(position);
        holder.bind(chapter, position);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView chapterName;

        ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapterName);
        }

        void bind(ChapterModel chapter, int position) {
            // Format: "1. UNIT 1"
            String displayText = (position + 1) + ". " + chapter.getName();
            chapterName.setText(displayText);

            // Set content description for accessibility
            itemView.setContentDescription(
                    itemView.getContext().getString(R.string.chapter_item_description, chapter.getName()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChapterClick(chapter, position);
                }
            });
        }
    }
}
