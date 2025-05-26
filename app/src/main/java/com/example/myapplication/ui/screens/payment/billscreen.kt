package com.example.myapplication.ui.screens.payment

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import com.example.myapplication.data.api.ApiStatus

@Composable
fun BillScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    viewModel: BookingViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel()
) {
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Payment flow states
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Transaction details for display
    val transactionId by paymentViewModel.transactionId.collectAsState()
    
    // Handle payment UI state changes
    LaunchedEffect(paymentUiState) {
        when (paymentUiState) {
            is PaymentUiState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is PaymentUiState.Success -> {
                isLoading = false
                errorMessage = null
                
                // Get transaction ID if available
                val successState = paymentUiState as PaymentUiState.Success
                if (successState.transactionId != null) {
                    Log.d("BillScreen", "Payment successful with transaction ID: ${successState.transactionId}")
                }
                
                // Short delay to show success before navigating
                delay(800)
                
                // Navigate to next screen on success
                onContinueClick()
            }
            is PaymentUiState.Error -> {
                isLoading = false
                errorMessage = (paymentUiState as PaymentUiState.Error).message
                Log.e("BillScreen", "Payment error: ${errorMessage}")
            }
            is PaymentUiState.ValidationError -> {
                isLoading = false
                errorMessage = "Please check your payment details and try again."
            }
            is PaymentUiState.Initial -> {
                isLoading = false
                errorMessage = null
            }
        }
    }
    
    // Currency formatter
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("DZD")
    }
    
    // Format prices as currency
    val carPricePerDay = currencyFormatter.format(viewModel.carPrice).replace("DZD", "DA")
    val driverFees = currencyFormatter.format(viewModel.driverFees).replace("DZD", "DA")
    val totalPrice = currencyFormatter.format(viewModel.totalPrice).replace("DZD", "DA")
    
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

                // Title "Bill" at the center
                Text(
                    text = "Bill",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Display error message if any
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontFamily = poppins,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Rest of the content in a scrollable column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.car_placeholder),
                        contentDescription = "Car image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.36f))
                                .padding(horizontal = 5.dp)
                        ) {
                            Text(
                                text = viewModel.carTransmission,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "${viewModel.carName} ${viewModel.carYear}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "$carPricePerDay/day",
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.36f))
                            .padding(vertical = 2.dp, horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.star_for_the_review_lonly),
                                contentDescription = "Rating icon",
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = viewModel.carRating.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(27.dp))

                BillDetailItem("Pick-Up Date & Time", "${viewModel.pickUpDate} | ${viewModel.pickUpTime}")
                BillDetailItem("Drop-Off Date & Time", "${viewModel.dropOffDate} | ${viewModel.dropOffTime}")
                BillDetailItem("Driver option", viewModel.driverOption)
                BillDetailItem("Amount", "$carPricePerDay/day")

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 15.dp)
                )

                // Total Days row (added as shown in the screenshot)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Days",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "${viewModel.totalDays}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                // Driver fees
                PriceDetailItem("Driver Fees", driverFees)

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 15.dp)
                )

                PriceDetailItem("Total", totalPrice, true)

                // Transaction ID display if available
                if (!transactionId.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECF8F2))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Transaction Details",
                                fontSize = 16.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF149459)
                            )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                            Text(
                                text = "Transaction ID: ${transactionId ?: ""}",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = "Date: ${Date().toString()}",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = "Status: Pending Confirmation",
                                fontSize = 14.sp,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
                
                // Personal Information Section
                Spacer(modifier = Modifier.height(24.dp))
                
                // Clean design for Personal Information section to match the screenshot
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Name:",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${viewModel.firstName} ${viewModel.lastName}",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phone:",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.phoneNumber,
                            fontSize = 16.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Email:",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.email,
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Wilaya
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Wilaya:",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.wilaya,
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // License
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "License:",
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.driverLicenseFileName,
                            fontFamily = poppins,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
                        
                Spacer(modifier = Modifier.height(100.dp)) // Extra space for button
            }
        }
        
        // Confirm button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Show loading indicator first
                    isLoading = true
                    errorMessage = null
                    
                    // Create a transaction ID if needed
                    if (transactionId == null) {
                        val txn = UUID.randomUUID().toString()
                        Log.d("BillScreen", "Generated new transaction ID: $txn")
                    }
                    
                    // Always use a valid reservation ID
                    val bookingId = if (viewModel.reservationId > 0) {
                        viewModel.reservationId
                    } else {
                        // Generate a temporary ID if needed
                        System.currentTimeMillis()
                    }
                    
                    // Make sure we have a valid ID before proceeding
                    if (bookingId > 0) {
                        // If we already have a valid reservation ID, always use that
                        viewModel.updateReservationId(bookingId)
                        Log.d("BillScreen", "Using reservation ID: $bookingId")
                        
                        // Process as Edahabia payment only
                        paymentViewModel.processPayment(
                            reservationId = bookingId,
                            paymentMethod = "EDAHABIA", // Explicitly for Edahabia electronic payment
                            amount = viewModel.totalPrice
                        )
                    } else {
                        // This should never happen now, but handle it just in case
                        isLoading = false
                        errorMessage = "Invalid reservation data. Please try again."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
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
                        text = "Confirm Payment",
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // Update the reservation status when payment is successful
        LaunchedEffect(transactionId) {
            if (!transactionId.isNullOrEmpty() && viewModel.reservationId > 0) {
                Log.d("BillScreen", "Payment confirmed with transaction ID: $transactionId - updating reservation status")
                reservationViewModel.updateReservationStatus(viewModel.reservationId, "PAID")
            }
        }
    }
}

@Composable
fun BillDetailItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@Composable
fun PriceDetailItem(title: String, value: String, isTotal: Boolean = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
            text = title,
            fontSize = if (isTotal) 18.sp else 16.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) Color.Black else Color.Black.copy(alpha = 0.7f)
            )

            Text(
                text = value,
            fontSize = if (isTotal) 20.sp else 16.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isTotal) Color(0xFF149459) else Color.Black
            )
        }
}

@Preview(showBackground = true)
@Composable
fun BillScreenPreview() {
    BillScreen(
        onBackClick = {},
        onContinueClick = {}
    )
}