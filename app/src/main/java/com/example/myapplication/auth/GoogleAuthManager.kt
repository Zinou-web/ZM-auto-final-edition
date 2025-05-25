package com.example.myapplication.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleAuthManager"

/**
 * Manager class for Google Authentication
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var googleSignInClient: GoogleSignInClient? = null
    private var signInResultCallback: ((String?) -> Unit)? = null
    private var activityRef: WeakReference<ComponentActivity>? = null
    private var initialized = false
    private var initAttempts = 0
    
    /**
     * Initialize Google Sign-In
     * @param webClientId Your web client ID from Google Developer Console
     */
    fun initialize(webClientId: String): Boolean {
        try {
            Log.d(TAG, "Attempting to initialize Google Sign-In (attempt ${++initAttempts})")
            
            if (webClientId.isEmpty()) {
                Log.e(TAG, "Web client ID is empty!")
                return false
            }
            
            if (webClientId.contains("YOUR_WEB_CLIENT_ID")) {
                Log.e(TAG, "Invalid web client ID: $webClientId - contains placeholder text")
                return false
            }
            
            // Create GoogleSignInOptions
            val gso = try {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating GoogleSignInOptions: ${e.message}", e)
                null
            }
            
            if (gso == null) {
                Log.e(TAG, "Failed to create GoogleSignInOptions")
                return false
            }
            
            // Create GoogleSignInClient - always use application context
            try {
                val appContext = context.applicationContext
                googleSignInClient = GoogleSignIn.getClient(appContext, gso)
                Log.d(TAG, "Google Sign-In client created successfully")
                
                // Force a disconnect and reconnect to ensure fresh state
                try {
                    googleSignInClient?.signOut()?.addOnCompleteListener {
                        Log.d(TAG, "Signed out before initialization to ensure fresh state")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Sign out during initialization failed: ${e.message}")
                    // Continue anyway
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating GoogleSignInClient: ${e.message}", e)
                googleSignInClient = null
                return false
            }
            
            // Check if the client was actually created
            if (googleSignInClient != null) {
                Log.d(TAG, "Google Sign-In initialized successfully")
                initialized = true
                
                // Check for existing sign-in
                try {
                    val account = GoogleSignIn.getLastSignedInAccount(context.applicationContext)
                    if (account != null) {
                        Log.d(TAG, "Found existing Google Sign-In: ${account.email}")
                    } else {
                        Log.d(TAG, "No existing Google Sign-In found")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking last signed in account: ${e.message}", e)
                }
                
                return true
            } else {
                Log.e(TAG, "Failed to create GoogleSignInClient")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Sign-In: ${e.message}", e)
            googleSignInClient = null
            return false
        }
    }
    
    /**
     * Force re-initialization with debug information
     */
    fun forceReInitialize(webClientId: String, debugTag: String? = null): Boolean {
        googleSignInClient = null
        initialized = false
        Log.d(TAG, "Force re-initializing Google Sign-In from $debugTag")
        return initialize(webClientId)
    }
    
    /**
     * Check if Google Sign-In is initialized
     */
    fun isInitialized(): Boolean {
        val isInit = googleSignInClient != null && initialized
        Log.d(TAG, "Google Sign-In initialization check: $isInit")
        return isInit
    }
    
    /**
     * Get sign-in intent to launch the Google Sign-In UI
     */
    fun getSignInIntent(): Intent? {
        if (googleSignInClient == null) {
            Log.e(TAG, "Cannot get sign-in intent: GoogleSignInClient is null")
            return null
        }
        
        return try {
            val intent = googleSignInClient!!.signInIntent
            Log.d(TAG, "Created Google sign-in intent successfully")
            intent
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create sign-in intent: ${e.message}", e)
            null
        }
    }
    
    /**
     * Pre-register Google Sign-In for an activity
     * Call this early in the activity lifecycle (like in onCreate)
     */
    fun preRegister(activity: ComponentActivity): Boolean {
        Log.d(TAG, "Pre-registering Google Sign-In")
        
        try {
            // Store activity reference 
            activityRef = WeakReference(activity)
            
            // Try to get web client ID from resources if not already initialized
            if (!isInitialized()) {
                try {
                    val webClientId = activity.getString(activity.resources.getIdentifier(
                        "default_web_client_id", "string", activity.packageName))
                    if (!webClientId.isNullOrEmpty()) {
                        Log.d(TAG, "Pre-registering: Attempting initialization with retrieved webClientId")
                        initialize(webClientId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Pre-registering: Failed to retrieve webClientId from resources: ${e.message}")
                    return false
                }
            }
            
            return isInitialized()
        } catch (e: Exception) {
            Log.e(TAG, "Exception during pre-registration: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Register activity for Google Sign-In result
     */
    fun registerForActivityResult(activity: ComponentActivity, callback: (String?) -> Unit): ActivityResultLauncher<Intent> {
        Log.d(TAG, "Registering for Google Sign-In activity result")
        
        // Store the callback and activity reference (weakly)
        signInResultCallback = callback
        activityRef = WeakReference(activity)
        
        try {
            // Double-check initialization
            if (googleSignInClient == null) {
                Log.e(TAG, "GoogleSignInClient is null when registering for activity result")
                
                // Try to get web client ID from resources if possible
                try {
                    val webClientId = activity.getString(activity.resources.getIdentifier(
                        "default_web_client_id", "string", activity.packageName))
                    if (!webClientId.isNullOrEmpty()) {
                        Log.d(TAG, "Attempting re-initialization with retrieved webClientId")
                        initialize(webClientId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to retrieve webClientId from resources: ${e.message}")
                }
            }
            
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                try {
                    handleSignInResult(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception in registerForActivityResult callback: ${e.message}", e)
                    signInResultCallback?.invoke(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception registering for activity result: ${e.message}", e)
            // Return a dummy launcher that will just call back with null
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { 
                signInResultCallback?.invoke(null)
            }
        }
    }
    
    /**
     * Handle the sign-in result
     */
    private fun handleSignInResult(result: ActivityResult) {
        try {
            if (result.resultCode == ComponentActivity.RESULT_CANCELED) {
                Log.d(TAG, "Google Sign-In was canceled by the user")
                signInResultCallback?.invoke(null)
                return
            }
            
            val data = result.data
            if (data == null) {
                Log.e(TAG, "Google Sign-In result had null data")
                signInResultCallback?.invoke(null)
                return
            }
            
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                
                if (idToken != null) {
                    Log.d(TAG, "Google Sign-In successful, token length: ${idToken.length}")
                    signInResultCallback?.invoke(idToken)
                } else {
                    Log.e(TAG, "Google Sign-In successful but returned null token")
                    signInResultCallback?.invoke(null)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed with status code: ${e.statusCode}", e)
                signInResultCallback?.invoke(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed with exception: ${e.message}", e)
            signInResultCallback?.invoke(null)
        }
    }
    
    /**
     * Reset launcher and references
     */
    fun resetLauncher() {
        activityRef = null
        signInResultCallback = null
    }
    
    /**
     * Silent sign-in to check if user is already authenticated
     */
    fun silentSignIn(callback: (GoogleSignInAccount?) -> Unit) {
        if (googleSignInClient == null) {
            Log.e(TAG, "Cannot perform silent sign-in: GoogleSignInClient is null")
            callback(null)
            return
        }
        
        googleSignInClient?.silentSignIn()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Silent sign-in successful")
                    callback(task.result)
                } else {
                    Log.d(TAG, "Silent sign-in failed: ${task.exception?.message}")
                    callback(null)
                }
            }
    }
    
    /**
     * Sign out from Google
     */
    fun signOut(callback: () -> Unit) {
        if (googleSignInClient == null) {
            Log.e(TAG, "Cannot sign out: GoogleSignInClient is null")
            callback()
            return
        }
        
        googleSignInClient?.signOut()
            ?.addOnCompleteListener {
                Log.d(TAG, "Sign-out completed")
                callback()
            }
    }
} 