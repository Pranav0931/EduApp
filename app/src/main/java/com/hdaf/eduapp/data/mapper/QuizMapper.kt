package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.local.entity.QuizAttemptEntity
import com.hdaf.eduapp.data.local.entity.QuizEntity
import com.hdaf.eduapp.data.local.entity.QuizQuestionEntity
import com.hdaf.eduapp.data.remote.dto.QuizAttemptDto
import com.hdaf.eduapp.data.remote.dto.QuizDto
import com.hdaf.eduapp.data.remote.dto.QuizQuestionDto
import com.hdaf.eduapp.domain.model.Quiz
import com.hdaf.eduapp.domain.model.QuizAttempt
import com.hdaf.eduapp.domain.model.QuizDifficulty
import com.hdaf.eduapp.domain.model.QuizQuestion
import com.hdaf.eduapp.domain.model.Subject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Injectable wrapper class for Quiz mapping functions.
 */
class QuizMapper @Inject constructor() {
    
    fun quizDtoToDomain(dto: QuizDto): Quiz = dto.toDomain()
    
    fun quizDtoToEntity(dto: QuizDto): QuizEntity = dto.toEntity()
    
    fun quizEntityToDomain(entity: QuizEntity, questions: List<QuizQuestion> = emptyList()): Quiz = 
        entity.toDomain(questions)
    
    fun quizToEntity(quiz: Quiz): QuizEntity = quiz.toEntity()
    
    fun questionDtoToDomain(dto: QuizQuestionDto): QuizQuestion = dto.toDomain()
    
    fun questionDtoToEntity(dto: QuizQuestionDto): QuizQuestionEntity = dto.toEntity()
    
    fun questionEntityToDomain(entity: QuizQuestionEntity): QuizQuestion = entity.toDomain()
    
    fun attemptDtoToDomain(dto: QuizAttemptDto): QuizAttempt = dto.toDomain()
    
    fun attemptDtoToEntity(dto: QuizAttemptDto): QuizAttemptEntity = dto.toEntity()
    
    fun attemptEntityToDomain(entity: QuizAttemptEntity): QuizAttempt = entity.toDomain()
    
    fun attemptToDto(attempt: QuizAttempt): QuizAttemptDto = attempt.toDto()
    
    fun attemptToEntity(attempt: QuizAttempt): QuizAttemptEntity = attempt.toEntity()
    
    /**
     * Convert quiz entity with question entities to domain model.
     */
    fun entityToDomain(quizEntity: QuizEntity, questionEntities: List<QuizQuestionEntity>): Quiz {
        val questions = questionEntities.map { it.toDomain() }
        return quizEntity.toDomain(questions)
    }
}

/**
 * Mappers for Quiz conversions between layers.
 */

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

// ==================== Quiz Mappers ====================

fun QuizDto.toEntity(): QuizEntity {
    return QuizEntity(
        id = id,
        chapterId = chapterId,
        title = title,
        subject = subject,
        difficulty = difficulty,
        totalQuestions = totalQuestions,
        timeLimitMinutes = timeLimitMinutes,
        isAiGenerated = isAiGenerated,
        createdAt = createdAt?.let { Date(parseDate(it)) } ?: Date()
    )
}

fun QuizDto.toDomain(): Quiz {
    return Quiz(
        id = id,
        chapterId = chapterId,
        title = title,
        subject = Subject.fromString(subject),
        difficulty = QuizDifficulty.fromString(difficulty),
        totalQuestions = totalQuestions,
        timeLimitMinutes = timeLimitMinutes,
        isAiGenerated = isAiGenerated,
        questions = questions?.map { it.toDomain() } ?: emptyList(),
        createdAt = createdAt?.let { parseDate(it) } ?: System.currentTimeMillis()
    )
}

fun QuizEntity.toDomain(questions: List<QuizQuestion> = emptyList()): Quiz {
    return Quiz(
        id = id,
        chapterId = chapterId,
        title = title,
        subject = Subject.fromString(subject),
        difficulty = QuizDifficulty.fromString(difficulty),
        totalQuestions = totalQuestions,
        timeLimitMinutes = timeLimitMinutes,
        isAiGenerated = isAiGenerated,
        questions = questions,
        createdAt = createdAt.time
    )
}

fun Quiz.toEntity(): QuizEntity {
    return QuizEntity(
        id = id,
        chapterId = chapterId,
        title = title,
        subject = subject.name,
        difficulty = difficulty.name,
        totalQuestions = totalQuestions,
        timeLimitMinutes = timeLimitMinutes,
        isAiGenerated = isAiGenerated,
        createdAt = Date(createdAt)
    )
}

// ==================== QuizQuestion Mappers ====================

fun QuizQuestionDto.toEntity(): QuizQuestionEntity {
    return QuizQuestionEntity(
        id = id,
        quizId = quizId,
        questionText = questionText,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation,
        topic = topic,
        difficulty = difficulty,
        orderIndex = orderIndex
    )
}

fun QuizQuestionDto.toDomain(): QuizQuestion {
    return QuizQuestion(
        id = id,
        quizId = quizId,
        questionText = questionText,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation,
        topic = topic,
        difficulty = QuizDifficulty.fromString(difficulty),
        orderIndex = orderIndex
    )
}

fun QuizQuestionEntity.toDomain(): QuizQuestion {
    return QuizQuestion(
        id = id,
        quizId = quizId,
        questionText = questionText,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation,
        topic = topic,
        difficulty = QuizDifficulty.fromString(difficulty),
        orderIndex = orderIndex
    )
}

fun QuizQuestion.toEntity(): QuizQuestionEntity {
    return QuizQuestionEntity(
        id = id,
        quizId = quizId,
        questionText = questionText,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation,
        topic = topic,
        difficulty = difficulty.name,
        orderIndex = orderIndex
    )
}

// ==================== QuizAttempt Mappers ====================

fun QuizAttemptDto.toEntity(): QuizAttemptEntity {
    return QuizAttemptEntity(
        id = id,
        userId = userId,
        quizId = quizId,
        subject = subject,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        timeTakenSeconds = timeTakenSeconds,
        weakTopics = weakTopics,
        answers = answers,
        isCompleted = isCompleted,
        attemptedAt = attemptedAt?.let { Date(parseDate(it)) } ?: Date(),
        isSynced = true
    )
}

fun QuizAttemptDto.toDomain(): QuizAttempt {
    return QuizAttempt(
        id = id,
        userId = userId,
        quizId = quizId,
        subject = Subject.fromString(subject),
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        timeTakenSeconds = timeTakenSeconds,
        weakTopics = weakTopics ?: emptyList(),
        answers = answers ?: emptyList(),
        isCompleted = isCompleted,
        attemptedAt = attemptedAt?.let { parseDate(it) } ?: System.currentTimeMillis()
    )
}

fun QuizAttemptEntity.toDomain(): QuizAttempt {
    return QuizAttempt(
        id = id,
        userId = userId,
        quizId = quizId,
        subject = Subject.fromString(subject),
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        timeTakenSeconds = timeTakenSeconds,
        weakTopics = weakTopics ?: emptyList(),
        answers = answers ?: emptyList(),
        isCompleted = isCompleted,
        attemptedAt = attemptedAt.time
    )
}

fun QuizAttempt.toEntity(isSynced: Boolean = false): QuizAttemptEntity {
    return QuizAttemptEntity(
        id = id,
        userId = userId,
        quizId = quizId,
        subject = subject.name,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        timeTakenSeconds = timeTakenSeconds,
        weakTopics = weakTopics,
        answers = answers,
        isCompleted = isCompleted,
        attemptedAt = Date(attemptedAt),
        isSynced = isSynced
    )
}

fun QuizAttempt.toDto(): QuizAttemptDto {
    return QuizAttemptDto(
        id = id,
        userId = userId,
        quizId = quizId,
        subject = subject.name,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        scorePercentage = scorePercentage,
        timeTakenSeconds = timeTakenSeconds,
        weakTopics = weakTopics.ifEmpty { null },
        answers = answers.ifEmpty { null },
        isCompleted = isCompleted,
        attemptedAt = attemptedAt.toIsoDateString()
    )
}

// ==================== List Extensions ====================

@JvmName("quizDtoListToEntity")
fun List<QuizDto>.toEntityList(): List<QuizEntity> = map { it.toEntity() }
@JvmName("quizDtoListToDomain")
fun List<QuizDto>.toDomainList(): List<Quiz> = map { it.toDomain() }
@JvmName("quizEntityListToDomain")
fun List<QuizEntity>.toDomainList(): List<Quiz> = map { it.toDomain() }

@JvmName("quizQuestionDtoListToEntity")
fun List<QuizQuestionDto>.toEntityList(): List<QuizQuestionEntity> = map { it.toEntity() }
@JvmName("quizQuestionEntityListToDomain")
fun List<QuizQuestionEntity>.toDomainList(): List<QuizQuestion> = map { it.toDomain() }

fun List<QuizAttemptDto>.toAttemptEntityList(): List<QuizAttemptEntity> = map { it.toEntity() }
fun List<QuizAttemptEntity>.toAttemptDomainList(): List<QuizAttempt> = map { it.toDomain() }

// ==================== Helper Functions ====================

private fun parseDate(dateString: String): Long {
    return try {
        dateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

fun Long.toIsoDateString(): String {
    return dateFormat.format(Date(this))
}
