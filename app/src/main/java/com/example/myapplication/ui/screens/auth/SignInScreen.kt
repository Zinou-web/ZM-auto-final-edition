package com.example.myapplication.ui.screens.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.myapplication.ui.viewmodel.SocialLoginViewModel
import com.example.myapplication.ui.viewmodel.SocialLoginUiState
import androidx.activity.ComponentActivity
import com.example.myapplication.data.preference.AuthPreferenceManager

private const val TAG = "SignInScreen"

@Composable
fun SignInScreen(
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onSignInSuccess: () -> Unit = {
        android.util.Log.d(TAG, "onSignInSuccess callback triggered, but default implementation does nothing")
    },
    onNavigateToGoogleTest: () -> Unit = {},
    socialViewModel: SocialLoginViewModel = hiltViewModel()
) {
    // Track server-side login error
    var loginError by remember { mutableStateOf<String?>(null) }
    android.util.Log.d(TAG, "SignInScreen composable is being entered")
    
    val context = LocalContext.current
    // Use encrypted preferences to pre-fill email if available
    val authPrefManager = remember { AuthPreferenceManager(context) }
    val activity = context as? ComponentActivity
    
    if (activity == null) {
        android.util.Log.e(TAG, "Activity is null - cannot initialize GoogleAuthManager")
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Error initializing authentication. Please restart the app.", Toast.LENGTH_LONG).show()
        }
    } else {
        android.util.Log.d(TAG, "Activity is available: ${activity.javaClass.simpleName}")
    }
    
    val socialLoginState by socialViewModel.loginState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf(authPrefManager.getUserEmail() ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Error state variables
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    
    // Validation functions
    fun validateEmail(): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return if (email.isBlank()) {
            emailError = "Email is required"
            false
        } else if (!email.matches(emailRegex)) {
            emailError = "Enter a valid email address"
            false
        } else {
            emailError = null
            true
        }
    }
    
    fun validatePassword(): Boolean {
        return if (password.isBlank()) {
            passwordError = "Password is required"
            false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            false
        } else {
            passwordError = null
            true
        }
    }
    
    fun validateForm(): Boolean {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        return emailValid && passwordValid
    }
    
    // Launcher for Google Sign-In
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        socialViewModel.handleGoogleSignInResult(result.data)
    }

    // Initialize Google Sign-In state
    var isGoogleInitialized by remember { mutableStateOf(false) }
    var initializationAttempted by remember { mutableStateOf(false) }

    // Check if Google is initialized on composition
    LaunchedEffect(Unit) {
        isGoogleInitialized = socialViewModel.isGoogleSignInInitialized()
        if (!isGoogleInitialized && activity != null) {
            try {
                android.util.Log.d(TAG, "Attempting to initialize Google Sign-In from SignInScreen")
                val webClientId = context.getString(R.string.default_web_client_id)
                if (webClientId.isNotEmpty()) {
                    // Try to initialize with the activity
                    isGoogleInitialized = socialViewModel.initializeGoogleSignIn(webClientId)
                    if (isGoogleInitialized) {
                        googleSignInError = null
                    } else {
                        googleSignInError = "Failed to initialize Google Sign-In. Tap the Google button to retry."
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error initializing Google Sign-In: ${e.message}")
                googleSignInError = "Failed to initialize Google Sign-In: ${e.message?.take(50)}"
            }
        }
        initializationAttempted = true
    }
    
    // Handle social login state changes
    LaunchedEffect(socialLoginState) {
        when (socialLoginState) {
            is SocialLoginUiState.Success -> {
                isLoading = false
                // Clear any previous login error
                loginError = null
                onSignInSuccess()
                socialViewModel.resetState()
            }
            is SocialLoginUiState.Error -> {
                isLoading = false
                val errorMessage = (socialLoginState as SocialLoginUiState.Error).message
                // Show toast for feedback
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                // Capture inline login error
                loginError = errorMessage
                // Retain Google-specific error handling
                if (errorMessage.contains("Google", ignoreCase = true) || errorMessage.contains("sign-in", ignoreCase = true)) {
                    googleSignInError = errorMessage
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

    // Try direct initialization on screen visibility
    LaunchedEffect(activity) {
        activity?.let { act ->
            try {
                android.util.Log.d(TAG, "Attempting direct initialization on screen visibility")
                isGoogleInitialized = socialViewModel.ensureDirectInitialization(act)
                if (isGoogleInitialized) {
                    android.util.Log.d(TAG, "Direct initialization successful")
                    googleSignInError = null
                } else {
                    android.util.Log.d(TAG, "Direct initialization failed, will try again on button click")
                    googleSignInError = "Google Sign-In not initialized. Click Google button to retry."
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error in direct initialization: ${e.message}")
                googleSignInError = "Error initializing Google Sign-In: ${e.message?.take(50)}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
            .padding(start = 16.dp)
            .padding(end = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopDecoration()
            // Email
            Text(
                text = "Email",
                fontSize = 18.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                letterSpacing = 0.08.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it 
                    if (emailError != null) validateEmail()
                },
                placeholder = { Text("Example@gmail.com",
                    fontFamily = FontFamily(Font(R.font.poly_regular, FontWeight.Normal)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 0.08.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily(Font(R.font.poly_regular, FontWeight.Normal)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black,
                    letterSpacing = 0.08.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = emailError != null
            )
            
            if (emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = Color.Red,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password",
                fontSize = 18.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                letterSpacing = 0.08.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    if (passwordError != null) validatePassword()
                },
                placeholder = { Text("123ZAbc!&",
                    fontFamily = FontFamily(Font(R.font.poly_regular, FontWeight.Normal)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 0.08.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily(Font(R.font.poly_regular, FontWeight.Normal)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black,
                    letterSpacing = 0.08.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image,
                            contentDescription = null,
                            tint = Color(0xFF149459))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = passwordError != null
            )
            
            if (passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = Color.Red,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 4.dp, top = 4.dp)
                )
            }
            
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Remember Me checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(top = 6.dp)

                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF149459),
                            uncheckedColor = Color(0xFF149459)
                        )
                    )
                    Text(
                        text = "Remember Me",
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.inter_regular)),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        letterSpacing = 0.08.sp,
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 8.dp)
                        .clickable(onClick = onNavigateToForgotPassword)
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF149459),
                        fontSize = 17.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.08.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Show Google Sign-In error if any
            if (googleSignInError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFECEC), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = googleSignInError ?: "",
                        color = Color.Red,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Sign In Button
            Button(
                onClick = {
                    socialViewModel.loginWithEmail(email, password)
                },
                // Only enable when not loading and no field errors
                enabled = !isLoading && emailError == null && passwordError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.08.sp
                    )
                }
            }
            
            // Display login error inline
            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = Color.Red,
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = Color(0xFFD9D9D9),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "  or sign up with  ",
                    color = Color(0xFF149459),
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.08.sp
                )
                Divider(
                    color = Color(0xFFD9D9D9),
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD9D9D9), CircleShape)
                        .clickable { 
                            Toast.makeText(context, "Facebook sign-in coming soon", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Facebook",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD9D9D9), CircleShape)
                        .clickable {
                            googleSignInError = null
                            if (isGoogleInitialized) {
                                val intent = socialViewModel.getSignInIntent()
                                if (intent != null) {
                                    googleLauncher.launch(intent)
                                } else {
                                    googleSignInError = "Failed to create sign-in intent"
                                }
                            } else {
                                googleSignInError = "Google Sign-In not initialized. Please restart the app."
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(25.dp))
            
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = Color.Black,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    letterSpacing = 0.08.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                ) {
                    Text(
                        text = "Create Account",
                        color = Color(0xFF149459),
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        letterSpacing = 0.08.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
fun TopDecoration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Big star (main sparkle), as background
        Image(
            painter = painterResource(id = R.drawable.bigstar),
            contentDescription = "Big Star",
            modifier = Modifier
                .size(width = 320.dp, height = 210.dp)
                .align(Alignment.Center)
        )
        // Small star, top left, closer to big star
        Image(
            painter = painterResource(id = R.drawable.smallstar),
            contentDescription = "Small Star",
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.TopStart)
                .offset(x = 35.dp, y = 80.dp)
        )
        // Medium star, top right
        Image(
            painter = painterResource(id = R.drawable.meduimstat),
            contentDescription = "Medium Star",
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-45).dp, y = 20.dp)
        )
        // Texts inside the big star
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign In",
                fontSize = 30.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF030303),
                letterSpacing = 0.10.sp
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "Good to See You Again!\nLet's Find Your Perfect Car.",
                fontSize = 14.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    MyApplicationTheme {
        SignInScreen()
    }
} 