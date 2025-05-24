package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FavoriteViewModel"

/**
 * Sealed class representing different UI states for favorite car operations.
 */
sealed class FavoriteUiState {
    object Loading : FavoriteUiState()
    data class Success(val cars: List<Car>) : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
    object Empty : FavoriteUiState()
}

/**
 * ViewModel for managing favorite car functionality.
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    // UI state for favorite cars
    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Empty)
    val uiState: StateFlow<FavoriteUiState> = _uiState
    
    // List of favorite cars
    private val _favoriteCars = MutableStateFlow<List<Car>>(emptyList())
    val favoriteCars: StateFlow<List<Car>> = _favoriteCars
    
    // Map of car favorite status (carId -> isFavorite)
    private val _favoriteStatusMap = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val favoriteStatusMap: StateFlow<Map<Long, Boolean>> = _favoriteStatusMap
    
    // Initialize by loading favorite cars
    init {
        loadFavoriteCars()
    }
    
    /**
     * Load all favorite cars from the repository.
     */
    fun loadFavoriteCars() {
        _uiState.value = FavoriteUiState.Loading
        
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoriteCars().collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            val cars = result.data ?: emptyList()
                            if (cars.isEmpty()) {
                                _uiState.value = FavoriteUiState.Empty
                            } else {
                                _uiState.value = FavoriteUiState.Success(cars)
                                _favoriteCars.value = cars
                                
                                // Update the favorite status map
                                val newMap = _favoriteStatusMap.value.toMutableMap()
                                cars.forEach { newMap[it.id] = true }
                                _favoriteStatusMap.value = newMap
                            }
                            Log.d(TAG, "Loaded ${cars.size} favorite cars")
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = FavoriteUiState.Error(
                                result.message ?: "Failed to load favorite cars"
                            )
                            Log.e(TAG, "Error loading favorite cars: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FavoriteUiState.Error(e.message ?: "Unknown error")
                Log.e(TAG, "Exception loading favorite cars", e)
            }
        }
    }
    
    /**
     * Toggle favorite status for a car.
     * If it's currently favorited, remove it. If not, add it.
     *
     * @param carId The ID of the car to toggle
     */
    fun toggleFavorite(carId: Long) {
        val currentStatus = _favoriteStatusMap.value[carId] ?: false
        
        if (currentStatus) {
            removeFromFavorites(carId)
        } else {
            addToFavorites(carId)
        }
    }
    
    /**
     * Add a car to favorites.
     *
     * @param carId The ID of the car to add to favorites
     */
    fun addToFavorites(carId: Long) {
        viewModelScope.launch {
            try {
                favoriteRepository.addToFavorites(carId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            if (result.data == true) {
                                // Update status map
                                val newMap = _favoriteStatusMap.value.toMutableMap()
                                newMap[carId] = true
                                _favoriteStatusMap.value = newMap
                                
                                // Reload favorite cars to update the list
                                loadFavoriteCars()
                                
                                Log.d(TAG, "Added car ID $carId to favorites")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error adding to favorites: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception adding to favorites", e)
            }
        }
    }
    
    /**
     * Remove a car from favorites.
     *
     * @param carId The ID of the car to remove from favorites
     */
    fun removeFromFavorites(carId: Long) {
        viewModelScope.launch {
            try {
                favoriteRepository.removeFromFavorites(carId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            if (result.data == true) {
                                // Update status map
                                val newMap = _favoriteStatusMap.value.toMutableMap()
                                newMap[carId] = false
                                _favoriteStatusMap.value = newMap
                                
                                // Reload favorite cars to update the list
                                loadFavoriteCars()
                                
                                Log.d(TAG, "Removed car ID $carId from favorites")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error removing from favorites: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception removing from favorites", e)
            }
        }
    }
    
    /**
     * Check if a car is favorited.
     *
     * @param carId The ID of the car to check
     */
    fun checkFavoriteStatus(carId: Long) {
        viewModelScope.launch {
            try {
                favoriteRepository.isCarFavorited(carId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            // Update status map
                            val newMap = _favoriteStatusMap.value.toMutableMap()
                            newMap[carId] = result.data ?: false
                            _favoriteStatusMap.value = newMap
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error checking favorite status: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception checking favorite status", e)
            }
        }
    }
    
    /**
     * Check favorite status for multiple cars at once.
     *
     * @param carIds List of car IDs to check
     */
    fun checkFavoriteStatusBatch(carIds: List<Long>) {
        for (carId in carIds) {
            checkFavoriteStatus(carId)
        }
    }
} 