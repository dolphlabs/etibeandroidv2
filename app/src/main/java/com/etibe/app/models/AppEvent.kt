package com.etibe.app.models

import kotlinx.coroutines.flow.MutableSharedFlow

sealed class AppEvent {
    object NoInternet : AppEvent()
    object SessionExpired : AppEvent()
    data class GenericError(val message: String) : AppEvent()
    data class NetworkError(val message: String) : AppEvent()
    data class ServerError(val code: Int, val message: String) : AppEvent()
}

object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(replay = 0)
    val events = _events

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}