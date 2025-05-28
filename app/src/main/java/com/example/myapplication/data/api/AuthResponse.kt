package com.example.myapplication.data.api

import com.google.gson.annotations.SerializedName

/**
 * Data class representing authentication responses from the API.
 * Adjusted to better align with potential backend responses and session-based flow.
 */
data class AuthResponse(
    // @SerializedName("id") // If backend sends 'id' for user's primary key
    // val backendLegacyId: Long? = null, // Example if backend used 'id' for user PK

    @SerializedName("userId") // Assuming backend sends 'userId' as the primary user identifier
    val userId: Long? = null, // Made nullable, should be primary ID from backend

    val token: String? = null, // Will hold session ID from backend, not JWT. Made nullable.
    val email: String? = null,
    val name: String? = null,
    val expiresIn: Long? = null, // Backend likely won't send this for session auth. Made nullable.
    val profileImageUrl: String? = null,
    val emailVerified: Boolean? = null, // Added to match backend LoginResponse

    // These fields might not be directly from backend auth response but useful for app state
    val success: Boolean = true, // App can determine this based on HTTP response
    val message: String? = null  // App can set this for UI messages
)

// Removed LoginRequest and RegisterRequest from here as they should be in their own files.
// /**
//  * Data class for login request payload
//  */
// data class LoginRequest(
//     val email: String,
//     val password: String
// )
// 
// /**
//  * Data class for registration request payload
//  */
// data class RegisterRequest(
//     val name: String,
//     val email: String,
//     val password: String,
//     val phone: String
// ) 