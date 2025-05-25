package com.example.myapplication.data.api

/**
 * Data class representing authentication responses from the API.
 */
data class AuthResponse(
    val id: Long = 0,
    val userId: Long = 0,
    val token: String,
    val email: String? = null,
    val name: String? = null,
    val success: Boolean = true,
    val message: String? = null
)

/**
 * Data class for login request payload
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class for registration request payload
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
) 