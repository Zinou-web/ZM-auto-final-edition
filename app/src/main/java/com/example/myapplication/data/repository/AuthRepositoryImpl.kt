package com.example.myapplication.data.repository

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Implementation of the AuthRepository interface.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager,
    @ApplicationContext private val context: Context
) : AuthRepository {
    
    private var useMockData = false // Set to true to use mock data
    
    override fun isLoggedIn(): Boolean {
        return authPreferenceManager.getAuthToken() != null
    }
    
    override fun login(email: String, password: String): Flow<ApiResource<AuthResponse>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock login response instead of calling backend")
                
                kotlinx.coroutines.delay(1000)
                val mockResponse = AuthResponse(
                    userId = 1L,
                    token = "mock-auth-token-for-testing",
                    email = email,
                    name = "Test User",
                    success = true,
                    emailVerified = true,
                    profileImageUrl = "https://example.com/default.jpg"
                )
                
                authPreferenceManager.saveAuthToken(mockResponse.token ?: "")
                authPreferenceManager.saveUserId(mockResponse.userId?.toString() ?: "0")
                mockResponse.email?.let { authPreferenceManager.saveUserEmail(it) }
                mockResponse.name?.let { authPreferenceManager.saveUserName(it) }
                mockResponse.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                mockResponse.expiresIn?.let { authPreferenceManager.saveTokenExpiry(it) }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Use the actual API call
                Log.d("AuthRepository", "Attempting to login with email: $email")
                val loginRequest = LoginRequest(email, password)
                val response = apiService.login(loginRequest)
                
                authPreferenceManager.saveUserId(response.userId?.toString() ?: "0")
                response.email?.let { authPreferenceManager.saveUserEmail(it) }
                response.name?.let { authPreferenceManager.saveUserName(it) }
                response.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)

                Log.d("AuthRepository", "Login API call successful for email: $email. User ID: ${response.userId}")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response, message = "Login successful"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Login failed"))
        }
    }
    
    override fun register(registerRequest: RegisterRequest): Flow<ApiResource<AuthResponse>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Use mock data for testing
                Log.d("AuthRepository", "Using mock registration response instead of calling backend")
                
                kotlinx.coroutines.delay(1000)
                val mockResponse = createMockAuthResponse(registerRequest.email)
                
                authPreferenceManager.saveAuthToken(mockResponse.token ?: "")
                authPreferenceManager.saveUserId(mockResponse.userId?.toString() ?: "0")
                mockResponse.email?.let { authPreferenceManager.saveUserEmail(it) }
                mockResponse.name?.let { authPreferenceManager.saveUserName(it) }
                mockResponse.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                mockResponse.expiresIn?.let { authPreferenceManager.saveTokenExpiry(it) }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Use the actual API call with the provided RegisterRequest object
                val response = apiService.register(registerRequest)
                
                authPreferenceManager.saveUserId(response.userId?.toString() ?: "0")
                response.email?.let { authPreferenceManager.saveUserEmail(it) }
                response.name?.let { authPreferenceManager.saveUserName(it) }
                response.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)

                Log.d("AuthRepository", "Registration API call successful for email: ${registerRequest.email}. User ID: ${response.userId}")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response, message = "Registration successful"))
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
                
                kotlinx.coroutines.delay(1000)
                val mockResponse = AuthResponse(
                    userId = 1L,
                    token = "mock-fb-auth-token-for-testing",
                    email = "facebook-user@example.com",
                    name = "Facebook User",
                    success = true,
                    emailVerified = true,
                    profileImageUrl = "https://example.com/fb.jpg"
                )
                
                authPreferenceManager.saveAuthToken(mockResponse.token ?: "")
                authPreferenceManager.saveUserId(mockResponse.userId?.toString() ?: "0")
                mockResponse.email?.let { authPreferenceManager.saveUserEmail(it) }
                mockResponse.name?.let { authPreferenceManager.saveUserName(it) }
                mockResponse.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                mockResponse.expiresIn?.let { authPreferenceManager.saveTokenExpiry(it) }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Simulate Facebook login using the OAuth2 redirect endpoint
                val response = apiService.handleOAuth2Redirect(token, 0) // UserId from server
                
                authPreferenceManager.saveUserId(response.userId?.toString() ?: "0")
                response.email?.let { authPreferenceManager.saveUserEmail(it) }
                response.name?.let { authPreferenceManager.saveUserName(it) }
                response.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                
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
                
                kotlinx.coroutines.delay(1000)
                val mockResponse = AuthResponse(
                    userId = 1L,
                    token = "mock-google-auth-token-for-testing",
                    email = "google-user@example.com",
                    name = "Google User",
                    success = true,
                    emailVerified = true,
                    profileImageUrl = "https://example.com/google.jpg"
                )
                
                authPreferenceManager.saveAuthToken(mockResponse.token ?: "")
                authPreferenceManager.saveUserId(mockResponse.userId?.toString() ?: "0")
                mockResponse.email?.let { authPreferenceManager.saveUserEmail(it) }
                mockResponse.name?.let { authPreferenceManager.saveUserName(it) }
                mockResponse.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                mockResponse.expiresIn?.let { authPreferenceManager.saveTokenExpiry(it) }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockResponse))
            } else {
                // Simulate Google login using the OAuth2 redirect endpoint
                val response = apiService.handleOAuth2Redirect(token, 0) // UserId from server
                
                authPreferenceManager.saveUserId(response.userId?.toString() ?: "0")
                response.email?.let { authPreferenceManager.saveUserEmail(it) }
                response.name?.let { authPreferenceManager.saveUserName(it) }
                response.profileImageUrl?.let { authPreferenceManager.saveUserProfileImage(it) }
                authPreferenceManager.setLoggedIn(true)
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Google login failed"))
        }
    }
    
    override suspend fun logout() {
        try {
            // Call the backend logout endpoint first
            val response = apiService.logout() // Assumes ApiService has a logout() suspend fun
            if (response.isSuccessful) {
                Log.d("AuthRepositoryImpl", "Backend logout successful")
            } else {
                // Log error or handle unsuccessful backend logout if needed
                Log.w("AuthRepositoryImpl", "Backend logout failed or returned error: ${response.code()}")
            }
        } catch (e: Exception) {
            // Log or handle network errors during logout call
            Log.e("AuthRepositoryImpl", "Error calling backend logout: ${e.message}", e)
        }
        // Always clear local data regardless of backend call success
        authPreferenceManager.clearAuthData()
        authPreferenceManager.setLoggedIn(false) // Ensure loggedIn flag is also cleared
        Log.d("AuthRepositoryImpl", "Local auth data cleared")
    }

    private fun createMockAuthResponse(email: String): AuthResponse {
        // This mock might need adjustment if we want to test firstName/lastName with mock data too.
        // For now, it only uses email to generate a mock name.
        val mockName = email.split("@")[0]
        return AuthResponse(
            userId = 123L,
            token = "mock_jwt_token_for_${email}",
            email = email,
            name = mockName,
            expiresIn = 3600L,
            profileImageUrl = "https://example.com/mockimage.jpg",
            emailVerified = true,
            success = true
        )
    }
} 