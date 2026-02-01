package com.hdaf.eduapp.domain.usecase.progress

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for getting today's XP earned.
 */
class GetTodayXpUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    operator fun invoke(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        
        progressRepository.getUserProgress().collect { result ->
            when (result) {
                is Resource.Success -> {
                    // Calculate today's XP (simplified - in production, track daily XP separately)
                    val todayXp = result.data.totalXp % 100 // Placeholder logic
                    emit(Resource.Success(todayXp))
                }
                is Resource.Error -> emit(Resource.Error(result.message))
                is Resource.Loading -> emit(Resource.Loading())
            }
        }
    }
}
