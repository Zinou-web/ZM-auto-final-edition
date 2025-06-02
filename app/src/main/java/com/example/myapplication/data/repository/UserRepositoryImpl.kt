package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.ImageUploadResponse
import com.example.myapplication.data.api.UpdateProfileRequest
import com.example.myapplication.data.model.User
import com.example.myapplication.data.preference.AuthPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.data.api.NetworkResponse

/**
 * Implementation of the UserRepository interface for user-related operations.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager
) : UserRepository {
    
    override fun getCurrentUser(): Flow<ApiResource<User>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val authToken = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val wrapper: NetworkResponse<User> = apiService.getCurrentUser("Bearer $authToken")
            if (!wrapper.success || wrapper.data == null) throw Exception(wrapper.message ?: "Failed to load profile")
            val user = wrapper.data
            emit(ApiResource(status = ApiStatus.SUCCESS, data = user))
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get user profile"))
        }
    }
    
    override fun updateProfile(name: String, email: String, phone: String): Flow<ApiResource<User>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val authToken = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            
            val request = UpdateProfileRequest(
                name = name,
                email = email,
                phone = phone
            )
            
            val updatedUser: User = apiService.updateUserProfile(userId, request, "Bearer $authToken")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = updatedUser))
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to update profile"))
        }
    }
    
    override fun changePassword(currentPassword: String, newPassword: String): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val authToken = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            
            val wrapper: NetworkResponse<Void> = apiService.changePassword(currentPassword, newPassword, "Bearer $authToken")
            if (!wrapper.success) throw Exception(wrapper.message ?: "Change password failed")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to change password"))
        }
    }
    
    override fun uploadProfileImage(image: MultipartBody.Part): Flow<ApiResource<String>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val authToken = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            
            val wrapper: NetworkResponse<ImageUploadResponse> = apiService.uploadProfileImage(image, "Bearer $authToken")
            if (!wrapper.success || wrapper.data == null) throw Exception(wrapper.message ?: "Upload image failed")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = wrapper.data.url))
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to upload profile image"))
        }
    }
    
    override fun deleteAccount(): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val authToken = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            
            val wrapper: NetworkResponse<Void> = apiService.deleteAccount(userId, "Bearer $authToken")
            if (!wrapper.success) throw Exception(wrapper.message ?: "Delete account failed")
            authPreferenceManager.clearAuthData()
            emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to delete account"))
        }
    }
} 