package com.etibe.app.models


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.etibe.app.utils.Circle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreEtibeViewModel @Inject constructor(
    private val repository: CircleRepository
) : ViewModel() {

    private val _circles = MutableLiveData<List<Circle>>()
    val circles: LiveData<List<Circle>> = _circles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private var allCircles: List<Circle> = emptyList()
    private var currentSearchQuery: String? = null

    init {
    }

    fun loadCircles(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.discoverCircles()) {
                is Result.Success -> {
                    allCircles = result.data
                    _circles.value = result.data
                    _isEmpty.value = result.data.isEmpty()
                    _isLoading.value = false
                }
                is Result.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                    _isEmpty.value = _circles.value?.isEmpty() ?: true
                }
                is Result.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    fun searchCircles(query: String) {
        currentSearchQuery = query
        if (query.isBlank()) {
            _circles.value = allCircles
            _isEmpty.value = allCircles.isEmpty()
            return
        }

        val filteredCircles = allCircles.filter { circle ->
            circle.name.contains(query, ignoreCase = true) ||
                    circle.description?.contains(query, ignoreCase = true) == true
        }

        _circles.value = filteredCircles
        _isEmpty.value = filteredCircles.isEmpty()
    }

    fun clearSearch() {
        currentSearchQuery = null
        _circles.value = allCircles
        _isEmpty.value = allCircles.isEmpty()
    }

    fun retry() {
        loadCircles(forceRefresh = true)
    }
}