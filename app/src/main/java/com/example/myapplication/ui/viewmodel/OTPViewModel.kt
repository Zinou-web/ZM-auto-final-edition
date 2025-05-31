package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.preference.AuthPreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for OTP verification screen
 */
sealed class OTPUiState {
    object Initial : OTPUiState()
    object Loading : OTPUiState()
    object VerificationSuccess : OTPUiState()
    object ResendSuccess : OTPUiState()
    data class Error(val message: String) : OTPUiState()
}

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authPreferenceManager: AuthPreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow<OTPUiState>(OTPUiState.Initial)
    val state: StateFlow<OTPUiState> = _state.asStateFlow()

    /**
     * Verify the email with the provided OTP code
     */
    fun verifyEmail(code: String) {
        val userIdStr = authPreferenceManager.getUserId()
        val userId = userIdStr?.toLongOrNull()
        if (userId == null) {
            _state.value = OTPUiState.Error("User not found")
            return
        }
        viewModelScope.launch {
            _state.value = OTPUiState.Loading
            authRepository.verifyEmail(userId, code).collectLatest { result ->
                _state.value = when (result.status) {
                    ApiStatus.SUCCESS -> OTPUiState.VerificationSuccess
                    ApiStatus.ERROR -> OTPUiState.Error(result.message ?: "Verification failed")
                    ApiStatus.LOADING -> OTPUiState.Loading
                }
            }
        }
    }

    /**
     * Resend the OTP code to the user's email
     */
    fun resendOtp() {
        val userIdStr = authPreferenceManager.getUserId()
        val userId = userIdStr?.toLongOrNull()
        if (userId == null) {
            _state.value = OTPUiState.Error("User not found")
            return
        }
        viewModelScope.launch {
            _state.value = OTPUiState.Loading
            authRepository.resendOtp(userId).collectLatest { result ->
                _state.value = when (result.status) {
                    ApiStatus.SUCCESS -> OTPUiState.ResendSuccess
                    ApiStatus.ERROR -> OTPUiState.Error(result.message ?: "Resend OTP failed")
                    ApiStatus.LOADING -> OTPUiState.Loading
                }
            }
        }
    }

    /**
     * Verify password reset code and update password
     */
    fun verifyPasswordReset(code: String, newPassword: String) {
        val email = authPreferenceManager.getUserEmail()
        if (email.isNullOrEmpty()) {
            _state.value = OTPUiState.Error("User email not found")
            return
        }
        viewModelScope.launch {
            _state.value = OTPUiState.Loading
            authRepository.verifyPasswordReset(email, code, newPassword).collectLatest { result ->
                _state.value = when (result.status) {
                    ApiStatus.SUCCESS -> OTPUiState.VerificationSuccess
                    ApiStatus.ERROR -> OTPUiState.Error(result.message ?: "Password reset failed")
                    ApiStatus.LOADING -> OTPUiState.Loading
                }
            }
        }
    }

    /**
     * Reset the UI state to initial
     */
    fun resetState() {
        _state.value = OTPUiState.Initial
    }

    /**
     * Get current user's email from preferences
     */
    fun getUserEmail(): String? = authPreferenceManager.getUserEmail()
} 