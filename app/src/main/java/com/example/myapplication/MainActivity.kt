package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.auth.GoogleAuthManager
import com.example.myapplication.navigation.NavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var googleAuthManager: GoogleAuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure initialization happens right away in a blocking way
        forceInitializeGoogleSignIn()
        
        setContent {
            // Set up the app theme
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create a NavController that will be used for navigation
                    val navController = rememberNavController()
                    
                    // Set up the navigation graph
                    // This will handle all navigation between screens
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    private fun forceInitializeGoogleSignIn() {
        try {
            // Get the web client ID directly from resource identifier
            val resourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
            if (resourceId == 0) {
                Log.e(TAG, "Could not find default_web_client_id resource")
                return
            }
            
            val webClientId = getString(resourceId)
            Log.d(TAG, "Retrieved web client ID from resources: $webClientId")
            
            if (webClientId.isNotEmpty()) {
                Log.d(TAG, "Force initializing Google Sign-In from MainActivity")
                
                // First try the manager way
                val success = googleAuthManager.forceReInitialize(webClientId, "MainActivity_onCreate")
                
                // Double check with direct initialization
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                
                // Create the client directly in the activity for immediate initialization
                val googleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
                
                // Sanity check for initialization
                if (googleAuthManager.isInitialized()) {
                    Log.d(TAG, "Google Sign-In initialization confirmed successful")
                    
                    // Save client ID for future use
                    getSharedPreferences("google_auth", Context.MODE_PRIVATE).edit()
                        .putString("web_client_id", webClientId)
                        .apply()
                } else {
                    Log.e(TAG, "Google Sign-In still not initialized after force initialization")
                }
            } else {
                Log.e(TAG, "Web client ID is empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during force initialization of Google Sign-In: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Always re-initialize in onResume to ensure it's available
        if (!googleAuthManager.isInitialized()) {
            Log.d(TAG, "Google Sign-In not initialized in onResume, re-initializing")
            forceInitializeGoogleSignIn()
        } else {
            Log.d(TAG, "Google Sign-In already initialized in onResume")
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // This helps catch any Google Sign-In results that might not be handled by the ActivityResultLauncher
        Log.d(TAG, "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode")
    }
}


