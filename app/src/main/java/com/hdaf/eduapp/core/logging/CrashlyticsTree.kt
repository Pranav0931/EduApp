package com.hdaf.eduapp.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Custom Timber Tree that logs to Firebase Crashlytics.
 * 
 * This tree filters out debug and verbose logs, only sending
 * warnings, errors, and assertions to Crashlytics for analysis.
 * 
 * Usage: Timber.plant(CrashlyticsTree()) in Application.onCreate()
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log warnings, errors, and assertions to Crashlytics
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val priorityLabel = when (priority) {
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }

        // Log the message to Crashlytics
        crashlytics.log("$priorityLabel/$tag: $message")

        // If there's a throwable, record it as a non-fatal exception
        t?.let { throwable ->
            crashlytics.recordException(throwable)
        }

        // For assertions (WTF logs), also record as exceptions
        if (priority == Log.ASSERT && t == null) {
            crashlytics.recordException(AssertionError("$tag: $message"))
        }
    }

    /**
     * Set a custom key-value pair for Crashlytics logs.
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set the user identifier for Crashlytics.
     */
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
