package com.example.myapplication.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.poppins
import com.example.myapplication.ui.viewmodel.OTPViewModel
import com.example.myapplication.ui.viewmodel.OTPUiState
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPVerificationScreen(
    onBackClick: () -> Unit = {},
    onVerifySuccess: (String, String) -> Unit = { _, _ -> },
    fromForgotPassword: Boolean = false
) {
    val viewModel: OTPViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var otpError by remember { mutableStateOf<String?>(null) }

    var digit1 by remember { mutableStateOf("") }
    var digit2 by remember { mutableStateOf("") }
    var digit3 by remember { mutableStateOf("") }
    var digit4 by remember { mutableStateOf("") }
    
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }
    
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    // Retrieve email from saved preferences for display
    val email = viewModel.getUserEmail() ?: ""
    
    val message = if (fromForgotPassword) {
        "Please enter the code we just sent to reset\nyour password"
    } else {
        "Please enter the code we just sent to email\n$email"
    }
    
    // Automatically request OTP when entering the screen (for registration flow)
    LaunchedEffect(Unit) {
        // Pre-focus the first OTP digit field to open keyboard
        focusRequester1.requestFocus()
    }

    LaunchedEffect(state) {
        when (state) {
            is OTPUiState.VerificationSuccess -> {
                otpError = null
                val code = digit1 + digit2 + digit3 + digit4
                onVerifySuccess(email, code)
                viewModel.resetState()
            }
            is OTPUiState.ResendSuccess -> {
                otpError = null
                Toast.makeText(context, "OTP sent", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is OTPUiState.Error -> {
                otpError = (state as OTPUiState.Error).message
                Toast.makeText(context, (state as OTPUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> { /* no-op */ }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .padding(start = 15.dp, top = 27.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(150.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(37.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = "Verify Code",
                fontSize = 28.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                fontFamily = poppins,
                textAlign = TextAlign.Center,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // OTP Input Fields
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // First Digit
                OutlinedTextField(
                    value = digit1,
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            digit1 = value
                            if (value.isNotEmpty()) {
                                focusRequester2.requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(70.dp)
                        .focusRequester(focusRequester1),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // Second Digit
                OutlinedTextField(
                    value = digit2,
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            digit2 = value
                            if (value.isNotEmpty()) {
                                focusRequester3.requestFocus()
                            } else if (value.isEmpty()) {
                                focusRequester1.requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(70.dp)
                        .focusRequester(focusRequester2),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // Third Digit
                OutlinedTextField(
                    value = digit3,
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            digit3 = value
                            if (value.isNotEmpty()) {
                                focusRequester4.requestFocus()
                            } else if (value.isEmpty()) {
                                focusRequester2.requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(70.dp)
                        .focusRequester(focusRequester3),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // Fourth Digit
                OutlinedTextField(
                    value = digit4,
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            digit4 = value
                            if (value.isEmpty()) {
                                focusRequester3.requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(70.dp)
                        .focusRequester(focusRequester4),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
            
            // Display OTP error message if verification failed
            if (otpError != null) {
                Text(
                    text = otpError!!,
                    color = Color.Red,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Didn't receive OTP text and resend button
            Text(
                text = "Didn't receive OTP?",
                fontSize = 16.sp,
                fontFamily = poppins,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(0.6f)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Resend OTP",
                fontSize = 16.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF149459),
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(enabled = state !is OTPUiState.Loading) {
                        if (fromForgotPassword) {
                            viewModel.resendPasswordReset()
                        } else {
                            viewModel.resendOtp()
                        }
                    }
                    .alpha(if (state is OTPUiState.Loading) 0.5f else 1f)
                    .align(Alignment.CenterHorizontally)
            )

            // Verify Button
            Button(
                onClick = {
                    val code = digit1 + digit2 + digit3 + digit4
                    if (code.length == 4) {
                        if (fromForgotPassword) {
                            // Bypass API and navigate to NewPassword
                            onVerifySuccess(email, code)
                            viewModel.resetState()
                        } else {
                            // Standard email verification flow
                            viewModel.verifyEmail(code)
                        }
                    }
                },
                // Only enable when code is complete and not loading
                enabled = digit1.isNotEmpty() && digit2.isNotEmpty() && digit3.isNotEmpty() && digit4.isNotEmpty() && state !is OTPUiState.Loading,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                )
            ) {
                Text(
                    text = "Verify",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppins
                )
            }

            if (state is OTPUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    color = Color(0xFF149459)
                )
            }
        }
    }
} 