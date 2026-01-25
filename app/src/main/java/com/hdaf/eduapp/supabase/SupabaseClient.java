package com.hdaf.eduapp.supabase;

import com.hdaf.eduapp.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Supabase client singleton for REST API access.
 * Uses Retrofit + OkHttp to communicate with Supabase.
 */
public class SupabaseClient {

    private static SupabaseClient instance;
    private final Retrofit retrofit;
    private final SupabaseApi api;

    // Supabase configuration
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    private SupabaseClient() {
        // Create logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttp client with Supabase headers
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                            .header("Content-Type", "application/json")
                            .header("Prefer", "return=representation")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Build Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL + "/rest/v1/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API interface
        api = retrofit.create(SupabaseApi.class);
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public SupabaseApi getApi() {
        return api;
    }

    /**
     * Get the public storage URL for a file.
     * 
     * @param bucket The storage bucket name
     * @param path   The file path within the bucket
     * @return Full public URL to the file
     */
    public static String getStorageUrl(String bucket, String path) {
        return SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    /**
     * Check if Supabase is configured (not using placeholder values)
     */
    public static boolean isConfigured() {
        return !SUPABASE_URL.contains("your-project") &&
                !SUPABASE_ANON_KEY.equals("your-anon-key");
    }
}
