package com.example.myapplication.ui.viewmodel

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.auth.GoogleAuthManager
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

private const val TAG = "SocialLoginViewModel"

@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAuthManager: GoogleAuthManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<SocialLoginUiState>(SocialLoginUiState.Initial)
    val loginState: StateFlow<SocialLoginUiState> = _loginState
    
    private var lastWebClientId: String? = null
    private var initializationAttempts = 0
    
    /**
     * Initialize Google Sign-In
     * @param webClientId Your web client ID from Google Developer Console
     * @return true if initialization was successful
     */
    fun initializeGoogleSignIn(webClientId: String): Boolean {
        Log.d(TAG, "Initializing Google Sign-In with client ID ending: ...${webClientId.takeLast(10)}")
        lastWebClientId = webClientId
        initializationAttempts++
        
        try {
            val success = googleAuthManager.forceReInitialize(webClientId, "SocialLoginViewModel")
            
            if (success) {
                Log.d(TAG, "Google Sign-In initialization successful")
            } else {
                Log.e(TAG, "Google Sign-In initialization failed (attempt $initializationAttempts)")
            }
            
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Google Sign-In initialization: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Retry initialization
     */
    fun retryInitialization(): Boolean {
        return lastWebClientId?.let { webClientId ->
            Log.d(TAG, "Retrying Google Sign-In initialization (attempt ${initializationAttempts + 1})")
            initializeGoogleSignIn(webClientId)
        } ?: false
    }
    
    /**
     * Check if Google Sign-In is initialized
     */
    fun isGoogleSignInInitialized(): Boolean {
        val initialized = googleAuthManager.isInitialized()
        Log.d(TAG, "Checking Google Sign-In initialization: $initialized")
        return initialized
    }
    
    /**
     * Provide the Intent to launch Google Sign-In UI
     */
    fun getSignInIntent(): Intent? = googleAuthManager.getSignInIntent()
    
    /**
     * Login with Google token
     */
    private fun loginWithGoogle(token: String) {
        viewModelScope.launch {
            Log.d(TAG, "Logging in with Google token")
            _loginState.value = SocialLoginUiState.Loading
            
            authRepository.loginWithGoogle(token).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        Log.d(TAG, "Google login successful")
                        _loginState.value = SocialLoginUiState.Success
                    }
                    ApiStatus.ERROR -> {
                        Log.e(TAG, "Google login failed: ${result.message}")
                        _loginState.value = SocialLoginUiState.Error(result.message ?: "Google login failed")
                    }
                    ApiStatus.LOADING -> {
                        _loginState.value = SocialLoginUiState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Login with Facebook token
     */
    fun loginWithFacebook(token: String) {
        viewModelScope.launch {
            Log.d(TAG, "Logging in with Facebook token")
            _loginState.value = SocialLoginUiState.Loading
            
            authRepository.loginWithFacebook(token).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        Log.d(TAG, "Facebook login successful")
                        _loginState.value = SocialLoginUiState.Success
                    }
                    ApiStatus.ERROR -> {
                        Log.e(TAG, "Facebook login failed: ${result.message}")
                        _loginState.value = SocialLoginUiState.Error(result.message ?: "Facebook login failed")
                    }
                    ApiStatus.LOADING -> {
                        _loginState.value = SocialLoginUiState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Login with email and password
     */
    fun loginWithEmail(email: String, password: String) {
        Log.d(TAG, "Logging in with email: $email")
        viewModelScope.launch {
            _loginState.value = SocialLoginUiState.Loading
            authRepository.login(email, password).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        Log.d(TAG, "Email login successful")
                        _loginState.value = SocialLoginUiState.Success
                    }
                    ApiStatus.ERROR -> {
                        Log.e(TAG, "Email login failed: ${result.message}")
                        _loginState.value = SocialLoginUiState.Error(result.message ?: "Login failed")
                    }
                    ApiStatus.LOADING -> {
                        _loginState.value = SocialLoginUiState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Reset the login state
     */
    fun resetState() {
        _loginState.value = SocialLoginUiState.Initial
    }
    
    override fun onCleared() {
        super.onCleared()
        googleAuthManager.resetLauncher()
    }

    // Add this method to allow direct creation of the sign-in client
    fun ensureDirectInitialization(activity: ComponentActivity): Boolean {
        try {
            Log.d(TAG, "Ensuring direct initialization of Google Sign-In")
            
            // Get the web client ID - try multiple methods
            val webClientId = try {
                // Try to get from resources
                val resourceId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
                if (resourceId != 0) {
                    activity.getString(resourceId)
                } else {
                    // Try shared preferences
                    activity.getSharedPreferences("google_auth", android.content.Context.MODE_PRIVATE)
                        .getString("web_client_id", null) ?: ""
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting web client ID: ${e.message}")
                return false
            }
            
            if (webClientId.isEmpty()) {
                Log.e(TAG, "Web client ID is empty or could not be retrieved")
                return false
            }
            
            // Store for future use
            lastWebClientId = webClientId
            
            // Initialize through GoogleAuthManager
            val initialized = googleAuthManager.forceReInitialize(webClientId, "SocialLoginViewModel_direct")
            
            // Also try direct initialization
            try {
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                
                // Create the client with application context
                val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                    activity.applicationContext, gso)
                
                // Save client ID for future use
                activity.getSharedPreferences("google_auth", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putString("web_client_id", webClientId)
                    .apply()
                
                Log.d(TAG, "Direct initialization attempted")
            } catch (e: Exception) {
                Log.e(TAG, "Error in direct initialization: ${e.message}")
            }
            
            // Register if initialized
            if (initialized) {
                googleAuthManager.preRegister(activity)
                // Initialization completed; wait for user to trigger sign-in
                return true
            }
            
            return initialized
        } catch (e: Exception) {
            Log.e(TAG, "Exception in ensureDirectInitialization: ${e.message}", e)
            return false
        }
    }

    /**
     * Handle the result Intent from Google Sign-In launcher
     */
    fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                Log.d(TAG, "Google Sign-In successful, proceeding to login")
                loginWithGoogle(idToken)
            } else {
                Log.e(TAG, "Google Sign-In failed: no token returned")
                _loginState.value = SocialLoginUiState.Error("Google sign-in failed: no token")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with status code: ${e.statusCode}", e)
            _loginState.value = SocialLoginUiState.Error("Google sign-in failed: ${e.statusCode}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception handling Google Sign-In result: ${e.message}", e)
            _loginState.value = SocialLoginUiState.Error("Google sign-in error: ${e.message}")
        }
    }
}

/**
 * UI state for social login flow
 */
sealed class SocialLoginUiState {
    object Initial : SocialLoginUiState()
    object Loading : SocialLoginUiState()
    object Success : SocialLoginUiState()
    data class Error(val message: String) : SocialLoginUiState()
} 