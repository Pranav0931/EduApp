package com.hdaf.eduapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.models.QuizModel;

import java.util.List;

/**
 * Adapter for displaying quiz list items.
 */
public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private List<QuizModel> quizzes;
    private OnQuizClickListener listener;

    public interface OnQuizClickListener {
        void onQuizClick(QuizModel quiz, int position);
    }

    public QuizListAdapter(List<QuizModel> quizzes, OnQuizClickListener listener) {
        this.quizzes = quizzes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_java, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizModel quiz = quizzes.get(position);
        holder.bind(quiz, position);
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvQuestionCount;
        private TextView tvDuration;
        private ImageView ivAiBadge;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvDescription = itemView.findViewById(R.id.tvQuizDescription);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            ivAiBadge = itemView.findViewById(R.id.ivAiBadge);
        }

        void bind(QuizModel quiz, int position) {
            tvTitle.setText(quiz.getTitle());
            tvDescription.setText(quiz.getDescription());
            tvQuestionCount.setText(quiz.getQuestionCount() + " Questions");
            tvDuration.setText(quiz.getDurationMinutes() + " min");
            ivAiBadge.setVisibility(quiz.isAiGenerated() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuizClick(quiz, position);
                }
            });
        }
    }
}
