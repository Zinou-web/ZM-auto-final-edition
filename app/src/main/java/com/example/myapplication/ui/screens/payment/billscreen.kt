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
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat

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
                // Navigate to next screen on success
                onContinueClick()
            }
            is PaymentUiState.Error -> {
                isLoading = false
                errorMessage = (paymentUiState as PaymentUiState.Error).message
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
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
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
                    modifier = Modifier.padding(vertical = 35.dp)
                )

                PriceDetailItem("Car price", carPricePerDay)
                PriceDetailItem("Driver fees", driverFees)

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 35.dp)
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
                        
                Spacer(modifier = Modifier.height(100.dp)) // Extra space for button
            }
        }

        // Confirm button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    // Handle both confirmation and payment initiation
                    if (transactionId.isNullOrEmpty()) {
                        // If we don't have a transaction ID yet, this is a new payment
                        
                        // Show loading indicator
                        isLoading = true
                        
                        // Create a new reservation first if needed
                        if (viewModel.reservationId <= 0) {
                            // Log current state
                            Log.d("BillScreen", "Creating new reservation for car: ${viewModel.carName}")
                            
                            // Parse dates
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                            val startDate = try {
                                val date = dateFormat.parse(viewModel.pickUpDate)
                                date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                                    ?: LocalDate.now()
                            } catch (e: Exception) {
                                Log.e("BillScreen", "Error parsing start date: ${e.message}")
                                LocalDate.now()
                            }
                            
                            val endDate = try {
                                val date = dateFormat.parse(viewModel.dropOffDate)
                                date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                                    ?: LocalDate.now().plusDays(3)
                            } catch (e: Exception) {
                                Log.e("BillScreen", "Error parsing end date: ${e.message}")
                                LocalDate.now().plusDays(3)
                            }
                            
                            // Default car ID if not specified (for testing)
                            val carId = viewModel.carId.takeIf { it > 0 } ?: 1L
                            
                            // Create the reservation using the ViewModel
                            lifecycleOwner.lifecycleScope.launch {
                                try {
                                    // Create reservation first
                                    reservationViewModel.createReservation(
                                        carId = carId,
                                        startDate = startDate,
                                        endDate = endDate,
                                        totalPrice = viewModel.totalPrice
                                    )
                                    
                                    // Wait for reservation to be created
                                    delay(1000)
                                    
                                    // Check if we got a reservation ID from the SingleReservationSuccess state
                                    val state = reservationViewModel.reservationState.value
                                    if (state is ReservationUiState.SingleReservationSuccess) {
                                        // Use the newly created reservation ID
                                        val newReservationId = state.reservation.id
                                        viewModel.updateReservationId(newReservationId)
                                        
                                        // Log the reservation ID
                                        Log.d("BillScreen", "Created reservation with ID: $newReservationId")
                                        
                                        // Process payment with the new reservation ID
                                        paymentViewModel.processPayment(
                                            reservationId = newReservationId,
                                            paymentMethod = "billscreen_confirm", 
                                            amount = viewModel.totalPrice
                                        )
                                    } else {
                                        // Handle reservation creation failure
                                        Log.e("BillScreen", "Failed to create reservation: $state")
                                        isLoading = false
                                        errorMessage = "Could not create reservation. Please try again."
                                    }
                                } catch (e: Exception) {
                                    // Handle any exceptions
                                    Log.e("BillScreen", "Error during reservation creation: ${e.message}", e)
                                    isLoading = false
                                    errorMessage = "An error occurred. Please try again."
                        }
                    }
                        } else {
                            // We already have a reservation ID, just process payment
                            Log.d("BillScreen", "Using existing reservation ID: ${viewModel.reservationId}")

                            // Process payment with existing reservation ID
                            paymentViewModel.processPayment(
                                reservationId = viewModel.reservationId,
                                paymentMethod = "billscreen_confirm", 
                                amount = viewModel.totalPrice
                            )
                        }
                    } else {
                        // If we already have a transaction ID, just continue to the next screen
                        onContinueClick()
                    }
                },
                    modifier = Modifier
                        .fillMaxWidth()
                    .height(52.dp),
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
                        text = "Confirm Payment",
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