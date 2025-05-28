package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.RegisterRequest
import com.example.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val loginState: StateFlow<LoginUiState> = _loginState

    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            
            authRepository.login(email, password).collectLatest { result ->
                _loginState.value = when (result.status) {
                    ApiStatus.SUCCESS -> LoginUiState.Success
                    ApiStatus.ERROR -> LoginUiState.Error(result.message ?: "Unknown error occurred")
                    ApiStatus.LOADING -> LoginUiState.Loading
                }
            }
        }
    }

    /**
     * Register a new user
     */
    fun register(firstName: String, lastName: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            
            val registerRequest = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                password = password
            )
            
            authRepository.register(registerRequest).collectLatest { result ->
                _loginState.value = when (result.status) {
                    ApiStatus.SUCCESS -> LoginUiState.RegisterSuccess
                    ApiStatus.ERROR -> LoginUiState.Error(result.message ?: "Unknown error occurred")
                    ApiStatus.LOADING -> LoginUiState.Loading
                }
            }
        }
    }
    
    /**
     * Reset the login state
     */
    fun resetState() {
        _loginState.value = LoginUiState.Initial
    }
}

/**
 * UI state for login/register flow
 */
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    object RegisterSuccess : LoginUiState()
    data class Error(val message: String) : LoginUiState()
} 