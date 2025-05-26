package com.example.myapplication.ui.screens.BookingCar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Assuming these composables are in the same package or imported correctly.
// If they are in 'carsdetails.kt' in the same package, they should be accessible.
// If not, proper imports would be needed.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarBookingScreen(
    carId: String? = null,
    onBackPressed: () -> Unit = {},
    onContinue: () -> Unit = {},
    viewModel: CarBookingViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel(),
    reservationViewModel: ReservationViewModel = viewModel()
) {
    // Context for date and time pickers
    val context = LocalContext.current
    
    // Check if we're modifying or rebooking an existing reservation
    val selectedReservation by reservationViewModel.selectedReservation.collectAsState()
    
    // Load car details based on ID
    LaunchedEffect(carId) {
        viewModel.loadCarDetails(carId)
    }
    
    // If we have a selected reservation, pre-fill the form with its details
    LaunchedEffect(selectedReservation) {
        selectedReservation?.let { reservation ->
            // Pre-fill the date and time fields
            val pickUpCalendar = Calendar.getInstance().apply {
                timeInMillis = reservation.startDate.toEpochDay() * 24 * 60 * 60 * 1000
                set(Calendar.HOUR_OF_DAY, 10) // Default time if not available
                set(Calendar.MINUTE, 0)
            }
            
            val dropOffCalendar = Calendar.getInstance().apply {
                timeInMillis = reservation.endDate.toEpochDay() * 24 * 60 * 60 * 1000
                set(Calendar.HOUR_OF_DAY, 10) // Default time if not available
                set(Calendar.MINUTE, 0)
            }
            
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
            
            // Update view model with reservation data
            viewModel.updatePickUpDate(dateFormatter.format(pickUpCalendar.time), pickUpCalendar)
            viewModel.updatePickUpTime(timeFormatter.format(pickUpCalendar.time), pickUpCalendar)
            viewModel.updateDropOffDate(dateFormatter.format(dropOffCalendar.time), dropOffCalendar)
            viewModel.updateDropOffTime(timeFormatter.format(dropOffCalendar.time), dropOffCalendar)
            
            // Set rent type based on car details
            if (reservation.car != null) {
                viewModel.updateRentType(
                    if (reservation.car.transmission.equals("Automatic", ignoreCase = true)) "With Driver" else "Self-Driver"
                )
            }
        }
    }
    
    // Get UI state from ViewModel
    val uiState = viewModel.uiState
    
    // Date and time formatters
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)
    
    // Date picker dialogs
    val pickupDatePicker = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val calendar = viewModel.pickUpCalendar.clone() as Calendar
            calendar.set(year, month, dayOfMonth)
            viewModel.updatePickUpDate(
                dateFormatter.format(calendar.time), 
                calendar
            )
        },
        viewModel.pickUpCalendar.get(Calendar.YEAR),
        viewModel.pickUpCalendar.get(Calendar.MONTH),
        viewModel.pickUpCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }
    
    val dropoffDatePicker = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val calendar = viewModel.dropOffCalendar.clone() as Calendar
            calendar.set(year, month, dayOfMonth)
            viewModel.updateDropOffDate(
                dateFormatter.format(calendar.time),
                calendar
            )
        },
        viewModel.dropOffCalendar.get(Calendar.YEAR),
        viewModel.dropOffCalendar.get(Calendar.MONTH),
        viewModel.dropOffCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = viewModel.pickUpCalendar.timeInMillis
    }
    
    // Time picker dialogs
    val pickupTimePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val calendar = viewModel.pickUpCalendar.clone() as Calendar
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.updatePickUpTime(
                timeFormatter.format(calendar.time),
                calendar
            )
        },
        viewModel.pickUpCalendar.get(Calendar.HOUR_OF_DAY),
        viewModel.pickUpCalendar.get(Calendar.MINUTE),
        false
    )
    
    val dropoffTimePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val calendar = viewModel.dropOffCalendar.clone() as Calendar
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.updateDropOffTime(
                timeFormatter.format(calendar.time),
                calendar
            )
        },
        viewModel.dropOffCalendar.get(Calendar.HOUR_OF_DAY),
        viewModel.dropOffCalendar.get(Calendar.MINUTE),
        false
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        if (uiState.isLoading) {
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF149459)
            )
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Reuse TopImageSection from CarDetailsScreen, but with title 'Car Booking' and no favorite
                val screenTitle = when {
                    selectedReservation != null && selectedReservation?.status == "COMPLETED" -> "Rebook Car"
                    selectedReservation != null -> "Modify Booking"
                    else -> "Car Booking"
                }
                
            TopImageSection(
                imageUrl = null, // Placeholder - Car object not available
                isFavorite = false,
                onFavoriteClick = {},
                onBackPressed = onBackPressed,
                    title = screenTitle,
                showFavorite = false
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Car Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                    CarTagAndRating( 
                        transmission = uiState.carTransmission, 
                        rating = uiState.carRating
                )
                    CarNameSection(
                        carName = uiState.carName, 
                        year = uiState.carYear
                )
            }
                HorizontalDivider(color = Color.Black, thickness = 1.dp) // Updated to HorizontalDivider
            Spacer(modifier = Modifier.height(10.dp))
            // Rent Type
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = "Rent type",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    Button(
                            onClick = { viewModel.updateRentType("Self-Driver") },
                        colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.rentType == "Self-Driver") Color(0xFF149459) else Color.White,
                                contentColor = if (viewModel.rentType == "Self-Driver") Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp), // User specified 20.dp
                        modifier = Modifier.weight(1f)
                    ) {
                            Text("Self-Driver", fontSize = 16.sp, fontFamily = poppins, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                            onClick = { viewModel.updateRentType("With Driver") },
                        colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.rentType == "With Driver") Color(0xFF149459) else Color.White,
                                contentColor = if (viewModel.rentType == "With Driver") Color.White else Color.Black
                        ),
                            shape = RoundedCornerShape(30.dp), // User specified 30.dp for this one
                        modifier = Modifier.weight(1f)
                    ) {
                            Text("With Driver", fontSize = 16.sp, fontFamily = poppins, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (viewModel.rentType == "With Driver") {
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                                text = "Additional 1000.00DA Driver Cost if you Choose With Driver Option",
                            fontSize = 13.sp,
                            fontFamily = poppins,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Pick-up and Drop-off
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = "Pick-up date and time",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    Button(
                        onClick = { pickupDatePicker.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "Date",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                viewModel.pickUpDate,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                    }
                    Button(
                        onClick = { pickupTimePicker.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timepick),
                            contentDescription = "Time",
                            tint = Color.Black,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                viewModel.pickUpTime,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Drop-off date and time",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    Button(
                            onClick = { dropoffDatePicker.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "Date",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                viewModel.dropOffDate,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                    }
                    Button(
                        onClick = { dropoffTimePicker.show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timepick),
                            contentDescription = "Time",
                            tint = Color.Black,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                viewModel.dropOffTime,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Price information
                if (viewModel.pickUpDate != "Date" && viewModel.dropOffDate != "Date") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Booking Summary",
                                fontSize = 18.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Duration:",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${viewModel.calculateDurationInDays()} days",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Car Price/Day:",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${uiState.carPrice} DA",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                    }
                            if (viewModel.rentType == "With Driver") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Driver Cost/Day:",
                                        fontSize = 16.sp,
                                        fontFamily = poppins,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "1000.00 DA",
                                        fontSize = 16.sp,
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                }
            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.LightGray) // Updated to HorizontalDivider
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Price:",
                                    fontSize = 18.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${viewModel.calculateTotalPrice()} DA",
                                    fontSize = 18.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF149459)
                                )
                            }
                        }
                    }
                }
                
            Spacer(modifier = Modifier.height(25.dp))
                // Update button text based on action
                val buttonText = when {
                    selectedReservation != null && selectedReservation?.status == "COMPLETED" -> "Rebook Car"
                    selectedReservation != null -> "Update Booking"
                    else -> "Continue"
                }
                
                // Continue button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                        .padding(18.dp)
                        .padding(bottom = 60.dp)
            ) {
                Button(
                    onClick = { 
                        // Prevent continue if form is invalid
                        if (!viewModel.isFormValid()) {
                            android.widget.Toast.makeText(
                                context,
                                "Please select both pick-up and drop-off date and time",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        // Calculate the rental duration
                        val diffMillis = viewModel.dropOffCalendar.timeInMillis - viewModel.pickUpCalendar.timeInMillis
                        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1
                        
                        // Calculate driver fees if applicable
                        val driverFees = if (viewModel.rentType == "With Driver") 1000.00 * diffDays else 0.0
                        
                        // Calculate total price
                        val totalPrice = uiState.carPrice * diffDays + driverFees
                        
                        // Log details before updating BookingViewModel
                        Log.d("CarBookingScreen", "Updating BookingViewModel with details:")
                        Log.d("CarBookingScreen", "  Car Name: ${uiState.carName}")
                        Log.d("CarBookingScreen", "  Car Year: ${uiState.carYear}")
                        Log.d("CarBookingScreen", "  Car Price (from uiState): ${uiState.carPrice}")
                        Log.d("CarBookingScreen", "  Car Transmission: ${uiState.carTransmission}")
                        Log.d("CarBookingScreen", "  Car Rating: ${uiState.carRating}")
                        Log.d("CarBookingScreen", "  Rent Type: ${viewModel.rentType}")
                        Log.d("CarBookingScreen", "  Pick Up Date: ${viewModel.pickUpDate}")
                        Log.d("CarBookingScreen", "  Pick Up Time: ${viewModel.pickUpTime}")
                        Log.d("CarBookingScreen", "  Drop Off Date: ${viewModel.dropOffDate}")
                        Log.d("CarBookingScreen", "  Drop Off Time: ${viewModel.dropOffTime}")
                        Log.d("CarBookingScreen", "  Total Days: ${diffDays.toInt()}")
                        Log.d("CarBookingScreen", "  Calculated Total Price: $totalPrice")

                        // Save booking details to ViewModel
                        bookingViewModel.updateCarDetails(
                            name = uiState.carName,
                            year = uiState.carYear,
                            price = uiState.carPrice,
                            transmission = uiState.carTransmission,
                            rating = uiState.carRating,
                            rentType = viewModel.rentType,
                            pickUp = viewModel.pickUpDate,
                            pickUpT = viewModel.pickUpTime,
                            dropOff = viewModel.dropOffDate,
                            dropOffT = viewModel.dropOffTime,
                            days = diffDays.toInt()
                        )
                        
                        // Set the carId in BookingViewModel
                        val carIdLong = carId?.toLongOrNull() ?: 0L
                        Log.d("CarBookingScreen", "  Car ID (for updateCarId): $carIdLong") // Log carIdLong
                        bookingViewModel.updateCarId(carIdLong)
                        Log.d("CarBookingScreen", "Updated BookingViewModel carId to $carIdLong")
                        
                        if (selectedReservation != null) {
                            // Handle modification or rebooking
                            
                            if (selectedReservation?.status == "COMPLETED") {
                                // Rebook - create a new reservation
                                val startDate = dateToLocalDate(viewModel.pickUpCalendar.time)
                                val endDate = dateToLocalDate(viewModel.dropOffCalendar.time)
                                
                                reservationViewModel.rebookFromPastReservation(
                                    originalReservationId = selectedReservation!!.id,
                                    startDate = startDate,
                                    endDate = endDate
                                )
                            } else {
                                // Modify existing reservation
                                val startDate = dateToLocalDate(viewModel.pickUpCalendar.time)
                                val endDate = dateToLocalDate(viewModel.dropOffCalendar.time)
                                
                                reservationViewModel.updateReservation(
                                    reservationId = selectedReservation!!.id,
                                    startDate = startDate,
                                    endDate = endDate,
                                    totalPrice = totalPrice
                                )
                            }
                            
                            // Clear the selected reservation
                            reservationViewModel.clearSelectedReservation()
                        }
                        
                        // Continue to next screen
                        onContinue()
                    },
                        enabled = viewModel.isFormValid(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isFormValid()) Color(0xFF149459) else Color.Gray,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                            .height(50.dp)
                ) {
                    Text(
                            text = buttonText,
                            color = Color.White,
                            fontSize = 17.sp,
                        fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold
                    )
                }
            }
            }
        }
    }
}

// Helper function to convert java.util.Date to java.time.LocalDate
private fun dateToLocalDate(date: Date): java.time.LocalDate {
    val calendar = Calendar.getInstance().apply { time = date }
    return java.time.LocalDate.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CarBookingScreenWithVMPreview() {
    CarBookingScreen(
        carId = "1",
        onBackPressed = {},
        onContinue = {}
    )
} 