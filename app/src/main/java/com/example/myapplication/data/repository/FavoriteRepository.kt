package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.model.Car
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for favorite car-related operations.
 */
interface FavoriteRepository {
    /**
     * Get all favorite cars for the current user
     */
    fun getFavoriteCars(): Flow<ApiResource<List<Car>>>
    
    /**
     * Add a car to favorites
     */
    fun addToFavorites(carId: Long): Flow<ApiResource<Boolean>>
    
    /**
     * Remove a car from favorites
     */
    fun removeFromFavorites(carId: Long): Flow<ApiResource<Boolean>>
    
    /**
     * Check if a car is in favorites
     */
    fun isCarFavorited(carId: Long): Flow<ApiResource<Boolean>>
} 