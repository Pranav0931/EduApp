package com.hdaf.eduapp.core.network

import com.hdaf.eduapp.BuildConfig
import com.hdaf.eduapp.core.security.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor for adding authentication headers.
 * 
 * Adds:
 * - Authorization: Bearer token
 * - API keys for Supabase
 * - Custom headers
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences
) : Interceptor {

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_API_KEY = "apikey"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_PREFER = "Prefer"
        
        private const val CONTENT_TYPE_JSON = "application/json"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        val requestBuilder = originalRequest.newBuilder()
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)

        // Add Supabase headers for Supabase API calls
        if (url.contains("supabase.co")) {
            val supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            if (supabaseKey.isNotEmpty()) {
                requestBuilder.header(HEADER_API_KEY, supabaseKey)
                requestBuilder.header(HEADER_AUTHORIZATION, BEARER_PREFIX + supabaseKey)
                requestBuilder.header(HEADER_PREFER, "return=representation")
            }
        }

        // Add auth token for authenticated requests
        val authToken = securePreferences.getAuthToken()
        if (!authToken.isNullOrEmpty() && !url.contains("supabase.co")) {
            requestBuilder.header(HEADER_AUTHORIZATION, BEARER_PREFIX + authToken)
        }

        return chain.proceed(requestBuilder.build())
    }
}
