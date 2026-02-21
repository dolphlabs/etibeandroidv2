package com.etibe.app.models

import com.etibe.app.utils.Circle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class CircleRepository @Inject constructor(
    private val apiService: Api   // ← this is the correct, Hilt-provided instance
) {
    suspend fun discoverCircles(search: String? = null): Result<List<Circle>> {
        return withContext(Dispatchers.IO) {
            try {
                // Use the injected service – NO RetrofitClient.instance(...) here!
                val response = apiService.discoverCircles(search = search)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.Success(body.data.circles)
                    } else {
                        Result.Error("Failed to fetch circles: ${body?.success ?: "No success flag"}")
                    }
                } else {
                    Result.Error(
                        message = "Server error: ${response.code()} - ${response.message()}",
                        exception = null
                    )
                }
            } catch (e: Exception) {
                Result.Error(
                    message = e.message ?: "Network or parsing error",
                    exception = e
                )
            }
        }
    }
}