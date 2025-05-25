package com.example.myapplication.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

private const val TAG = "GoogleSignInTest"

/**
 * A simple screen to test Google Sign-In configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInTest() {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    
    var statusText by remember { mutableStateOf("Ready to test") }
    var isTestRunning by remember { mutableStateOf(false) }
    var webClientId by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        // Get the web client ID
        webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
        
        statusText = "Web Client ID: ${webClientId.take(10)}...${webClientId.takeLast(5)}\nLength: ${webClientId.length}"
    }
    
    fun runGoogleSignInTest() {
        isTestRunning = true
        statusText = "Testing Google Sign-In..."
        
        try {
            // Check if we can create GoogleSignInOptions
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            
            statusText += "\n✅ Successfully created GoogleSignInOptions"
            
            // Try to create GoogleSignInClient
            val googleSignInClient: GoogleSignInClient? = 
                activity?.let { GoogleSignIn.getClient(it, gso) }
            
            if (googleSignInClient != null) {
                statusText += "\n✅ Successfully created GoogleSignInClient"
                
                // Check for existing sign-in
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    statusText += "\n✅ Found existing Google account: ${account.email}"
                } else {
                    statusText += "\n❓ No existing Google account found"
                }
                
                // Check if we can create a sign-in intent
                try {
                    val signInIntent = googleSignInClient.signInIntent
                    statusText += "\n✅ Successfully created sign-in intent"
                } catch (e: Exception) {
                    statusText += "\n❌ Failed to create sign-in intent: ${e.message}"
                }
            } else {
                statusText += "\n❌ Failed to create GoogleSignInClient"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In test failed", e)
            statusText += "\n❌ Exception: ${e.javaClass.simpleName} - ${e.message}"
        } finally {
            isTestRunning = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Sign-In Test") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { runGoogleSignInTest() },
                enabled = !isTestRunning
            ) {
                if (isTestRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Test Google Sign-In")
                }
            }
        }
    }
} 