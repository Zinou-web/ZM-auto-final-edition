package com.example.myapplication.data.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class for managing user preferences using SharedPreferences.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCE_NAME, Context.MODE_PRIVATE
    )
    
    private val gson = Gson()

    /**
     * Save JWT token for authenticated user
     */
    fun saveAuthToken(token: String) {
        sharedPreferences.edit {
            putString(KEY_AUTH_TOKEN, token)
            apply()
        }
    }

    /**
     * Get saved JWT token
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Clear authentication token when user logs out
     */
    fun clearAuthToken() {
        sharedPreferences.edit {
            remove(KEY_AUTH_TOKEN)
            apply()
        }
    }
    
    /**
     * Save favorite car IDs for the current user
     */
    fun saveFavoriteCarIds(carIds: List<Long>) {
        val json = gson.toJson(carIds)
        sharedPreferences.edit {
            putString(KEY_FAVORITE_CARS, json)
            apply()
        }
    }
    
    /**
     * Get favorite car IDs for the current user
     */
    fun getFavoriteCarIds(): List<Long>? {
        val json = sharedPreferences.getString(KEY_FAVORITE_CARS, null) ?: return null
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Clear all user data when user logs out
     */
    fun clearAllUserData() {
        sharedPreferences.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_FAVORITE_CARS)
            // Add other user-specific keys here to clear
            apply()
        }
    }

    companion object {
        private const val PREFERENCE_NAME = "car_rental_preferences"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_FAVORITE_CARS = "favorite_cars"
        // Add other preference keys here
    }
} 