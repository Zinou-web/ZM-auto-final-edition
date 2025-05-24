package com.example.myapplication.ui.screens.bookings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedBookingsScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMyBookingsClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUpcomingTabClick: () -> Unit = {},
    onRebookClick: () -> Unit = {},
    viewModel: ReservationViewModel = hiltViewModel()
) {
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    // Observe past reservations
    val pastReservations by viewModel.pastReservations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Load reservations when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadPastReservations()
    }
    
    // Handle error states
    LaunchedEffect(uiState) {
        if (uiState is ReservationUiState.Error) {
            Toast.makeText(
                context,
                (uiState as ReservationUiState.Error).message,
                Toast.LENGTH_LONG
            ).show()
        }
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
                .padding(bottom = 80.dp)
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title "My Bookings" at the center
                Text(
                    text = "My Bookings",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab selector (Upcoming / Completed)
            CompletedBookingTabRow(onUpcomingTabClick = onUpcomingTabClick)

            Spacer(modifier = Modifier.height(10.dp))

            // Content for completed bookings
            when (uiState) {
                is ReservationUiState.Loading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF149459))
                    }
                }
                is ReservationUiState.Error -> {
                    // Show error message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error loading bookings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uiState as ReservationUiState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadPastReservations() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF149459)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    if (pastReservations.isEmpty()) {
                        // Show empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.empty_bookings),
                                    contentDescription = "No bookings",
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No completed bookings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your booking history will appear here",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onMyBookingsClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF149459)
                                    )
                                ) {
                                    Text("View Upcoming Bookings")
                                }
                            }
                        }
                    } else {
                        // Show completed bookings list
                        CompletedBookingsList(
                            reservations = pastReservations,
                            onRebookClick = onRebookClick,
                            onCarClick = { /* Navigate to car details */ }
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        MyBookingsBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onMyBookingsClick = onMyBookingsClick,
            onFavoriteClick = onFavoriteClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun CompletedBookingTabRow(onUpcomingTabClick: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUpcomingTabClick() }
            ) {
                Text(
                    text = "Upcoming",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(90.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "Completed",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            // Indicator line under active tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFF149459))
            )
        }
    }
}

@Composable
fun CompletedBookingsList(
    reservations: List<Reservation>,
    onRebookClick: () -> Unit,
    onCarClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(reservations) { reservation ->
            CompletedBookingItem(
                reservation = reservation,
                onRebookClick = onRebookClick,
                onCarClick = { onCarClick(reservation.carId) }
            )
        }
    }
}

@Composable
fun CompletedBookingItem(
    reservation: Reservation,
    onRebookClick: () -> Unit,
    onCarClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val startDate = reservation.startDate.format(dateFormatter)
    val endDate = reservation.endDate.format(dateFormatter)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCarClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Car details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${reservation.car?.brand ?: "Car"} ${reservation.car?.model ?: ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = "Booking ID: ${reservation.id}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when (reservation.status) {
                                "COMPLETED" -> Color(0xFF149459).copy(alpha = 0.1f)
                                "CANCELLED" -> Color(0xFFFF5252).copy(alpha = 0.1f)
                                else -> Color(0xFF149459).copy(alpha = 0.1f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = reservation.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (reservation.status) {
                            "COMPLETED" -> Color(0xFF149459)
                            "CANCELLED" -> Color(0xFFFF5252)
                            else -> Color(0xFF149459)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Booking period
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "From",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = startDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                
                Column {
                    Text(
                        text = "To",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = endDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                
                Column {
                    Text(
                        text = "Total",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "${reservation.totalPrice}DA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF149459)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rebook button
            Button(
                onClick = onRebookClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Again")
            }
        }
    }
}

@Composable
fun CompletedBookingsBottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onMyBookingsClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompletedBookingsBottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                onClick = onHomeClick
            )

            CompletedBookingsBottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                isSelected = true,
                onClick = onMyBookingsClick
            )

            CompletedBookingsBottomNavItem(
                iconRes = R.drawable.heart,
                label = "Favorite",
                onClick = onFavoriteClick
            )

            CompletedBookingsBottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun CompletedBookingsBottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val itemColor = if (isSelected) Color.Black else Color.Gray
    val bgColor = if (isSelected) Color(0xFFEADDFA) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = itemColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = itemColor,
                fontFamily = poppins
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CompletedBookingsScreenPreview() {
    CompletedBookingsScreen()
}