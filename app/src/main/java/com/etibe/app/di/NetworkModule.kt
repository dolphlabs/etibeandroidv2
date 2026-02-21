package com.etibe.app.di

import android.content.Context
import android.util.Log
import com.etibe.app.adapter.SafeDoubleAdapter
import com.etibe.app.adapter.SafeIntAdapter
import com.etibe.app.models.Api
import com.etibe.app.models.AppEvent
import com.etibe.app.models.AppEventBus
import com.etibe.app.ui.NetworkUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TAG = "RetrofitClient"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideOkHttpClient(

        @ApplicationContext context: Context
    ): OkHttpClient {
        // Your existing builder with logging + auth + network check interceptors
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(getNetworkCheckInterceptor(context))
            .addInterceptor(getAuthInterceptor(context))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.etibeapp.xyz/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

    // Provide your custom Gson if needed
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Int::class.java, SafeIntAdapter())
        .registerTypeAdapter(Double::class.java, SafeDoubleAdapter())
        .setLenient()
        .create()
}

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

private fun handleLogout(context: Context) {
    val sharedPreferences = context.getSharedPreferences("info", Context.MODE_PRIVATE)
    sharedPreferences.edit().apply {
        remove("auth_token")
        remove("refresh_token")
        apply()
    }
    Log.d(TAG, "User logged out - tokens cleared")
}
