package com.example.myapplication.data.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "auth_preferences",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN_EXPIRES = "token_expires"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
        private const val KEY_USER_PHONE = "user_phone"
    }
    
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    fun getAuthToken(): String? = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)
    
    fun saveTokenExpiry(expiresIn: Long) {
        val expirationTime = System.currentTimeMillis() + expiresIn * 1000
        sharedPreferences.edit().putLong(KEY_TOKEN_EXPIRES, expirationTime).apply()
    }
    
    fun isTokenExpired(): Boolean {
        val expirationTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRES, 0)
        return System.currentTimeMillis() > expirationTime
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }
    
    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)
    
    fun saveUserPhone(phone: String) {
        sharedPreferences.edit().putString(KEY_USER_PHONE, phone).apply()
    }
    
    fun getUserPhone(): String? = sharedPreferences.getString(KEY_USER_PHONE, null)
    
    fun saveUserName(userName: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, userName).apply()
    }
    
    fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)
    
    fun saveUserProfileImage(imageUrl: String) {
        sharedPreferences.edit().putString(KEY_USER_PROFILE_IMAGE, imageUrl).apply()
    }
    
    fun getUserProfileImage(): String? = sharedPreferences.getString(KEY_USER_PROFILE_IMAGE, null)
    
    fun clearAuthData() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_TOKEN_EXPIRES)
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PROFILE_IMAGE)
            .apply()
    }
} 