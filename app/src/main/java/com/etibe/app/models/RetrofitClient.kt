package com.etibe.app.models

import android.content.Context
import android.util.Log
import com.etibe.app.NetworkUtil
import com.etibe.app.adapter.SafeDoubleAdapter
import com.etibe.app.adapter.SafeIntAdapter
import com.google.gson.GsonBuilder
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.io.IOException
object RetrofitClient {
    private const val BASE_URL = "https://api.etibeapp.xyz/"
    private const val TAG = "RetrofitClient"

    // Logging interceptor for debugging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Singleton Retrofit instance
    private var retrofit: Retrofit? = null

    // GSON instance with custom adapters
    val gson = GsonBuilder()
        .registerTypeAdapter(Int::class.java, SafeIntAdapter())
        .registerTypeAdapter(Double::class.java, SafeDoubleAdapter())
        .setLenient()
        .create()

    /**
     * Thread-safe method to get Retrofit instance
     */
    fun instance(context: Context): Api {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(getOkHttpClient(context))
                        .build()
                }
            }
        }
        return retrofit!!.create(Api::class.java)
    }

    /**
     * OkHttpClient with interceptors and timeouts
     */
    private fun getOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(getNetworkCheckInterceptor(context))
            .addInterceptor(getAuthInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // Retry on connection failure
            .build()
    }

    /**
     * Interceptor to check network connectivity before making requests
     */
    private fun getNetworkCheckInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            if (!NetworkUtil.isConnected(context)) {
                // Emit event in non-blocking way
                kotlinx.coroutines.runBlocking {
                    AppEventBus.emit(AppEvent.NoInternet)
                }
                throw IOException("NO_INTERNET")
            }
            chain.proceed(chain.request())
        }
    }

    /**
     * Interceptor to handle authentication and session management
     */
    private fun getAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // Add headers to request
            val request = originalRequest.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .apply {
                    getAuthToken(context).takeIf { it.isNotEmpty() }?.let {
                        addHeader("Authorization", "Bearer $it")
                    }
                }
                .build()

            // Execute request
            val response = chain.proceed(request)

            // Handle 401 Unauthorized
            if (response.code == 401) {
                Log.w(TAG, "Received 401 Unauthorized - Session expired")
                handleLogout(context)
                kotlinx.coroutines.runBlocking {
                    AppEventBus.emit(AppEvent.SessionExpired)
                }
                response.close()
                throw IOException("UNAUTHORIZED")
            }

            response
        }
    }

    /**
     * Helper function to get auth token from SharedPreferences
     */
    private fun getAuthToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("info", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", "") ?: ""
        Log.d(TAG, "Retrieved Token: ${if (token.isNotEmpty()) "Token exists" else "No token"}")
        return token
    }

    /**
     * Helper function to get refresh token from SharedPreferences
     */
    private fun getRefreshToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("info", Context.MODE_PRIVATE)
        return sharedPreferences.getString("refresh_token", "") ?: ""
    }

    /**
     * Helper function to save tokens in SharedPreferences
     */
    fun saveTokens(context: Context, authToken: String, refreshToken: String? = null) {
        val sharedPreferences = context.getSharedPreferences("info", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("auth_token", authToken)
            refreshToken?.let { putString("refresh_token", it) }
            apply()
        }
        Log.d(TAG, "Tokens saved successfully")
    }

    /**
     * Handle logout by clearing tokens
     */
    private fun handleLogout(context: Context) {
        val sharedPreferences = context.getSharedPreferences("info", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            remove("auth_token")
            remove("refresh_token")
            apply()
        }
        Log.d(TAG, "User logged out - tokens cleared")
    }

    /**
     * Public method to clear tokens (for manual logout)
     */
    fun clearTokens(context: Context) {
        handleLogout(context)
    }
}