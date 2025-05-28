package com.example.myapplication.data.api

/**
 * Data class for registration requests.
 * Updated to send firstName and lastName separately to align with backend User entity.
 */
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    // val name: String, // Replaced by firstName and lastName
    val fcmToken: String? = null // For push notifications, optional
) 