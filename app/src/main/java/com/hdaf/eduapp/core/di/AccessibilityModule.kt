package com.hdaf.eduapp.core.di

import com.hdaf.eduapp.data.local.dao.AccessibilityProfileDao
import com.hdaf.eduapp.data.local.dao.AIChatDao
import com.hdaf.eduapp.data.local.dao.OCRCacheDao
import com.hdaf.eduapp.data.local.dao.StudyAnalyticsDao
import com.hdaf.eduapp.data.local.dao.StudyRecommendationDao
import com.hdaf.eduapp.data.local.dao.VoiceCommandDao
import com.hdaf.eduapp.data.local.EduAppDatabase
import com.hdaf.eduapp.data.repository.AccessibilityRepositoryImpl
import com.hdaf.eduapp.data.repository.AIChatRepositoryImpl
import com.hdaf.eduapp.data.repository.StudyAnalyticsRepositoryImpl
import com.hdaf.eduapp.domain.repository.AccessibilityRepository
import com.hdaf.eduapp.domain.repository.AIChatRepository
import com.hdaf.eduapp.domain.repository.StudyAnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for accessibility-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AccessibilityModule {

    @Binds
    @Singleton
    abstract fun bindAccessibilityRepository(
        impl: AccessibilityRepositoryImpl
    ): AccessibilityRepository

    @Binds
    @Singleton
    abstract fun bindAIChatRepository(
        impl: AIChatRepositoryImpl
    ): AIChatRepository

    @Binds
    @Singleton
    abstract fun bindStudyAnalyticsRepository(
        impl: StudyAnalyticsRepositoryImpl
    ): StudyAnalyticsRepository
}

/**
 * Provides accessibility DAOs from the database.
 */
@Module
@InstallIn(SingletonComponent::class)
object AccessibilityDaoModule {

    @Provides
    @Singleton
    fun provideAccessibilityProfileDao(database: EduAppDatabase): AccessibilityProfileDao {
        return database.accessibilityProfileDao()
    }

    @Provides
    @Singleton
    fun provideAIChatDao(database: EduAppDatabase): AIChatDao {
        return database.aiChatDao()
    }

    @Provides
    @Singleton
    fun provideStudyAnalyticsDao(database: EduAppDatabase): StudyAnalyticsDao {
        return database.studyAnalyticsDao()
    }

    @Provides
    @Singleton
    fun provideStudyRecommendationDao(database: EduAppDatabase): StudyRecommendationDao {
        return database.studyRecommendationDao()
    }

    @Provides
    @Singleton
    fun provideOCRCacheDao(database: EduAppDatabase): OCRCacheDao {
        return database.ocrCacheDao()
    }

    @Provides
    @Singleton
    fun provideVoiceCommandDao(database: EduAppDatabase): VoiceCommandDao {
        return database.voiceCommandDao()
    }
}
