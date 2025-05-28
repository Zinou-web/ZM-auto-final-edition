package com.example.myapplication.ui.screens.payment

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins
import java.text.NumberFormat
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.launch
import okhttp3.JavaNetCookieJar
import java.net.CookieManager

@Composable
fun EReceiptScreen(
    reservationId: Long = 0L,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit = {},
    bookingViewModel: BookingViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel()
) {
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var hasError by remember { mutableStateOf(false) }
    
    // Log when screen is first composed
    LaunchedEffect(Unit) {
        Log.d("EReceiptScreen", "Screen first composed with reservationId: $reservationId")
    }
    
    // Get reservation data when screen loads
    LaunchedEffect(reservationId) {
        Log.d("EReceiptScreen", "LaunchedEffect triggered for reservationId: $reservationId")
        
        if (reservationId <= 0) {
            Log.e("EReceiptScreen", "Invalid reservation ID: $reservationId")
            errorMessage = "Invalid reservation ID"
            isLoading = false
            Toast.makeText(context, "Invalid reservation ID: $reservationId", Toast.LENGTH_LONG).show()
            // Removed the immediate back navigation to prevent exiting before error UI shows
            return@LaunchedEffect
        }
        
        coroutineScope.launch {
            try {
                Log.d("EReceiptScreen", "Attempting to fetch reservation data for ID: $reservationId")
                reservationViewModel.getReservationById(reservationId)
            } catch (e: Exception) {
                Log.e("EReceiptScreen", "Error fetching reservation: ${e.message}", e)
                errorMessage = "Failed to load receipt data: ${e.message}"
                isLoading = false
                hasError = true
                Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Observe reservation state
    val reservationState by reservationViewModel.reservationState.collectAsState()
    
    // Update UI based on reservation state
    LaunchedEffect(reservationState) {
        Log.d("EReceiptScreen", "Reservation state changed: $reservationState")
        
        try {
            when (reservationState) {
                is ReservationUiState.SingleReservationSuccess -> {
                    val reservationData = (reservationState as ReservationUiState.SingleReservationSuccess).reservation
                    
                    // Add detailed logging of the reservation data we received
                    Log.d("EReceiptScreen", "Successfully loaded reservation: ${reservationData.id}")
                    
                    // Handle missing car data safely
                    if (reservationData.car == null) {
                        Log.w("EReceiptScreen", "Car data is null in reservation ${reservationData.id}")
                    } else {
                        Log.d("EReceiptScreen", "Car data in reservation: ${reservationData.car}")
                    }
                    
                    reservation = reservationData
                    
                    // Update booking view model with data from reservation - use safe defaults for everything
                    bookingViewModel.apply {
                        // Set defaults first
                        carId = reservationData.carId
                        carName = reservationData.car?.let { "${it.brand} ${it.model}" } ?: "Car Details"
                        carYear = reservationData.car?.year?.toString() ?: ""
                        carPrice = reservationData.car?.rentalPricePerDay?.toDouble() ?: (reservationData.totalPrice / Math.max(1.0, 1.0))
                        carType = reservationData.car?.type ?: "Vehicle"
                        seats = reservationData.car?.seatingCapacity?.toString() ?: "4"
                        carRating = reservationData.car?.rating?.toFloat() ?: 4.0f
                        
                        totalPrice = reservationData.totalPrice
                        paymentMethod = reservationData.paymentStatus.ifEmpty { "Paid" }
                        
                        // Fix for reassignment issue
                        this.reservationId = reservationData.id
                        
                        // Safe handling of dates
                        pickUpDate = reservationData.startDate?.toString() ?: "N/A"
                        dropOffDate = reservationData.endDate?.toString() ?: "N/A"
                        
                        // Calculate total days safely - default to 1 day
                        totalDays = 1
                        
                        try {
                            if (reservationData.startDate != null && reservationData.endDate != null) {
                                val start = reservationData.startDate
                                val end = reservationData.endDate
                                totalDays = (end.toEpochDay() - start.toEpochDay() + 1).toInt().coerceAtLeast(1)
                            }
                        } catch (e: Exception) {
                            Log.e("EReceiptScreen", "Error calculating days: ${e.message}")
                        }
                    }
                    
                    // Log the successful data loading
                    Log.d("EReceiptScreen", "Successfully loaded data for reservation ${reservationData.id}")
                    isLoading = false
                }
                is ReservationUiState.Error -> {
                    val error = (reservationState as ReservationUiState.Error).message
                    Log.e("EReceiptScreen", "Error state: $error")
                    errorMessage = error
                    isLoading = false
                    hasError = true
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
                is ReservationUiState.Loading -> {
                    Log.d("EReceiptScreen", "Loading state")
                    isLoading = true
                }
                else -> {
                    Log.d("EReceiptScreen", "Other state: $reservationState")
                }
            }
        } catch (e: Exception) {
            Log.e("EReceiptScreen", "Error processing reservation state: ${e.message}", e)
            errorMessage = "Error processing data: ${e.message}"
            isLoading = false
            hasError = true
            Toast.makeText(context, "Error processing data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    // Currency formatter with error handling
    val currencyFormatter = remember {
        try {
            NumberFormat.getCurrencyInstance(Locale.US).apply {
                currency = Currency.getInstance("DZD")
            }
        } catch (e: Exception) {
            Log.e("EReceiptScreen", "Error creating currency formatter: ${e.message}")
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }
    
    // Format prices as currency with error handling
    val carPricePerDay = remember(bookingViewModel.carPrice) {
        try {
            currencyFormatter.format(bookingViewModel.carPrice).replace("DZD", "DA")
        } catch (e: Exception) {
            Log.e("EReceiptScreen", "Error formatting car price: ${e.message}")
            "${bookingViewModel.carPrice} DA"
        }
    }
    
    val driverFees = remember(bookingViewModel.driverFees) {
        try {
            currencyFormatter.format(bookingViewModel.driverFees).replace("DZD", "DA")
        } catch (e: Exception) {
            Log.e("EReceiptScreen", "Error formatting driver fees: ${e.message}")
            "${bookingViewModel.driverFees} DA"
        }
    }
    
    val totalPrice = remember(bookingViewModel.totalPrice) {
        try {
            currencyFormatter.format(bookingViewModel.totalPrice).replace("DZD", "DA")
        } catch (e: Exception) {
            Log.e("EReceiptScreen", "Error formatting total price: ${e.message}")
            "${bookingViewModel.totalPrice} DA"
        }
    }

    // Error handling for unexpected crashes
    if (hasError && errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F5FA)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Something went wrong",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = errorMessage ?: "Unknown error",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459)
                    )
                ) {
                    Text("Go Back")
                }
            }
        }
        return
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

                // Title "E-Receipt" at the center
                Text(
                    text = "E-Receipt",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Loading, error, or main content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF149459))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF149459))
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            } else {
                // Main content - only show if we have valid data
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Show car name from the reservation data
                    Text(
                        text = "Car: ${bookingViewModel.carName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Barcode image
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Barcode image (placeholder)
                            Image(
                                painter = painterResource(id = R.drawable.credit_card_icon),
                                contentDescription = "Barcode",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Reservation ID
                            Text(
                                text = "Reservation ID: ${reservation?.id ?: reservationId}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Details table
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                        ) {
                            // Car details
                            ReceiptRow(
                                label = "Car", 
                                value = bookingViewModel.carName
                            )
                            
                            ReceiptRow(
                                label = "Type", 
                                value = bookingViewModel.carType ?: "SUV"
                            )
                            
                            ReceiptRow(
                                label = "Seats", 
                                value = bookingViewModel.seats ?: "4"
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            
                            // Date details
                            ReceiptRow(
                                label = "Pick-Up Date", 
                                value = bookingViewModel.pickUpDate
                            )
                            
                            ReceiptRow(
                                label = "Return Date", 
                                value = bookingViewModel.dropOffDate
                            )
                            
                            ReceiptRow(
                                label = "Driver Option", 
                                value = bookingViewModel.driverOption ?: "Self-Driver"
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            
                            // Pricing details
                            ReceiptRow(
                                label = "Amount", 
                                value = "${carPricePerDay}/day"
                            )
                            
                            ReceiptRow(
                                label = "Total Days", 
                                value = "${bookingViewModel.totalDays}"
                            )
                            
                            ReceiptRow(
                                label = "Driver Fees", 
                                value = driverFees
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            
                            // Total
                            ReceiptRow(
                                label = "Total", 
                                value = totalPrice,
                                isHighlighted = true
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color.LightGray
                            )
                            
                            // Payment method
                            ReceiptRow(
                                label = "Payment Method", 
                                value = bookingViewModel.paymentMethod ?: "Cash"
                            )
                            
                            // Payment status
                            ReceiptRow(
                                label = "Status", 
                                value = reservation?.status ?: "Confirmed"
                            )
                        }
                    }
                    
                    // Continue button
                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF149459)
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = poppins,
            color = Color.DarkGray
        )
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = poppins,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) Color.Black else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EReceiptScreenPreview() {
    EReceiptScreen(onBackClick = {})
}

class TestOkhttp {
    fun test() {
        val cookieManager = CookieManager()
        val cookieJar = JavaNetCookieJar(cookieManager)
        println(cookieJar)
    }
} 