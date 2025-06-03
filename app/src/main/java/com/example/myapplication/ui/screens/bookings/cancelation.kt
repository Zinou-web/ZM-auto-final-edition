package com.example.myapplication.ui.screens.bookings


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke

class CancelationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                CancelationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelationScreen(
    onBackClick: () -> Unit = {},
    reservationId: Long = 0,
    viewModel: ReservationViewModel = hiltViewModel()
) {
    val reasons = listOf(
        "Schedule Change",
        "Book Another Car",
        "Found Better alternative",
        "My reason is not listed",
        "Want to Book Another Car",
        "Others"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var otherText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Observe UI state for cancellation
    val uiState by viewModel.reservationState.collectAsState()
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is ReservationUiState.Loading -> {
                isSubmitting = true
            }
            is ReservationUiState.Error -> {
                isSubmitting = false
                Toast.makeText(
                    context,
                    (uiState as ReservationUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            is ReservationUiState.Idle -> {
                // If we just transitioned to idle after a cancellation
                if (isSubmitting) {
                    isSubmitting = false
                    Toast.makeText(
                        context,
                        "Booking cancelled successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    onBackClick()
                }
            }
            else -> {}
        }
    }

    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title "Cancelation" at the center
            Text(
                text = "Cancelation",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
            )

                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF7F2))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "We're sorry to see you cancel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF149459)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Please let us know why you're cancelling so we can improve our service.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Please select a reason for cancellation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Radio Buttons with enhanced styling
                reasons.forEach { reason ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedReason = reason },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedReason == reason) 
                                Color(0xFFE5F5EE) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (selectedReason == reason) 1.dp else 0.dp,
                            color = if (selectedReason == reason) 
                                Color(0xFF149459) else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF149459),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedReason == reason) 
                                        Color(0xFF149459) else Color.Black.copy(alpha = 0.7f),
                                    fontWeight = if (selectedReason == reason) 
                                        FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Others TextField if selected with improved styling
                if (selectedReason == "Others") {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Please tell us more",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = { otherText = it },
                        placeholder = { 
                            Text(
                                "Please specify your reason",
                                color = Color.Gray.copy(alpha = 0.7f)
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White,
                            focusedBorderColor = Color(0xFF149459),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF149459)
                        ),
                        maxLines = 5
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Submit Button with enhanced styling
                Button(
                    onClick = { 
                        if (reservationId > 0) {
                            // Call the viewModel to cancel the reservation
                            viewModel.cancelReservation(reservationId)
                        } else {
                            // No reservation ID provided
                            Toast.makeText(
                                context,
                                "Error: No booking ID provided",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459),
                        disabledContainerColor = Color(0xFF149459).copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Confirm Cancellation", 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Cancel button
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                    ),
                    border = BorderStroke(1.dp, Color(0xFF149459))
                ) {
                    Text(
                        "Keep My Booking", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Medium, 
                        color = Color(0xFF149459)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}




