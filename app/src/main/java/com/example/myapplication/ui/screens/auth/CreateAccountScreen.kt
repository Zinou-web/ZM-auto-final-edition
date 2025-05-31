package com.example.myapplication.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.myapplication.ui.theme.poppins
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.myapplication.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.text.ClickableText
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onSignInClick: () -> Unit = {},
    onCreateAccountSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Get local context for Toast
    val context = LocalContext.current
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(true) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var registrationError by remember { mutableStateOf<String?>(null) }

    // Error state variables
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        val state = uiState
        Log.d("CreateAccountScreen", "UI State changed: $state")
        // Clear server error on new state
        registrationError = null
        when (state) {
            is AuthUiState.Success -> {
                val message = state.message
                if (message == "registration_success") {
                    Log.d("CreateAccountScreen", "Registration success, navigating to OTP")
                onCreateAccountSuccess()
                }
            }
            is AuthUiState.Error -> {
                val errorMsg = state.message
                Log.e("CreateAccountScreen", "Error: $errorMsg")
                registrationError = errorMsg
            }
            is AuthUiState.Loading -> {
                Log.d("CreateAccountScreen", "Loading state...")
            }
            else -> {
                Log.d("CreateAccountScreen", "Idle or other state")
            }
        }
    }
    
    // Terms and Conditions Dialog
    if (showTermsDialog) {
        TermsAndConditionsDialog(
            onDismiss = { showTermsDialog = false }
        )
    }
    
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
    
    fun validateConfirmPassword(): Boolean {
        return if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirm password is required"
            false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            false
        } else {
            confirmPasswordError = null
            true
        }
    }
    
    fun validateTerms(): Boolean {
        return if (!termsAccepted) {
            termsError = "You must accept the terms and conditions"
            false
        } else {
            termsError = null
            true
        }
    }
    
    fun validateForm(): Boolean {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        val confirmPasswordValid = validateConfirmPassword()
        val termsValid = validateTerms()
        return emailValid && passwordValid && confirmPasswordValid && termsValid
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CreateAccountTopDecoration()
            
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
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
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
                    .background(Color.White, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    containerColor = Color.White,
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
            // Password
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
                    if (confirmPassword.isNotEmpty()) validateConfirmPassword()
                },
                placeholder = { Text("123ZAbc!&",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
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
                    .background(Color.White, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    containerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color(0xFF149459))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
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
            
            Spacer(modifier = Modifier.height(16.dp))
            // Confirm Password
            Text(
                text = "Confirm Password",
                fontSize = 18.sp,
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
                placeholder = { Text("Confirm your password",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
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
                    .background(Color.White, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFD9D9D9),
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    containerColor = Color.White,
                    errorBorderColor = Color.Red
                ),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color(0xFF149459))
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
            
            Spacer(modifier = Modifier.height(9.dp))
            // Terms and Conditions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { 
                        termsAccepted = it 
                        if (termsError != null) validateTerms()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF149459),
                        uncheckedColor = Color(0xFFD9D9D9),
                    )
                )
                ClickableText(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Black.copy(alpha = 0.7f),
                                fontFamily = FontFamily(Font(R.font.inter_regular)),
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                letterSpacing = 0.08.sp
                            )
                        ) {
                            append("By creating an account or signing you \nagree to our ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF149459),
                                fontFamily = FontFamily(Font(R.font.inter_semibold)),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.08.sp,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Terms and Conditions")
                        }
                    },
                    style = androidx.compose.ui.text.TextStyle.Default,
                    onClick = { 
                        showTermsDialog = true
                    }
                )
            }
            
            if (termsError != null) {
                Text(
                    text = termsError ?: "",
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
            
            Spacer(modifier = Modifier.height(15.dp))
            // Display server-side registration error
            if (registrationError != null) {
                Text(
                    text = registrationError ?: "",
                    color = Color.Red,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                )
            }
            // Create Account Button
            Button(
                onClick = {
                    if (validateForm()) {
                        // Only attempt registration with email & password; name and phone collected later
                        viewModel.register(
                            firstName = "",
                            lastName = "",
                            email = email,
                            password = password
                        )
                    }
                },
                // Only enable when not loading and no field errors
                enabled = uiState !is AuthUiState.Loading
                    && emailError == null
                    && passwordError == null
                    && confirmPasswordError == null
                    && termsError == null,
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
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.08.sp
                    )
                }
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
                SocialButton(iconRes = "facebook")
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(iconRes = "google")
            }
            Spacer(modifier = Modifier.height(25.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color.Black,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    letterSpacing = 0.08.sp
                )
                Text(
                    text = "Sign In",
                    color = Color(0xFF149459),
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    letterSpacing = 0.08.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onSignInClick)
                )
            }
        }
    }
}

@Composable
fun CreateAccountTopDecoration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Big star (background, behind the main one) - move right and down
        Image(
            painter = painterResource(id = com.example.myapplication.R.drawable.newbigstar),
            contentDescription = "Big Star Background",
            modifier = Modifier
                .size(width = 320.dp, height = 210.dp)
                .align(Alignment.Center)
                .offset(x = 60.dp, y = 40.dp)
        )
        // Big star (main sparkle), as background - move a little to the left
        Image(
            painter = painterResource(id = com.example.myapplication.R.drawable.bigstar),
            contentDescription = "Big Star",
            modifier = Modifier
                .size(width = 320.dp, height = 210.dp)
                .align(Alignment.Center)
                .offset(x = (-20).dp)
        )
        // Small star, top left, closer to big star - move a little to the left
        Image(
            painter = painterResource(id = com.example.myapplication.R.drawable.smallstar),
            contentDescription = "Small Star",
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.TopStart)
                .offset(x = 15.dp, y = 80.dp)
        )
        // Medium star, top right - move a little to the left
        Image(
            painter = painterResource(id = com.example.myapplication.R.drawable.meduimstat),
            contentDescription = "Medium Star",
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-65).dp, y = 20.dp)
        )
        // Texts inside the big star
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 31.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                fontSize = 30.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF030303),
                letterSpacing = 0.10.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fill  with your information below\n to start\nthe journey ",
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

@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Terms and Conditions",
                    fontSize = 22.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF149459),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = termsAndConditionsText,
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF149459)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Close",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Sample terms and conditions text
private val termsAndConditionsText = """
1. Acceptance of Terms

By accessing or using the ZM Auto car rental application, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use the application.

2. User Registration

2.1. To use certain features of the application, you must register for an account.
2.2. You agree to provide accurate and complete information during registration.
2.3. You are responsible for maintaining the confidentiality of your account credentials.
2.4. You are responsible for all activities that occur under your account.

3. Rental Terms

3.1. All rental bookings are subject to vehicle availability.
3.2. A valid driver's license and other identification documents may be required to complete a rental.
3.3. Rental fees are as displayed in the application at the time of booking.
3.4. Additional charges may apply for late returns, fuel refilling, damage to the vehicle, etc.

4. Payment Terms

4.1. Payment methods accepted are as indicated in the application.
4.2. Rental fees and any security deposits must be paid prior to the commencement of the rental period.
4.3. The security deposit will be refunded after the vehicle has been returned and inspected, subject to any deductions for damages or additional charges.

5. Cancellation Policy

5.1. Cancellations made more than 48 hours before the rental period may receive a full refund.
5.2. Cancellations made within 48 hours of the rental period may be subject to a cancellation fee.
5.3. No-shows may result in forfeiture of the full rental fee.

6. Vehicle Use

6.1. The vehicle must only be driven by the registered renter or additional drivers approved by ZM Auto.
6.2. The vehicle must be used in accordance with all applicable laws and regulations.
6.3. The vehicle must not be used for any illegal activities, racing, or off-road driving unless specifically permitted.
6.4. Smoking is not permitted in the vehicles.

7. Liability

7.1. The renter is liable for any damage to the vehicle during the rental period, subject to the terms of any insurance coverage.
7.2. ZM Auto is not liable for any personal property left in the vehicle.
7.3. ZM Auto is not liable for any indirect or consequential damages arising from the use of the vehicle.

8. Privacy Policy

8.1. ZM Auto collects personal information as described in our Privacy Policy.
8.2. By using the application, you consent to the collection and use of your information as described in the Privacy Policy.

9. Modifications to Terms

9.1. ZM Auto reserves the right to modify these Terms and Conditions at any time.
9.2. Users will be notified of any material changes to these terms.

10. Governing Law

These Terms and Conditions shall be governed by and construed in accordance with the laws of [Country], without regard to its conflict of law provisions.

11. Contact Information

If you have any questions about these Terms and Conditions, please contact us at support@zmauto.com.

Last Updated: January 1, 2024
""".trimIndent()

@Composable
fun SocialButton(
    iconRes: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = when (iconRes) {
                    "facebook" -> R.drawable.facebook
                    "google" -> R.drawable.google
                    else -> R.drawable.facebook
                }
            ),
            contentDescription = "Social Login Button",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateAccountScreenPreview() {
    CreateAccountScreen()
} 