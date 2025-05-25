package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.AuthResponse
import com.example.myapplication.data.api.LoginRequest
import com.example.myapplication.data.api.PasswordResetResponse
import com.example.myapplication.data.api.RegisterRequest
import com.example.myapplication.data.preference.AuthPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the AuthRepository interface.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager
) : AuthRepository {
    
    // Use mock data for email/password login when in debug build for testing purposes
    private val useMockData = BuildConfig.DEBUG
    
    override fun isLoggedIn(): Boolean {
        return authPreferenceManager.getAuthToken() != null
    }
    
    override fun login(email: String, password: String): Flow<ApiResource<Any>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock login response instead of calling backend")
                
                // Create mock response with a 1-second delay to simulate network request
                kotlinx.coroutines.delay(1000)
                val mockResponse = AuthResponse(
                    id = 1,
                    userId = 1,
                    token = "mock-auth-token-for-testing",
                    email = email,
                    name = "Test User",
                    success = true
                )
                
                // Store the token
                authPreferenceManager.saveAuthToken(mockResponse.token)
                authPreferenceManager.saveUserId(mockResponse.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Use the actual API call
                Log.d("AuthRepository", "Attempting to login with email: $email")
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)
                
                // Store the token
                authPreferenceManager.saveAuthToken(response.token)
                authPreferenceManager.saveUserId(response.id.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Login failed"))
        }
    }
    
    override fun register(name: String, email: String, password: String, phone: String): Flow<ApiResource<Any>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock registration response instead of calling backend")
                
                // Create mock response with a 1-second delay to simulate network request
                kotlinx.coroutines.delay(1000)
                val mockResponse = AuthResponse(
                    id = 1,
                    userId = 1,
                    token = "mock-auth-token-for-testing",
                    email = email,
                    name = name,
                    success = true
                )
                
                // Store the token
                authPreferenceManager.saveAuthToken(mockResponse.token)
                authPreferenceManager.saveUserId(mockResponse.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Use the actual API call
                Log.d("AuthRepository", "Attempting to register user: $email")
                val registerRequest = RegisterRequest(name, email, password, phone)
                val response = apiService.register(registerRequest)
                
                // Store the token
                authPreferenceManager.saveAuthToken(response.token)
                authPreferenceManager.saveUserId(response.id.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration error: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Registration failed"))
        }
    }
    
    override fun checkEmailExists(email: String): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock email check response instead of calling backend")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Mock: every email except test@exists.com doesn't exist
                val exists = email == "test@exists.com"
                emit(ApiResource(status = ApiStatus.SUCCESS, data = exists))
            } else {
                val response = apiService.checkEmailExists(email)
                val exists = response["exists"] ?: false
                emit(ApiResource(status = ApiStatus.SUCCESS, data = exists))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to check email"))
        }
    }
    
    override fun requestPasswordReset(email: String): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock password reset request response")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Always return success for mock
                emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
            } else {
                apiService.requestPasswordReset(email)
                // If no exception, we consider it successful
                emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to request password reset"))
        }
    }
    
    override fun verifyPasswordReset(email: String, code: String, newPassword: String): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock password reset verification response")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Mock: if code is "123456", verification succeeds
                val success = code == "123456"
                emit(ApiResource(status = ApiStatus.SUCCESS, data = success))
            } else {
                val response = apiService.verifyPasswordReset(email, code, newPassword)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response.success))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to verify password reset"))
        }
    }
    
    override fun loginWithFacebook(token: String): Flow<ApiResource<Any>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock Facebook login response")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Create mock response
                val mockResponse = AuthResponse(
                    id = 1,
                    userId = 1,
                    token = "mock-fb-auth-token-for-testing",
                    email = "facebook-user@example.com",
                    name = "Facebook User",
                    success = true
                )
                
                // Store the token
                authPreferenceManager.saveAuthToken(mockResponse.token)
                authPreferenceManager.saveUserId(mockResponse.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Simulate Facebook login using the OAuth2 redirect endpoint
                val response = apiService.handleOAuth2Redirect(token, 0) // UserId will be returned by the server
                
                // Store the token
                authPreferenceManager.saveAuthToken(response.token)
                authPreferenceManager.saveUserId(response.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Facebook login failed"))
        }
    }
    
    override fun loginWithGoogle(token: String): Flow<ApiResource<Any>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock Google login response")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // Create mock response
                val mockResponse = AuthResponse(
                    id = 1,
                    userId = 1,
                    token = "mock-google-auth-token-for-testing",
                    email = "google-user@example.com",
                    name = "Google User",
                    success = true
                )
                
                // Store the token
                authPreferenceManager.saveAuthToken(mockResponse.token)
                authPreferenceManager.saveUserId(mockResponse.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Simulate Google login using the OAuth2 redirect endpoint
                val response = apiService.handleOAuth2Redirect(token, 0) // UserId will be returned by the server
                
                // Store the token
                authPreferenceManager.saveAuthToken(response.token)
                authPreferenceManager.saveUserId(response.userId.toString())
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Google login failed"))
        }
    }
    
    override suspend fun logout() {
        authPreferenceManager.clearAuthData()
    }
} 