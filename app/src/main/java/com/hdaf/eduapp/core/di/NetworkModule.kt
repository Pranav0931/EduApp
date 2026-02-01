package com.hdaf.eduapp.core.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hdaf.eduapp.BuildConfig
import com.hdaf.eduapp.core.network.AuthInterceptor
import com.hdaf.eduapp.core.network.NetworkInterceptor
import com.hdaf.eduapp.data.remote.api.AuthApi
import com.hdaf.eduapp.data.remote.api.ContentApi
import com.hdaf.eduapp.data.remote.api.GamificationApi
import com.hdaf.eduapp.data.remote.api.QuizApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Network dependency injection module.
 * 
 * Provides:
 * - OkHttpClient with interceptors
 * - Retrofit instances
 * - API service interfaces
 * - Gson configuration
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE = 50L * 1024L * 1024L // 50 MB
    private const val TIMEOUT_SECONDS = 30L

    /**
     * Provides Gson with custom configuration.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create()
    }

    /**
     * Provides HTTP cache.
     */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, CACHE_SIZE)
    }

    /**
     * Provides logging interceptor for debug builds.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provides certificate pinner for SSL pinning.
     * 
     * TODO: Add actual certificate hashes for production domains
     */
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            // Add certificate pins for your API domains
            // .add("api.eduapp.com", "sha256/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            // .add("*.supabase.co", "sha256/XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            .build()
    }

    /**
     * Provides OkHttpClient with all interceptors and configuration.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        networkInterceptor: NetworkInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(authInterceptor)
            .addInterceptor(networkInterceptor)
            .addInterceptor(loggingInterceptor)
            .certificatePinner(certificatePinner)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provides main API Retrofit instance.
     */
    @Provides
    @Singleton
    @Named("mainApi")
    fun provideMainRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides Supabase Retrofit instance.
     */
    @Provides
    @Singleton
    @Named("supabaseApi")
    fun provideSupabaseRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val baseUrl = if (supabaseUrl.isNotEmpty()) {
            "$supabaseUrl/rest/v1/"
        } else {
            "https://placeholder.supabase.co/rest/v1/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides Auth API service.
     */
    @Provides
    @Singleton
    fun provideAuthApi(@Named("mainApi") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * Provides Content API service.
     */
    @Provides
    @Singleton
    fun provideContentApi(@Named("supabaseApi") retrofit: Retrofit): ContentApi {
        return retrofit.create(ContentApi::class.java)
    }

    /**
     * Provides Quiz API service.
     */
    @Provides
    @Singleton
    fun provideQuizApi(@Named("mainApi") retrofit: Retrofit): QuizApi {
        return retrofit.create(QuizApi::class.java)
    }

    /**
     * Provides Gamification API service.
     */
    @Provides
    @Singleton
    fun provideGamificationApi(@Named("mainApi") retrofit: Retrofit): GamificationApi {
        return retrofit.create(GamificationApi::class.java)
    }
}
