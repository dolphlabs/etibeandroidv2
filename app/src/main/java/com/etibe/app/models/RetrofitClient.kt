package com.etibe.app.models

import android.content.Context
import android.system.Os.remove
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit



object RetrofitClient {

    private const val BASE_URL = "https://api.etibeapp.xyz/"
    private const val TAG = "RetrofitClient"

    private lateinit var appContext: Context
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun instance(): Api {
        check(::appContext.isInitialized) {
            "RetrofitClient.init(context) must be called before instance()"
        }

        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(getOkHttpClient())
                        .build()
                }
            }
        }
        return retrofit!!.create(Api::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Change to BASIC for prod
        }



        return OkHttpClient.Builder()
          //  .cookieJar(cookieJar)
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val authInterceptor = Interceptor { chain ->
        val token = getAuthToken()

        val requestBuilder = chain.request().newBuilder()
            .addHeader("Accept", "application/json")

        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.w(TAG, "No auth token found")
        }

        chain.proceed(requestBuilder.build())
    }

    private fun getAuthToken(): String {
        val prefs = appContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", "") ?: ""
    }
}
