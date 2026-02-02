package com.hdaf.eduapp.presentation.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.ItemQuizBinding
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizDifficulty

/**
 * Adapter for displaying quizzes with full TalkBack accessibility.
 */
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
        holder.bind(getItem(position), position, itemCount)
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

        fun bind(quiz: Quiz, position: Int, total: Int) {
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
                val (difficultyText, difficultyHindi) = when (quiz.difficulty) {
                    QuizDifficulty.EASY -> Pair(R.string.difficulty_easy, "आसान")
                    QuizDifficulty.MEDIUM -> Pair(R.string.difficulty_medium, "मध्यम")
                    QuizDifficulty.HARD -> Pair(R.string.difficulty_hard, "कठिन")
                }
                chipDifficulty.setText(difficultyText)
                
                // Show AI badge if AI-generated
                chipAiGenerated.visibility = if (quiz.isAiGenerated) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                // Enhanced accessibility with position and full context
                val aiText = if (quiz.isAiGenerated) ", AI जनित" else ""
                root.contentDescription = buildString {
                    append("प्रश्नोत्तरी ${position + 1} में से $total: ")
                    append("${quiz.title}, ")
                    append("${quiz.questions.size} प्रश्न, ")
                    append("${quiz.timeLimitMinutes} मिनट, ")
                    append("कठिनाई: $difficultyHindi")
                    append(aiText)
                    append(". शुरू करने के लिए डबल टैप करें.")
                }
                
                // Set accessibility delegate for action labels
                ViewCompat.setAccessibilityDelegate(root, object : AccessibilityDelegateCompat() {
                    override fun onInitializeAccessibilityNodeInfo(
                        host: View,
                        info: AccessibilityNodeInfoCompat
                    ) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.addAction(
                            AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                                AccessibilityNodeInfoCompat.ACTION_CLICK,
                                "प्रश्नोत्तरी शुरू करें"
                            )
                        )
                    }
                })
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
