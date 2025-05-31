package com.example.myapplication.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.R
import com.example.myapplication.ui.theme.poppins
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.screens.profile.ProfileViewModel
import com.example.myapplication.ui.screens.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onBackClick: () -> Unit = {},
    onProfileCompleted: () -> Unit = {}
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    // Populate fields when user data is loaded
    LaunchedEffect(user) {
        user?.let {
            // Safely split full name into first and last, default to empty if name is null
            val fullName = it.name.orEmpty()
            val parts = fullName.split(" ")
            firstName = parts.getOrNull(0) ?: ""
            lastName = parts.drop(1).joinToString(" ").trim()
            phoneNumber = it.phone ?: ""
        }
    }

    // Handle update result
    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                // Only navigate on update success (data contains a message), ignore initial load
                val message = (uiState as ProfileUiState.Success).data as? String
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    onProfileCompleted()
                }
            }
            is ProfileUiState.Error -> {
                Toast.makeText(context, (uiState as ProfileUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> { /* no-op for Loading, Idle */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .padding(start = 15.dp, top = 27.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopStart)
                .zIndex(1f) // Ensure back button stays on top
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

        // Main content in a scrollable column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp) // Add padding at bottom for the button
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Big star (background, behind the main one)
                    Image(
                        painter = painterResource(id = R.drawable.bigblackstar),
                        contentDescription = "Big Star Background",
                        modifier = Modifier
                            .size(width = 320.dp, height = 210.dp)
                            .align(Alignment.Center)
                            .offset(x = 60.dp, y = 40.dp)
                    )
                    // Big star (main sparkle)
                    Image(
                        painter = painterResource(id = R.drawable.bigblackstar),
                        contentDescription = "Big Star",
                        modifier = Modifier
                            .size(width = 320.dp, height = 210.dp)
                            .align(Alignment.Center)
                            .offset(x = (-20).dp)
                    )
                    // Small star, top left
                    Image(
                        painter = painterResource(id = R.drawable.smallblackstar),
                        contentDescription = "Small Star",
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 15.dp, y = 80.dp)
                    )
                    // Medium star, top right
                    Image(
                        painter = painterResource(id = R.drawable.medblackstar),
                        contentDescription = "Medium Star",
                        modifier = Modifier
                            .size(38.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-65).dp, y = 20.dp)
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Complete Your Profile",
                            fontSize = 31.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF030303),
                            letterSpacing = 0.10.sp
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You're 80% Done",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Just a Few More Details to Complete Your Profile!",
                                fontSize = 15.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                letterSpacing = 0.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Profile Image
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.account),
                            contentDescription = "Profile Image",
                            modifier = Modifier.size(180.dp)
                        )
                    }
                    
                    // Edit button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = 120.dp, y = 135.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF149459))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.vectorpen),
                            contentDescription = "Edit Profile",
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // First Name Field
                Text(
                    text = "First Name",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("Example: Ahmed",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.08.sp) },
                    
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Last Name Field
                Text(
                    text = "Last Name",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Example: Ahmad",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.08.sp) },
                    
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Number Field with prefix
                Text(
                    text = "Phone Number",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("Enter your Phone Number",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.08.sp) },

                    leadingIcon = {
                        Text(
                            text = "+213 |",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Birthday Field
                Text(
                    text = "Birthday",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    placeholder = { Text("DD/MM/YYYY",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.08.sp) },
                    
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFF149459),
                            modifier = Modifier.clickable { /* Open date picker if needed */ }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location Field
                Text(
                    text = "Location",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Enter your location",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        letterSpacing = 0.08.sp) },
                    
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Select Location",
                            tint = Color(0xFF149459)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color(0xFFD9D9D9),
                        containerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
                
                // Add spacer at the bottom for better scrolling
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Fixed position button at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Button(
                onClick = {
                    // Update viewModel before submitting
                    val fullName = "${firstName.trim()} ${lastName.trim()}"
                    viewModel.name.value = fullName
                    viewModel.phone.value = phoneNumber
                    viewModel.birthday.value = birthday
                    viewModel.location.value = location
                    viewModel.updateProfile()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.08.sp
                )
            }
            // Show loading indicator over button when updating
            if (uiState is ProfileUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd),
                    color = Color.White
                )
            }
        }
    }
}

// Preview removed due to ViewModel dependencies 