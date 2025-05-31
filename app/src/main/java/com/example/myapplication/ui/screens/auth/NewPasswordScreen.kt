package com.example.myapplication.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.auth.AuthViewModel
import com.example.myapplication.ui.screens.auth.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPasswordScreen(
    email: String,
    code: String,
    onBackClick: () -> Unit = {},
    onPasswordResetSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                if ((uiState as AuthUiState.Success).message == "password_reset_success") {
                    Toast.makeText(context, "Password reset successfully", Toast.LENGTH_LONG).show()
                    onPasswordResetSuccess()
                    viewModel.resetState()
                }
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Error state variables
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Validation functions
    fun validateNewPassword(): Boolean {
        return if (newPassword.isBlank()) {
            newPasswordError = "Password is required"
            false
        } else if (newPassword.length < 6) {
            newPasswordError = "Password must be at least 6 characters"
            false
        } else {
            newPasswordError = null
            true
        }
    }
    
    fun validateConfirmPassword(): Boolean {
        return if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirm password is required"
            false
        } else if (confirmPassword != newPassword) {
            confirmPasswordError = "Passwords do not match"
            false
        } else {
            confirmPasswordError = null
            true
        }
    }
    
    fun validateForm(): Boolean {
        val newPasswordValid = validateNewPassword()
        val confirmPasswordValid = validateConfirmPassword()
        return newPasswordValid && confirmPasswordValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .padding(start = 15.dp, top = 40.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopStart)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                // Big star (background, behind the main one) - move right and down
                Image(
                    painter = painterResource(id = com.example.myapplication.R.drawable.bigblackstar),
                    contentDescription = "Big Star Background",
                    modifier = Modifier
                        .size(width = 320.dp, height = 210.dp)
                        .align(Alignment.Center)
                        .offset(x = 60.dp, y = 40.dp)
                )
                // Big star (main sparkle), as background - move a little to the left
                Image(
                    painter = painterResource(id = com.example.myapplication.R.drawable.bigblackstar),
                    contentDescription = "Big Star",
                    modifier = Modifier
                        .size(width = 320.dp, height = 210.dp)
                        .align(Alignment.Center)
                        .offset(x = (-20).dp)
                )
                // Small star, top left, closer to big star - move a little to the left
                Image(
                    painter = painterResource(id = com.example.myapplication.R.drawable.smallblackstar),
                    contentDescription = "Small Star",
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 15.dp, y = 80.dp)
                )
                // Medium star, top right - move a little to the left
                Image(
                    painter = painterResource(id = com.example.myapplication.R.drawable.medblackstar),
                    contentDescription = "Medium Star",
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-65).dp, y = 20.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New Password",
                        fontSize = 30.sp,
                        fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF030303),
                        letterSpacing = 0.10.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your new password must be different\nfrom the last one ",
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        letterSpacing = 0.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }


            Text(
                text = "New Password",
                fontSize = 19.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                letterSpacing = 0.08.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    if (newPasswordError != null) validateNewPassword()
                    // If confirm password was already entered, validate it again when new password changes
                    if (confirmPassword.isNotEmpty()) validateConfirmPassword()
                },
                placeholder = { Text("123ZAbc!&",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 0.08.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black,
                    letterSpacing = 0.08.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (newPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF149459)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                isError = newPasswordError != null
            )
            
            if (newPasswordError != null) {
                Text(
                    text = newPasswordError ?: "",
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
                text = "Confirm Password",
                fontSize = 19.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                letterSpacing = 0.08.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it 
                    if (confirmPasswordError != null) validateConfirmPassword()
                },
                placeholder = { Text("123ZAbc!&",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    letterSpacing = 0.08.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.Black,
                    letterSpacing = 0.08.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF149459)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = confirmPasswordError != null
            )
            
            if (confirmPasswordError != null) {
                Text(
                    text = confirmPasswordError ?: "",
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

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    if (validateForm()) {
                        viewModel.resetPassword(email, code, newPassword)
                    }
                },
                // Only enable when not loading and no field errors
                enabled = uiState !is AuthUiState.Loading
                    && newPasswordError == null
                    && confirmPasswordError == null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF149459)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Reset Password",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.08.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPasswordScreenPreview() {
    NewPasswordScreen("email@example.com", "code123")
} 