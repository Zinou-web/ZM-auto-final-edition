package com.example.myapplication.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteYourBookingScreen(
    onBackPressed: () -> Unit = {},
    onContinue: () -> Unit = {},
    bookingViewModel: BookingViewModel = viewModel()
) {
    // Context for file operations
    val context = LocalContext.current
    
    // Form state variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedWilaya by remember { mutableStateOf("") }
    var hasDriverLicense by remember { mutableStateOf(false) }
    var driverLicenseUri by remember { mutableStateOf<Uri?>(null) }
    var driverLicenseFileName by remember { mutableStateOf("") }
    
    // Form validation state
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var wilayaError by remember { mutableStateOf<String?>(null) }
    var driverLicenseError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // PDF document launcher for driver license
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            driverLicenseUri = it
            
            // Get file name from URI
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val displayNameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        driverLicenseFileName = c.getString(displayNameIndex)
                    } else {
                        driverLicenseFileName = "license_${System.currentTimeMillis()}.pdf"
                    }
                }
            }
            
            hasDriverLicense = true
            driverLicenseError = null
        }
    }
    
    // Available wilayas
    val wilayas = listOf("Algiers", "Oran", "Blida", "Setif", "Constantine", "Annaba", "Tlemcen", "Batna")
    
    // Validation functions
    fun validateFirstName(): Boolean {
        return if (firstName.isBlank()) {
            firstNameError = "First name is required"
            false
        } else if (firstName.length < 2) {
            firstNameError = "Name is too short"
            false
        } else {
            firstNameError = null
            true
        }
    }
    
    fun validateLastName(): Boolean {
        return if (lastName.isBlank()) {
            lastNameError = "Last name is required"
            false
        } else if (lastName.length < 2) {
            lastNameError = "Name is too short"
            false
        } else {
            lastNameError = null
            true
        }
    }
    
    fun validatePhoneNumber(): Boolean {
        return if (phoneNumber.isBlank()) {
            phoneNumberError = "Phone number is required"
            false
        } else if (!phoneNumber.matches(Regex("^[0-9]{10,13}$"))) {
            phoneNumberError = "Enter a valid phone number"
            false
        } else {
            phoneNumberError = null
            true
        }
    }
    
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
    
    fun validateWilaya(): Boolean {
        return if (selectedWilaya.isBlank()) {
            wilayaError = "Please select a wilaya"
            false
        } else {
            wilayaError = null
            true
        }
    }
    
    fun validateDriverLicense(): Boolean {
        return if (!hasDriverLicense || driverLicenseUri == null) {
            driverLicenseError = "Driver license is required"
            false
        } else {
            driverLicenseError = null
            true
        }
    }
    
    fun validateForm(): Boolean {
        val firstNameValid = validateFirstName()
        val lastNameValid = validateLastName()
        val phoneValid = validatePhoneNumber()
        val emailValid = validateEmail()
        val wilayaValid = validateWilaya()
        val licenseValid = validateDriverLicense()
        
        return firstNameValid && lastNameValid && phoneValid && emailValid && wilayaValid && licenseValid
    }
    
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        // Back Button at top left
        Box(
            modifier = Modifier
                .padding(start = 15.dp, top = topPadding + 15.dp)
                .size(45.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopStart)
                .zIndex(1f) // Ensure back button stays on top
                .clickable { onBackPressed() }
        ) {
            IconButton(
                onClick = { onBackPressed() },
                modifier = Modifier.size(45.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Title at the top center
        Text(
            text = "Complete Your Booking",
            fontSize = 23.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPadding + 25.dp)
        )
        
        // Main content in a scrollable column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding + 70.dp)
                .padding(bottom = 96.dp) // Add padding at bottom for the button
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Form Section Title
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Renter Information",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // First Name Field
                Text(
                    text = "First Name",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { 
                        firstName = it
                        if (firstNameError != null) validateFirstName()
                    },
                    placeholder = { 
                        Text(
                            text = "Example: Ahmed",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ) 
                    },
                    isError = firstNameError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF149459),
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                if (firstNameError != null) {
                    Text(
                        text = firstNameError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Last Name Field
                Text(
                    text = "Last Name",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        if (lastNameError != null) validateLastName()
                    },
                    placeholder = { 
                        Text(
                            text = "Example: Ahmad",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ) 
                    },
                    isError = lastNameError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF149459),
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                if (lastNameError != null) {
                    Text(
                        text = lastNameError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone Number Field with prefix
                Text(
                    text = "Phone Number",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        if (phoneNumberError != null) validatePhoneNumber()
                    },
                    placeholder = { 
                        Text(
                            text = "Enter your phone number",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ) 
                    },
                    leadingIcon = {
                        Text(
                            text = "+213 |",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        )
                    },
                    isError = phoneNumberError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF149459),
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                
                if (phoneNumberError != null) {
                    Text(
                        text = phoneNumberError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                Text(
                    text = "Email",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        if (emailError != null) validateEmail()
                    },
                    placeholder = { 
                        Text(
                            text = "Example@gmail.com",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        ) 
                    },
                    isError = emailError != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF149459),
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                
                if (emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Wilaya Dropdown
                Text(
                    text = "Wilaya",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedWilaya,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { 
                            Text(
                                text = "Select your wilaya",
                                fontFamily = poppins,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color.Gray
                            ) 
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF149459),
                            unfocusedBorderColor = Color.LightGray,
                            errorBorderColor = Color.Red,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        isError = wilayaError != null
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        wilayas.forEach { wilaya ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = wilaya,
                                        fontFamily = poppins,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    selectedWilaya = wilaya
                                    expanded = false
                                    wilayaError = null
                                }
                            )
                        }
                    }
                }
                
                if (wilayaError != null) {
                    Text(
                        text = wilayaError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Driver License Upload
                Text(
                    text = "Driver License",
                    fontSize = 16.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (driverLicenseError != null) Color(0xFFFFEDED) else Color.White
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = if (driverLicenseError != null) {
                            androidx.compose.ui.graphics.SolidColor(Color.Red)
                        } else {
                            androidx.compose.ui.graphics.SolidColor(Color.LightGray)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Launch PDF picker
                                pdfLauncher.launch("application/pdf")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (hasDriverLicense && driverLicenseFileName.isNotEmpty()) 
                                      driverLicenseFileName 
                                      else "Select your driver license PDF",
                                fontFamily = poppins,
                                fontSize = 14.sp
                            )
                            if (hasDriverLicense && driverLicenseFileName.isNotEmpty()) {
                                Text(
                                    text = "Uploaded successfully",
                                    fontFamily = poppins,
                                    fontSize = 12.sp,
                                    color = Color(0xFF149459)
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (hasDriverLicense) Color(0xFF149459) else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasDriverLicense) Icons.Default.Close else Icons.Default.Upload,
                                contentDescription = if (hasDriverLicense) "Remove" else "Upload",
                                tint = if (hasDriverLicense) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                if (driverLicenseError != null) {
                    Text(
                        text = driverLicenseError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, top = 4.dp)
                    )
                }
                
                // Add spacer at the bottom for better scrolling
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Fixed position button at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    if (validateForm()) {
                        isSubmitting = true
                        
                        // Update booking view model with renter information
                        bookingViewModel.updateRenterInfo(
                            first = firstName,
                            last = lastName,
                            phone = phoneNumber,
                            emailAddress = email,
                            selectedWilaya = selectedWilaya,
                            licenseUri = driverLicenseUri,
                            licenseFileName = driverLicenseFileName
                        )
                        
                        onContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459),
                    disabledContainerColor = Color(0xFFABD6C2)
                ),
                enabled = !isSubmitting
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!isSubmitting) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    } else {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompleteYourBookingScreenPreview() {
    CompleteYourBookingScreen(
        onBackPressed = {},
        onContinue = {}
    )
} 