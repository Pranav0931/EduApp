package com.hdaf.eduapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration as WorkConfig
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hdaf.eduapp.core.logging.CrashlyticsTree
import com.hdaf.eduapp.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class for EduApp.
 * 
 * Responsibilities:
 * - Initialize Hilt dependency injection
 * - Configure WorkManager with Hilt
 * - Setup logging (Timber + Crashlytics)
 * - Initialize Firebase services
 * - Configure Coil image loader
 * - Handle app locale configuration
 * 
 * @see HiltAndroidApp
 */
@HiltAndroidApp
class EduApplication : Application(), WorkConfig.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        initializeFirebase()
        initializeLogging()
        initializeCrashlytics()
    }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(base))
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.applyLocale(this)
    }

    /**
     * Initialize Firebase services.
     */
    private fun initializeFirebase() {
        FirebaseApp.initializeApp(this)
    }

    /**
     * Initialize Timber logging.
     * - Debug: DebugTree for logcat output
     * - Release: CrashlyticsTree for crash reporting
     */
    private fun initializeLogging() {
        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
        }
        
        if (BuildConfig.ENABLE_CRASH_REPORTING) {
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * Initialize Firebase Crashlytics.
     */
    private fun initializeCrashlytics() {
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(BuildConfig.ENABLE_CRASH_REPORTING)
        }
    }

    /**
     * WorkManager configuration with Hilt support.
     */
    override val workManagerConfiguration: WorkConfig
        get() = WorkConfig.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.ENABLE_LOGGING) android.util.Log.DEBUG 
                else android.util.Log.ERROR
            )
            .build()

    /**
     * Configure Coil image loader with memory and disk cache.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.10) // 10% of available disk space
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
