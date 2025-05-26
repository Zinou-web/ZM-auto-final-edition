package com.example.myapplication.ui.screens.payment



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins

@Composable
fun PaymentPending(
    onBackToHomeClick: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
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
            // Header with title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 15.dp, end = 15.dp, bottom = 10.dp)
            ) {
                // Title removed as requested
                
                // Empty spacers for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterStart))
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Content with pending icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.payment_pending_photos),
                        contentDescription = "Pending icon",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp)
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment Pending – Cash",
                    color = Color(0xFF000000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please pay in cash when picking up the car.\nA valid ID will be required at pickup.",
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    modifier = Modifier
                        .padding(horizontal = 32.dp).alpha(0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Reservation information card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Reservation Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Reservation ID
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Reservation ID:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "#${bookingViewModel.reservationId}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Car details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Car:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = bookingViewModel.carName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pick-up:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${bookingViewModel.pickUpDate} | ${bookingViewModel.pickUpTime}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Drop-off:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${bookingViewModel.dropOffDate} | ${bookingViewModel.dropOffTime}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Total price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Price:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${bookingViewModel.totalPrice} DA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF149459)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Payment status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Payment Status:",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Pay on Arrival",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFFFA000) // Amber color for pending
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onBackToHomeClick,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459)
                    )
                ) {
                    Text(
                        text = "Back to Main page",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentPendingPreview() {
    PaymentPending(
        onBackToHomeClick = {}
    )
}
