package com.hdaf.eduapp.sync;

import android.content.Context;
import android.util.Log;

import com.hdaf.eduapp.analytics.LearningSession;
import com.hdaf.eduapp.gamification.UserProgress;
import com.hdaf.eduapp.supabase.SupabaseClient;
import com.hdaf.eduapp.supabase.models.AnalyticsLogModel;
import com.hdaf.eduapp.supabase.models.ProfileModel;
import com.hdaf.eduapp.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages synchronization of data between the local app and the cloud (Supabase).
 */
public class CloudSyncManager {

    private static final String TAG = "CloudSyncManager";
    private static CloudSyncManager instance;

    private final Context context;
    private final PreferenceManager prefManager;

    private CloudSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefManager = PreferenceManager.getInstance(context);
    }

    public static synchronized CloudSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudSyncManager(context);
        }
        return instance;
    }

    /**
     * Get or create persistent user ID for this device.
     */
    private String getUserId() {
        String userId = prefManager.getStringPref("device_user_id");

        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            prefManager.putString("device_user_id", userId);
        }
        return userId;
    }

    /**
     * Sync latest user profile stats.
     */
    public void syncProfile(UserProgress progress) {
        if (!SupabaseClient.isConfigured()) return;

        ProfileModel profile = new ProfileModel(
                getUserId(),
                progress.getTotalXP(),
                progress.getCurrentLevel(),
                progress.getQuizzesCompleted(),
                progress.getCurrentStreak(),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                        .format(new Date())
        );

        SupabaseClient.getInstance().getApi()
                .upsertProfile(profile)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Profile synced successfully");
                        } else {
                            Log.e(TAG, "Profile sync failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Profile sync error", t);
                    }
                });
    }

    /**
     * Upload a completed learning session.
     */
    public void uploadSession(LearningSession session) {
        if (!SupabaseClient.isConfigured() || session == null) return;

        String metadataJson = "{}";
        if (session.getMetadata() != null && !session.getMetadata().isEmpty()) {
            metadataJson = new com.google.gson.Gson().toJson(session.getMetadata());
        }

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        AnalyticsLogModel log = new AnalyticsLogModel(
                session.getSessionId(),
                getUserId(),
                session.getActivityType().name(),
                session.getSubject(),
                sdf.format(new Date(session.getStartTime())),
                sdf.format(new Date(session.getEndTime())),
                session.getDurationSeconds(),
                metadataJson
        );

        SupabaseClient.getInstance().getApi()
                .createAnalyticsLog(log)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Session uploaded successfully");
                        } else {
                            Log.e(TAG, "Session upload failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Session upload error", t);
                    }
                });
    }
}
