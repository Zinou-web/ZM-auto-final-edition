package com.example.myapplication.ui.screens.payment

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.PaymentMethodType
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdahabiaScreen(
    reservationId: Long,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    // Local state for UI
    var cardNumber by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf("") }
    var saveCardDetails by remember { mutableStateOf(false) }
    
    // Error state
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var cardHolderNameError by remember { mutableStateOf<String?>(null) }
    var cvvError by remember { mutableStateOf<String?>(null) }
    var expireDateError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    
    // UI state for loading indicator
    var isLoading by remember { mutableStateOf(false) }
    
    // Context for showing toasts
    val context = LocalContext.current
    
    // Collect payment UI state
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    
    // Handle UI state changes
    LaunchedEffect(paymentUiState) {
        when (paymentUiState) {
            is PaymentUiState.Loading -> {
                isLoading = true
                generalError = null
            }
            is PaymentUiState.Success -> {
                isLoading = false
                generalError = null
                // Navigate to next screen on success
                onContinueClick()
            }
            is PaymentUiState.Error -> {
                isLoading = false
                generalError = (paymentUiState as PaymentUiState.Error).message
            }
            is PaymentUiState.ValidationError -> {
                isLoading = false
                val errors = (paymentUiState as PaymentUiState.ValidationError).errors
                
                // Map validation errors to UI fields
                errors["cardNumber"]?.let { cardNumberError = it }
                errors["cardHolderName"]?.let { cardHolderNameError = it }
                errors["cvv"]?.let { cvvError = it }
                errors["expiryDate"]?.let { expireDateError = it }
                
                // Show general error if needed
                if (errors.isNotEmpty()) {
                    generalError = "Please check your payment details and try again."
                }
            }
            is PaymentUiState.Initial -> {
                isLoading = false
                generalError = null
            }
        }
    }
    
    // Function to handle payment processing
    val processPayment = {
        try {
            // Update ViewModel with form values
            paymentViewModel.updateCardNumber(cardNumber.replace(" ", ""))
            paymentViewModel.updateCardHolderName(cardHolderName)
            paymentViewModel.updateCvv(cvv)
            paymentViewModel.updateExpiryDate(expireDate)
            paymentViewModel.updateSaveCardDetails(saveCardDetails)
            
            Log.d("EdahabiaScreen", "Processing payment for reservationId: $reservationId, amount: ${bookingViewModel.totalPrice}")

            // Call processPayment with EDAHABIA type and the passed reservationId
            paymentViewModel.processPayment(
                reservationId = reservationId,
                paymentMethod = PaymentMethodType.EDAHABIA.name,
                amount = bookingViewModel.totalPrice
            )
        } catch (e: Exception) {
            Log.e("EdahabiaScreen", "Error during payment processing", e)
            generalError = "Error: ${e.message}"
            isLoading = false
        }
    }

    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .verticalScroll(scrollState)
        ) {
            // Header with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 15.dp, end = 15.dp, bottom = 10.dp)
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFFFF))
                        .clickable { onBackClick() }
                ) {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier.size(45.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.fleche_icon_lonly),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title "Edahabia" at the center
                Text(
                    text = "Edahabia",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card image
            Image(
                    painter = painterResource(id = R.drawable.edahbia_card_photos),
                    contentDescription = "Edahabia Card",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )

            Spacer(modifier = Modifier.height(24.dp))

            // Display general error message if any
            if (generalError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = generalError ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Card Number
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Card Number",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.filter { it.isDigit() }
                        if (digitsOnly.length <= 16) {
                            cardNumber = formatCardNumber(digitsOnly)
                            cardNumberError = null // Clear error on change
                        }
                    },
                    placeholder = { Text("0000 0000 0000 0000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (cardNumberError == null) Color.LightGray else Color.Red,
                        unfocusedBorderColor = if (cardNumberError == null) Color.LightGray else Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = cardNumberError != null
                )
                
                // Display error if any
                if (cardNumberError != null) {
                    Text(
                        text = cardNumberError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Holder Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Card Holder Name",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = { 
                        cardHolderName = it 
                        cardHolderNameError = null // Clear error on change
                    },
                    placeholder = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (cardHolderNameError == null) Color.LightGray else Color.Red,
                        unfocusedBorderColor = if (cardHolderNameError == null) Color.LightGray else Color.Red,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    isError = cardHolderNameError != null
                )
                
                // Display error if any
                if (cardHolderNameError != null) {
                    Text(
                        text = cardHolderNameError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = poppins,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Expire Date and CVV
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Expire Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expire Date",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = expireDate,
                        onValueChange = { newValue ->
                            expireDate = formatExpirationDate(newValue)
                            expireDateError = null // Clear error on change
                        },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (expireDateError == null) Color.LightGray else Color.Red,
                            unfocusedBorderColor = if (expireDateError == null) Color.LightGray else Color.Red,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = expireDateError != null
                    )
                    
                    // Display error if any
                    if (expireDateError != null) {
                        Text(
                            text = expireDateError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // CVV
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CVV",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 3) {
                                cvv = digitsOnly
                                cvvError = null // Clear error on change
                            }
                        },
                        placeholder = { Text("***") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (cvvError == null) Color.LightGray else Color.Red,
                            unfocusedBorderColor = if (cvvError == null) Color.LightGray else Color.Red,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = cvvError != null
                    )
                    
                    // Display error if any
                    if (cvvError != null) {
                        Text(
                            text = cvvError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontFamily = poppins,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save card details checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveCardDetails,
                    onCheckedChange = { saveCardDetails = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF149459),
                        uncheckedColor = Color.Black
                    )
                )
                Text(
                    text = "Save card details for future payments",
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Add space at the bottom for the button
        }

        // Continue button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { processPayment() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                Text(
                    text = "Continue",
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                }
            }
        }
    }
}

// Helper function to format card number with spaces
private fun formatCardNumber(input: String): String {
    val result = StringBuilder()
    for (i in input.indices) {
        if (i > 0 && i % 4 == 0) {
            result.append(" ")
        }
        result.append(input[i])
    }
    return result.toString()
}

// Helper function to format expiration date
private fun formatExpirationDate(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    if (digitsOnly.length <= 8) {
        val result = StringBuilder()
        for (i in digitsOnly.indices) {
            if (i == 2 || i == 4) {
                result.append("/")
            }
            result.append(digitsOnly[i])
        }
        return result.toString()
    }
    return input
}

@Preview(showBackground = true)
@Composable
fun EdahabiaScreenPreview() {
    EdahabiaScreen(
        reservationId = 12345L,
        onBackClick = {},
        onContinueClick = {}
    )
}