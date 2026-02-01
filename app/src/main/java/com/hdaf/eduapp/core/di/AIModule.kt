package com.hdaf.eduapp.core.di

import com.hdaf.eduapp.data.ai.GeminiAIServiceImpl
import com.hdaf.eduapp.domain.ai.EduAIService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AI service binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    @Binds
    @Singleton
    abstract fun bindEduAIService(
        geminiAIServiceImpl: GeminiAIServiceImpl
    ): EduAIService
}
