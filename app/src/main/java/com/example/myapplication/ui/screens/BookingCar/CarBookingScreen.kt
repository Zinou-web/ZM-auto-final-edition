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
    bookingViewModel: BookingViewModel = viewModel()
) {
    // Context for date and time pickers
    val context = LocalContext.current
    
    // Load car details based on ID
    LaunchedEffect(carId) {
        viewModel.loadCarDetails(carId)
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
                TopImageSection(
                    imageUrl = null, // Placeholder - Car object not available
                    isFavorite = false,
                    onFavoriteClick = {},
                    onBackPressed = onBackPressed,
                    title = "Car Booking",
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
                Divider(color = Color.Black, thickness = 1.dp) // User requested Color.Black
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
                            Divider(color = Color.LightGray)
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
                // Continue Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Button(
                        onClick = { 
                            // Populate the shared BookingViewModel with the car details and booking information
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
                                days = viewModel.calculateDurationInDays()
                            )
                            
                            onContinue() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF149459),
                            disabledContainerColor = Color(0xFFABD6C2)
                        ),
                        enabled = viewModel.isFormValid()
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 18.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
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