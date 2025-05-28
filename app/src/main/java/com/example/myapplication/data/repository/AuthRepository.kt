package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiResource
import kotlinx.coroutines.flow.Flow
import com.example.myapplication.data.api.AuthResponse
import com.example.myapplication.data.api.LoginRequest
import com.example.myapplication.data.api.RegisterRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Interface for authentication-related operations.
 */
interface AuthRepository {
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String): Flow<ApiResource<AuthResponse>>
    
    /**
     * Register a new user
     */
    fun register(registerRequest: RegisterRequest): Flow<ApiResource<AuthResponse>>
    
    /**
     * Check if an email exists in the system
     */
    fun checkEmailExists(email: String): Flow<ApiResource<Boolean>>
    
    /**
     * Request a password reset
     */
    fun requestPasswordReset(email: String): Flow<ApiResource<Boolean>>
    
    /**
     * Verify a password reset code and set new password
     */
    fun verifyPasswordReset(email: String, code: String, newPassword: String): Flow<ApiResource<Boolean>>
    
    /**
     * Log in with Facebook
     */
    fun loginWithFacebook(token: String): Flow<ApiResource<Any>>
    
    /**
     * Log in with Google
     */
    fun loginWithGoogle(token: String): Flow<ApiResource<Any>>
    
    /**
     * Logout the current user
     */
    suspend fun logout()
} 