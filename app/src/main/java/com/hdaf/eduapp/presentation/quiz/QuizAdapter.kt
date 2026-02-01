package com.hdaf.eduapp.presentation.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemQuizBinding
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizDifficulty

class QuizAdapter(
    private val onQuizClick: (Quiz) -> Unit
) : ListAdapter<Quiz, QuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onQuizClick(getItem(position))
                }
            }
        }

        fun bind(quiz: Quiz) {
            binding.apply {
                tvQuizTitle.text = quiz.title
                tvQuestionCount.text = root.context.getString(
                    R.string.question_count_format,
                    quiz.questions.size
                )
                tvDuration.text = root.context.getString(
                    R.string.duration_format,
                    quiz.timeLimitMinutes
                )
                
                // Set difficulty indicator
                val difficultyText = when (quiz.difficulty) {
                    QuizDifficulty.EASY -> R.string.difficulty_easy
                    QuizDifficulty.MEDIUM -> R.string.difficulty_medium
                    QuizDifficulty.HARD -> R.string.difficulty_hard
                }
                chipDifficulty.setText(difficultyText)
                
                // Show AI badge if AI-generated
                chipAiGenerated.visibility = if (quiz.isAiGenerated) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                // Accessibility
                root.contentDescription = root.context.getString(
                    R.string.quiz_item_description,
                    quiz.title,
                    quiz.questions.size,
                    quiz.timeLimitMinutes
                )
            }
        }
    }

    class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
}
