package com.example.myapplication.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.viewmodel.LoginUiState
import com.example.myapplication.ui.viewmodel.LoginViewModel
import com.example.myapplication.ui.viewmodel.SocialLoginUiState
import com.example.myapplication.ui.viewmodel.SocialLoginViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity

private const val TAG = "LoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    socialViewModel: SocialLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        socialViewModel.handleGoogleSignInResult(result.data)
    }
    
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val socialLoginState by socialViewModel.loginState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Get the web client ID from strings.xml
    val webClientId = remember { context.getString(R.string.default_web_client_id) }
    
    // Initialize Google Sign-In
    var isGoogleInitialized by remember { mutableStateOf(false) }
    
    // Log what's happening with the webClientId
    LaunchedEffect(Unit) {
        Log.d(TAG, "Web client ID: ${webClientId.take(10)}...${webClientId.takeLast(5)}")
        Log.d(TAG, "Web client ID length: ${webClientId.length}")
    }
    
    // Initialize Google Sign-In once
    LaunchedEffect(Unit) {
        try {
            isGoogleInitialized = socialViewModel.initializeGoogleSignIn(webClientId)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Google Sign-In: ${e.message}")
        }
    }
    
    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginUiState.Success -> {
                isLoading = false
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginUiState.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (loginState as LoginUiState.Error).message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is LoginUiState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }
    
    // Handle social login state changes
    LaunchedEffect(socialLoginState) {
        when (socialLoginState) {
            is SocialLoginUiState.Success -> {
                isLoading = false
                onLoginSuccess()
                socialViewModel.resetState()
            }
            is SocialLoginUiState.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (socialLoginState as SocialLoginUiState.Error).message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is SocialLoginUiState.Loading -> {
                isLoading = true
            }
            else -> {
                // No action needed
            }
        }
    }
    
    // Check if Google is initialized periodically
    LaunchedEffect(Unit) {
        isGoogleInitialized = socialViewModel.isGoogleSignInInitialized()
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Login") }
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
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }
            
            // Social login buttons
            SocialLoginButtons(
                onGoogleClick = {
                    Log.d(TAG, "Google login button clicked, initialized: $isGoogleInitialized")
                    if (isGoogleInitialized) {
                        val intent = socialViewModel.getSignInIntent()
                        if (intent != null) {
                            googleLauncher.launch(intent)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to create sign-in intent")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Google Sign-In not initialized. Please restart the app.")
                        }
                    }
                },
                onFacebookClick = { /* Handle Facebook login */ },
                isGoogleInitialized = isGoogleInitialized
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
} 