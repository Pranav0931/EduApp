package com.hdaf.eduapp.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.repository.ContentRepository
import com.hdaf.eduapp.domain.repository.ProgressRepository
import com.hdaf.eduapp.domain.repository.QuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for syncing offline data with server.
 * Runs periodically and on network availability.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val quizRepository: QuizRepository,
    private val progressRepository: ProgressRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "edu_app_sync"
        const val WORK_NAME_IMMEDIATE = "edu_app_sync_immediate"
        
        /**
         * Schedule periodic sync.
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 30,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .addTag("sync")
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            
            Timber.d("Scheduled periodic sync")
        }
        
        /**
         * Trigger immediate sync.
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .addTag("sync_immediate")
                .build()
            
            WorkManager.getInstance(context).enqueue(syncRequest)
            Timber.d("Triggered immediate sync")
        }
        
        /**
         * Cancel all sync work.
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            WorkManager.getInstance(context).cancelAllWorkByTag("sync")
        }
    }

    override suspend fun doWork(): Result {
        Timber.d("Starting sync work")
        
        return try {
            // Sync content
            val contentResult = contentRepository.syncContent()
            if (contentResult is Resource.Error) {
                Timber.w("Content sync failed: ${contentResult.message}")
            }
            
            // Sync offline quiz attempts
            val quizResult = quizRepository.syncOfflineAttempts()
            when (quizResult) {
                is Resource.Success -> {
                    Timber.d("Synced ${quizResult.data} quiz attempts")
                }
                is Resource.Error -> {
                    Timber.w("Quiz sync failed: ${quizResult.message}")
                }
                is Resource.Loading -> { }
            }
            
            // Sync progress
            val progressResult = progressRepository.syncProgress()
            if (progressResult is Resource.Error) {
                Timber.w("Progress sync failed: ${progressResult.message}")
            }
            
            Timber.d("Sync completed successfully")
            Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Sync work failed")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

/**
 * Worker for downloading content in background.
 */
@HiltWorker
class ContentDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_BOOK_ID = "book_id"
        const val KEY_CHAPTER_ID = "chapter_id"
        const val KEY_DOWNLOAD_TYPE = "download_type"
        
        fun downloadBook(context: Context, bookId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build()
            
            val downloadRequest = OneTimeWorkRequestBuilder<ContentDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(
                    androidx.work.workDataOf(
                        KEY_BOOK_ID to bookId,
                        KEY_DOWNLOAD_TYPE to "book"
                    )
                )
                .addTag("download")
                .addTag("download_book_$bookId")
                .build()
            
            WorkManager.getInstance(context).enqueue(downloadRequest)
        }
        
        fun downloadChapter(context: Context, chapterId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build()
            
            val downloadRequest = OneTimeWorkRequestBuilder<ContentDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(
                    androidx.work.workDataOf(
                        KEY_CHAPTER_ID to chapterId,
                        KEY_DOWNLOAD_TYPE to "chapter"
                    )
                )
                .addTag("download")
                .addTag("download_chapter_$chapterId")
                .build()
            
            WorkManager.getInstance(context).enqueue(downloadRequest)
        }
    }

    override suspend fun doWork(): Result {
        val downloadType = inputData.getString(KEY_DOWNLOAD_TYPE) ?: return Result.failure()
        
        return try {
            when (downloadType) {
                "book" -> {
                    val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
                    contentRepository.downloadBook(bookId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val progress = result.data
                                setProgress(androidx.work.workDataOf("progress" to progress))
                            }
                            is Resource.Error -> {
                                throw Exception(result.message)
                            }
                            is Resource.Loading -> { }
                        }
                    }
                }
                "chapter" -> {
                    val chapterId = inputData.getString(KEY_CHAPTER_ID) ?: return Result.failure()
                    contentRepository.downloadChapter(chapterId).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                setProgress(androidx.work.workDataOf("progress" to result.data))
                            }
                            is Resource.Error -> {
                                throw Exception(result.message)
                            }
                            is Resource.Loading -> { }
                        }
                    }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Download failed")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}

/**
 * Worker for daily streak reminder notifications.
 */
@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val progressRepository: ProgressRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "streak_reminder"
        
        fun scheduleDaily(context: Context, hourOfDay: Int = 18) {
            val constraints = Constraints.Builder()
                .build()
            
            // Calculate initial delay to target time
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            
            if (target.before(now)) {
                target.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            
            val initialDelay = target.timeInMillis - now.timeInMillis
            
            val reminderRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag("reminder")
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Check if streak is at risk by looking at last activity
            progressRepository.getStreakInfo().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val streakStatus = result.data
                        // If user has a streak and hasn't been active today, remind them
                        if (streakStatus.currentStreak > 0 && !streakStatus.isActiveToday) {
                            showStreakReminderNotification()
                        }
                    }
                    else -> { /* Ignore errors */ }
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Streak reminder failed")
            Result.failure()
        }
    }
    
    private fun showStreakReminderNotification() {
        // Notification implementation would go here
        // Using NotificationCompat with proper channel setup
        Timber.d("Would show streak reminder notification")
    }
}
