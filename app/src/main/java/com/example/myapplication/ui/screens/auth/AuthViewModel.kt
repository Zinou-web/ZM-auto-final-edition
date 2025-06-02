package com.example.myapplication.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.RegisterRequest
import com.example.myapplication.data.auth.FacebookAuthHelper
import com.example.myapplication.data.auth.GoogleAuthHelper
import com.example.myapplication.data.repository.AuthRepository
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.FacebookSdk
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

/**
 * Sealed class representing the different states of the authentication UI
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * ViewModel that handles authentication logic and UI state.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private var facebookAuthHelper: FacebookAuthHelper? = null
    private var googleAuthHelper: GoogleAuthHelper? = null

    init {
        initializeAuthHelpers()
        
        // Check if user is already logged in
        if (authRepository.isLoggedIn()) {
            _uiState.value = AuthUiState.Success("auto_login")
        }
    }

    private fun initializeAuthHelpers() {
        // Initialize Google authentication helper
        try {
            googleAuthHelper = GoogleAuthHelper(
                context = context,
                onSuccess = { token ->
                    handleGoogleToken(token)
                },
                onError = { error ->
                    _uiState.value = AuthUiState.Error(error)
                    Log.e(TAG, "Google authentication error: $error")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Google auth helper", e)
            _uiState.value = AuthUiState.Error("Failed to initialize Google authentication")
        }

        // Facebook login temporarily disabled
        facebookAuthHelper = null
    }

    /**
     * Handle email/password login.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        Log.d(TAG, "Starting login process for email: $email")
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            Log.d(TAG, "Set UI state to Loading")
            
            authRepository.login(email, password).collectLatest { result ->
                Log.d(TAG, "Received login result with status: ${result.status}")
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { responseData ->
                            Log.d(TAG, "Login successful, User ID: ${responseData.userId}")
                            _uiState.value = AuthUiState.Success("login_success")
                        } ?: run {
                            Log.e(TAG, "Login succeeded but response data is null")
                            _uiState.value = AuthUiState.Error("Login succeeded but no user data returned")
                        }
                    }
                    ApiStatus.ERROR -> {
                        Log.e(TAG, "Login API error: ${result.message}")
                        _uiState.value = AuthUiState.Error(result.message ?: "Login failed")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                        Log.d(TAG, "Login API status is still loading")
                    }
                }
            }
        }
    }

    /**
     * Handle user registration with first name, last name, email and password
     */
    fun register(firstName: String, lastName: String, email: String, password: String, phone: String = "") {
        // Only email and password are required here; firstName and lastName collected later
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and Password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val registerRequest = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                password = password
                // fcmToken can be added here if needed later
            )
            
            authRepository.register(registerRequest).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { responseData ->
                            Log.d(TAG, "Registration successful, User ID: ${responseData.userId}")
                            _uiState.value = AuthUiState.Success("registration_success")
                        } ?: run {
                            _uiState.value = AuthUiState.Error("Registration succeeded but no user data returned")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = AuthUiState.Error(result.message ?: "Registration failed")
                        Log.e(TAG, "Registration failed: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }

    /**
     * Check if an email exists in the system
     */
    fun checkEmailExists(email: String, onResult: (Boolean) -> Unit) {
        if (email.isBlank()) {
            onResult(false)
            return
        }
        
        viewModelScope.launch {
            authRepository.checkEmailExists(email).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { exists ->
                            onResult(exists)
                        } ?: run {
                            onResult(false)
                        }
                    }
                    ApiStatus.ERROR -> {
                        onResult(false)
                        Log.e(TAG, "Email check failed: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Do nothing while loading
                    }
                }
            }
        }
    }

    /**
     * Handle Facebook login button click.
     */
    fun loginWithFacebook(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        if (facebookAuthHelper == null) {
            _uiState.value = AuthUiState.Error("Facebook authentication not initialized")
            return
        }
        try {
            _uiState.value = AuthUiState.Loading
            facebookAuthHelper?.login(launcher)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Failed to start Facebook login")
            Log.e(TAG, "Error starting Facebook login", e)
        }
    }

    /**
     * Handle Google login button click.
     */
    fun loginWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        if (googleAuthHelper == null) {
            _uiState.value = AuthUiState.Error("Google authentication not initialized")
            return
        }
        try {
            _uiState.value = AuthUiState.Loading
            googleAuthHelper?.signIn(launcher)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Failed to start Google login")
            Log.e(TAG, "Error starting Google login", e)
        }
    }

    /**
     * Handle Facebook login result from activity.
     */
    fun handleFacebookActivityResult(requestCode: Int, data: Intent?) {
        if (facebookAuthHelper == null) {
            _uiState.value = AuthUiState.Error("Facebook authentication not initialized")
            return
        }
        try {
            facebookAuthHelper?.getCallbackManager()?.onActivityResult(requestCode, requestCode, data)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Failed to process Facebook login result")
            Log.e(TAG, "Error handling Facebook activity result", e)
        }
    }

    /**
     * Handle Google login result from activity.
     */
    fun handleGoogleActivityResult(task: Task<GoogleSignInAccount>) {
        if (googleAuthHelper == null) {
            _uiState.value = AuthUiState.Error("Google authentication not initialized")
            return
        }
        try {
            googleAuthHelper?.handleSignInResult(task)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Failed to process Google login result")
            Log.e(TAG, "Error handling Google activity result", e)
        }
    }

    /**
     * Process Facebook access token and authenticate with backend.
     */
    private fun handleFacebookToken(token: String) {
        if (token.isBlank()) {
            _uiState.value = AuthUiState.Error("Invalid Facebook token")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                authRepository.loginWithFacebook(token).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { responseData ->
                                val userId = when (responseData) {
                                    is com.example.myapplication.data.api.AuthResponse -> responseData.userId?.toString() ?: "unknown_fb_user"
                                    is Map<*, *> -> (responseData["userId"] ?: responseData["id"])?.toString() ?: "unknown_fb_user_map"
                                    else -> "unknown_fb_user_type"
                                }
                                _uiState.value = AuthUiState.Success("Login successful for user ID: $userId")
                                Log.d(TAG, "User logged in with Facebook successfully: ID $userId")
                            } ?: run {
                                _uiState.value = AuthUiState.Error("Facebook login succeeded but no user data returned")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = AuthUiState.Error(result.message ?: "Facebook login failed")
                            Log.e(TAG, "Facebook login failed: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                // If the method doesn't exist, fallback to a generic error message
                _uiState.value = AuthUiState.Error("Facebook login is not implemented yet")
                Log.e(TAG, "Facebook login method not available", e)
            }
        }
    }

    /**
     * Process Google ID token and authenticate with backend.
     */
    private fun handleGoogleToken(token: String) {
        if (token.isBlank()) {
            _uiState.value = AuthUiState.Error("Invalid Google token")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                authRepository.loginWithGoogle(token).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { responseData ->
                                val userId = when (responseData) {
                                    is com.example.myapplication.data.api.AuthResponse -> responseData.userId?.toString() ?: "unknown_google_user"
                                    is Map<*, *> -> (responseData["userId"] ?: responseData["id"])?.toString() ?: "unknown_google_user_map"
                                    else -> "unknown_google_user_type"
                                }
                                _uiState.value = AuthUiState.Success("Login successful for user ID: $userId")
                                Log.d(TAG, "User logged in with Google successfully: ID $userId")
                            } ?: run {
                                _uiState.value = AuthUiState.Error("Google login succeeded but no user data returned")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = AuthUiState.Error(result.message ?: "Google login failed")
                            Log.e(TAG, "Google login failed: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                // If the method doesn't exist, fallback to a generic error message
                _uiState.value = AuthUiState.Error("Google login is not implemented yet")
                Log.e(TAG, "Google login method not available", e)
            }
        }
    }

    /**
     * Log the user out.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
    
    /**
     * Request a password reset for the given email.
     */
    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Email cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            authRepository.requestPasswordReset(email).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        _uiState.value = AuthUiState.Success("password_reset_requested")
                        Log.d(TAG, "Password reset requested successfully for email: $email")
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = AuthUiState.Error(result.message ?: "Password reset request failed")
                        Log.e(TAG, "Password reset request failed: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Verify a password reset code and set a new password.
     */
    fun resetPassword(email: String, code: String, newPassword: String) {
        if (email.isBlank() || code.isBlank() || newPassword.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            try {
                // Attempt to use verifyPasswordReset method if it exists
                authRepository.verifyPasswordReset(email, code, newPassword).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            _uiState.value = AuthUiState.Success("password_reset_success")
                            Log.d(TAG, "Password reset completed successfully for email: $email")
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = AuthUiState.Error(result.message ?: "Password reset failed")
                            Log.e(TAG, "Password reset failed: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                // If the method doesn't exist, fallback to a generic error message
                _uiState.value = AuthUiState.Error("Password reset is not implemented yet")
                Log.e(TAG, "Password reset method not available", e)
            }
        }
    }

    /**
     * Reset the UI state to Idle. Useful for composables to clear previous states.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
