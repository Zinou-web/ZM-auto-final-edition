package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.preference.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FavoriteRepositoryImpl"

/**
 * Implementation of the FavoriteRepository interface.
 * Currently uses mock data, but could be updated to use a backend API or local database.
 */
@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : FavoriteRepository {
    
    // In-memory cache of favorite car IDs for the current session
    private val favoriteCars = mutableSetOf<Long>()
    
    // Mock car data - in a real app, this would be fetched from the server or local database
    private val mockCarData = mapOf(
        1L to Car(
            id = 1,
            brand = "Toyota",
            model = "Corolla",
            year = 2022,
            rentalPricePerDay = java.math.BigDecimal(55.0),
            transmission = "Automatic",
            rating = 4,
            colour = "White",
            fuel = "Petrol",
            type = "Sedan"
        ),
        2L to Car(
            id = 2,
            brand = "BMW",
            model = "X5",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(120.0),
            transmission = "Automatic",
            rating = 5,
            colour = "Black",
            fuel = "Diesel",
            type = "SUV"
        ),
        3L to Car(
            id = 3,
            brand = "Mercedes",
            model = "S-Class",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(150.0),
            transmission = "Automatic",
            rating = 5,
            colour = "Silver",
            fuel = "Petrol",
            type = "Luxury"
        )
    )
    
    init {
        // Load saved favorites from preferences or server
        // In a real app, we would load this data from persistent storage
        userPreferences.getFavoriteCarIds()?.let { savedIds ->
            favoriteCars.addAll(savedIds)
            Log.d(TAG, "Loaded ${favoriteCars.size} favorite cars from preferences")
        }
    }
    
    /**
     * Get all favorite cars for the current user
     */
    override fun getFavoriteCars(): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource.loading())
        
        try {
            // Simulate network delay
            kotlinx.coroutines.delay(300)
            
            // Filter mock cars by favorite IDs
            val favorites = mockCarData.filterKeys { favoriteCars.contains(it) }.values.toList()
            emit(ApiResource.success(favorites))
            Log.d(TAG, "Loaded ${favorites.size} favorite cars")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorite cars", e)
            emit(ApiResource.error("Failed to load favorite cars: ${e.message}"))
        }
    }
    
    /**
     * Add a car to favorites
     */
    override fun addToFavorites(carId: Long): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource.loading())
        
        try {
            // Simulate network delay
            kotlinx.coroutines.delay(300)
            
            favoriteCars.add(carId)
            
            // Save updated favorites
            userPreferences.saveFavoriteCarIds(favoriteCars.toList())
            
            emit(ApiResource.success(true))
            Log.d(TAG, "Added car ID $carId to favorites")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to favorites", e)
            emit(ApiResource.error("Failed to add to favorites: ${e.message}"))
        }
    }
    
    /**
     * Remove a car from favorites
     */
    override fun removeFromFavorites(carId: Long): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource.loading())
        
        try {
            // Simulate network delay
            kotlinx.coroutines.delay(300)
            
            favoriteCars.remove(carId)
            
            // Save updated favorites
            userPreferences.saveFavoriteCarIds(favoriteCars.toList())
            
            emit(ApiResource.success(true))
            Log.d(TAG, "Removed car ID $carId from favorites")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from favorites", e)
            emit(ApiResource.error("Failed to remove from favorites: ${e.message}"))
        }
    }
    
    /**
     * Check if a car is in favorites
     */
    override fun isCarFavorited(carId: Long): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource.loading())
        
        try {
            val isFavorite = favoriteCars.contains(carId)
            emit(ApiResource.success(isFavorite))
        } catch (e: Exception) {
            Log.e(TAG, "Error checking favorite status", e)
            emit(ApiResource.error("Failed to check favorite status: ${e.message}"))
        }
    }
} 