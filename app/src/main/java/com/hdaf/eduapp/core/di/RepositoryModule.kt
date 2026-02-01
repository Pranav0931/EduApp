package com.hdaf.eduapp.core.di

import com.hdaf.eduapp.data.repository.AuthRepositoryImpl
import com.hdaf.eduapp.data.repository.ContentRepositoryImpl
import com.hdaf.eduapp.data.repository.GamificationRepositoryImpl
import com.hdaf.eduapp.data.repository.ProgressRepositoryImpl
import com.hdaf.eduapp.data.repository.QuizRepositoryImpl
import com.hdaf.eduapp.data.repository.UserRepositoryImpl
import com.hdaf.eduapp.domain.repository.AuthRepository
import com.hdaf.eduapp.domain.repository.ContentRepository
import com.hdaf.eduapp.domain.repository.GamificationRepository
import com.hdaf.eduapp.domain.repository.ProgressRepository
import com.hdaf.eduapp.domain.repository.QuizRepository
import com.hdaf.eduapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        quizRepositoryImpl: QuizRepositoryImpl
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(
        gamificationRepositoryImpl: GamificationRepositoryImpl
    ): GamificationRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
