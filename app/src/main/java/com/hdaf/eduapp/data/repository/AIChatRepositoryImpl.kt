package com.hdaf.eduapp.data.repository

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.core.di.IoDispatcher
import com.hdaf.eduapp.data.local.dao.AIChatDao
import com.hdaf.eduapp.data.local.entity.AIChatMessageEntity
import com.hdaf.eduapp.data.mapper.AIChatMessage
import com.hdaf.eduapp.data.mapper.ResponseMode
import com.hdaf.eduapp.data.mapper.toDomain
import com.hdaf.eduapp.data.mapper.toEntity
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.hdaf.eduapp.domain.repository.AIChatRepository
import com.hdaf.eduapp.domain.repository.AccessibilityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AIChatRepository.
 * Handles AI assistant chat with offline caching.
 */
@Singleton
class AIChatRepositoryImpl @Inject constructor(
    private val aiChatDao: AIChatDao,
    private val accessibilityRepository: AccessibilityRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AIChatRepository {

    override suspend fun sendMessage(
        userId: String,
        sessionId: String,
        message: String,
        contextSubject: String?,
        contextChapter: String?
    ): Resource<AIChatMessage> = withContext(ioDispatcher) {
        try {
            // Save user message
            val userMessage = AIChatMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                sessionId = sessionId,
                message = message,
                isUserMessage = true,
                contextSubject = contextSubject,
                contextChapter = contextChapter
            )
            aiChatDao.insertMessage(userMessage.toEntity())

            // Check for cached response first
            val cachedResponses = aiChatDao.searchCachedResponses(message)
            if (cachedResponses.isNotEmpty()) {
                val cached = cachedResponses.first()
                val cachedMessage = cached.toDomain().copy(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis()
                )
                aiChatDao.insertMessage(cachedMessage.toEntity())
                return@withContext Resource.Success(cachedMessage)
            }

            // Determine response mode based on user's accessibility profile
            val profile = accessibilityRepository.getProfile(userId)
            val responseMode = when {
                profile is Resource.Success && profile.data.accessibilityMode == AccessibilityModeType.BLIND -> ResponseMode.AUDIO
                profile is Resource.Success && profile.data.accessibilityMode == AccessibilityModeType.DEAF -> ResponseMode.TEXT
                else -> ResponseMode.TEXT
            }

            // Generate AI response (mock implementation - replace with actual AI service)
            val aiResponse = generateAIResponse(message, contextSubject, contextChapter, responseMode)
            
            val responseMessage = AIChatMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                sessionId = sessionId,
                message = aiResponse,
                isUserMessage = false,
                responseMode = responseMode,
                contextSubject = contextSubject,
                contextChapter = contextChapter,
                isCached = true
            )
            
            aiChatDao.insertMessage(responseMessage.toEntity())
            Resource.Success(responseMessage)
            
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            Resource.Error("Failed to send message: ${e.message}")
        }
    }

    override suspend fun getRecentMessages(userId: String, limit: Int): Resource<List<AIChatMessage>> = 
        withContext(ioDispatcher) {
            try {
                val messages = aiChatDao.getRecentMessages(userId, limit)
                Resource.Success(messages.map { it.toDomain() })
            } catch (e: Exception) {
                Timber.e(e, "Error getting recent messages")
                Resource.Error("Failed to get messages: ${e.message}")
            }
        }

    override fun observeSessionMessages(sessionId: String): Flow<List<AIChatMessage>> {
        return aiChatDao.observeSessionMessages(sessionId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun searchCachedResponses(query: String): List<AIChatMessage> = 
        withContext(ioDispatcher) {
            try {
                aiChatDao.searchCachedResponses(query).map { it.toDomain() }
            } catch (e: Exception) {
                Timber.e(e, "Error searching cached responses")
                emptyList()
            }
        }

    override suspend fun clearSession(userId: String, sessionId: String): Resource<Unit> = 
        withContext(ioDispatcher) {
            try {
                aiChatDao.clearSession(userId, sessionId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error clearing session")
                Resource.Error("Failed to clear session: ${e.message}")
            }
        }

    override suspend fun generateSpokenSummary(contentId: String, text: String): Resource<String> = 
        withContext(ioDispatcher) {
            try {
                // Generate a simplified summary for TTS
                val summary = generateSummary(text)
                Resource.Success(summary)
            } catch (e: Exception) {
                Timber.e(e, "Error generating spoken summary")
                Resource.Error("Failed to generate summary: ${e.message}")
            }
        }

    /**
     * Generate AI response based on context.
     * TODO: Replace with actual AI service integration (OpenAI, Google AI, etc.)
     */
    private fun generateAIResponse(
        message: String,
        contextSubject: String?,
        contextChapter: String?,
        responseMode: ResponseMode
    ): String {
        // Mock AI response - replace with actual AI service call
        val contextInfo = buildString {
            if (contextSubject != null) append("Subject: $contextSubject. ")
            if (contextChapter != null) append("Chapter: $contextChapter. ")
        }
        
        return when {
            message.contains("explain", ignoreCase = true) -> 
                "Let me explain that concept for you. $contextInfo This topic covers fundamental principles that build upon what you've learned. Would you like me to break it down into smaller parts?"
            
            message.contains("help", ignoreCase = true) -> 
                "I'm here to help! $contextInfo What specific aspect would you like assistance with? I can explain concepts, help with practice problems, or provide study tips."
            
            message.contains("quiz", ignoreCase = true) -> 
                "Great idea to practice! $contextInfo I can help you prepare for quizzes by reviewing key concepts and testing your understanding. Ready to start?"
            
            message.contains("summary", ignoreCase = true) -> 
                "Here's a brief summary: $contextInfo The main points to remember are the core concepts and their applications. Would you like more detail on any specific area?"
            
            else -> 
                "I understand you're asking about: \"$message\". $contextInfo Let me help you with that. Could you tell me more about what you'd like to know?"
        }
    }

    /**
     * Generate a simplified summary suitable for TTS.
     */
    private fun generateSummary(text: String): String {
        // Simple summarization - take first few sentences and key points
        val sentences = text.split(Regex("[.!?]")).filter { it.isNotBlank() }
        return if (sentences.size <= 3) {
            text
        } else {
            sentences.take(3).joinToString(". ") + "."
        }
    }
}
