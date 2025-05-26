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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import java.time.format.DateTimeFormatter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.ui.screens.home.FavoriteViewModel
import android.util.Log


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController = rememberNavController(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCompletedTabClick: () -> Unit = {},
    viewModel: ReservationViewModel = hiltViewModel()
) {
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    // Observe upcoming reservations
    val upcomingReservations by viewModel.upcomingReservations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Load reservations when screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("MyBookingsScreen", "Loading upcoming reservations")
        viewModel.loadUpcomingReservations()
    }
    
    // Handle error states
    LaunchedEffect(uiState) {
        when (uiState) {
            is ReservationUiState.Error -> {
                val errorMsg = (uiState as ReservationUiState.Error).message
                Log.e("MyBookingsScreen", "Error state: $errorMsg")
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
            is ReservationUiState.Loading -> {
                Log.d("MyBookingsScreen", "Loading state")
            }
            else -> {
                Log.d("MyBookingsScreen", "UI State: $uiState")
            }
        }
    }
    
    // Function to navigate to E-Receipt screen
    val navigateToEReceipt = { reservationId: Long ->
        Log.d("MyBookingsScreen", "Navigating to EReceipt with ID: $reservationId")
        try {
            // Make sure reservationId is valid
            if (reservationId > 0) {
                // Find the reservation to ensure we have valid car data
                val reservation = upcomingReservations.find { it.id == reservationId }
                
                if (reservation != null) {
                    // Verify car data exists and log it for debugging
                    val carInfo = reservation.car?.let { "${it.brand} ${it.model}" } ?: "Unknown"
                    Log.d("MyBookingsScreen", "Found reservation $reservationId with car: $carInfo (carId: ${reservation.carId})")
                    
                    // Use the exact route format matching the composable definition
                    val route = "${Screen.EReceipt.route}/$reservationId"
                    Log.d("MyBookingsScreen", "Navigation route: $route")
                    navController.navigate(route)
                } else {
                    Log.e("MyBookingsScreen", "Reservation $reservationId not found in current list")
                    Toast.makeText(
                        context,
                        "Could not find reservation details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.e("MyBookingsScreen", "Invalid reservation ID: $reservationId")
                Toast.makeText(
                    context,
                    "Cannot view receipt: Invalid reservation ID",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("MyBookingsScreen", "Navigation error: ${e.message}", e)
            Toast.makeText(
                context,
                "Error opening receipt: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Update the lambda where we call this navigation
    val upcomingItemClick: (Long) -> Unit = { reservationId ->
        navigateToEReceipt(reservationId)
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
            BookingTabRow(onCompletedTabClick = onCompletedTabClick)

            Spacer(modifier = Modifier.height(10.dp))

            // Content for upcoming bookings
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
                                onClick = { viewModel.loadUpcomingReservations() },
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
                    if (upcomingReservations.isEmpty()) {
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
                                    text = "No upcoming bookings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your upcoming bookings will appear here",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { navController.navigate(Screen.Home.name) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF149459)
                                    )
                                ) {
                                    Text("Browse Cars")
                                }
                            }
                        }
                    } else {
                        // Show upcoming bookings list
                        UpcomingBookingsList(
                            reservations = upcomingReservations,
                            onCancelClick = { reservationId -> 
                                // Navigate to CancellationScreen with the reservation ID
                                navController.navigate("${Screen.Cancelation.name}?reservationId=${reservationId}")
                            },
                            onBillClick = upcomingItemClick,
                            onCarClick = { carId ->
                                navController.navigate("${Screen.CarDetails.name}/$carId")
                            },
                            onModifyClick = { reservation ->
                                // Set the selected reservation for modification
                                viewModel.setSelectedReservation(reservation)
                                
                                // Navigate to CarBookingScreen with the car ID
                                // The CarBookingScreen will check if there's a selected reservation
                                navController.navigate("${Screen.CarBooking.name}/${reservation.carId}")
                            },
                            favoriteViewModel = hiltViewModel()
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        MyBookingsBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onMyBookingsClick = { /* Already on MyBookings */ },
            onFavoriteClick = onFavoriteClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun BookingTabRow(onCompletedTabClick: () -> Unit = {}) {
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
            ) {
                Text(
                    text = "Upcoming",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(90.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCompletedTabClick() }
            ) {
                Text(
                    text = "Completed",
                    fontSize = 17.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
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
                    .background(Color(0xFF149459))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun UpcomingBookingsList(
    reservations: List<Reservation>,
    onCancelClick: (Long) -> Unit,
    onBillClick: (Long) -> Unit,
    onCarClick: (Long) -> Unit,
    onModifyClick: (Reservation) -> Unit = {},
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(reservations) { reservation ->
            UpcomingBookingItem(
                reservation = reservation,
                onCancelClick = { onCancelClick(reservation.id) },
                onBillClick = { onBillClick(reservation.id) },
                onCarClick = { onCarClick(reservation.carId) },
                onModifyClick = { onModifyClick(reservation) },
                favoriteViewModel = favoriteViewModel
            )
        }
    }
}

@Composable
fun UpcomingBookingItem(
    reservation: Reservation,
    onCancelClick: () -> Unit,
    onBillClick: (Long) -> Unit,
    onCarClick: () -> Unit,
    onModifyClick: () -> Unit = {},
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    // Get car ID from reservation
    val carId = reservation.car?.id ?: reservation.carId
    
    // Debug log the car info to track down incorrect data
    LaunchedEffect(Unit) {
        Log.d("UpcomingBookingItem", "Showing reservation ID: ${reservation.id}")
        Log.d("UpcomingBookingItem", "Car info: ${reservation.car?.brand} ${reservation.car?.model} (ID: $carId)")
    }
    
    // Get favorite status from ViewModel
    val favoriteStatusMap by favoriteViewModel.favoriteStatusMap.collectAsState()
    val isFavorite = favoriteStatusMap[carId] ?: false
    
    // Check favorite status for this car when first displayed
    LaunchedEffect(carId) {
        favoriteViewModel.checkFavoriteStatus(carId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .clickable(onClick = onCarClick)
    ) {
        // Outer white card containing everything
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Car Image with padding inside a card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Car Image
                        Image(
                            painter = painterResource(id = R.drawable.car_placeholder),
                            contentDescription = 
                                reservation.car?.let { "${it.brand} ${it.model}" } ?: "Car Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Rating Badge
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, top = 12.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "${reservation.car?.rating ?: 5}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Heart Icon - Now integrated with FavoriteViewModel
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp, top = 12.dp)
                                .align(Alignment.TopEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { favoriteViewModel.toggleFavorite(carId) }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) Color(0xFFFF4444) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Car Title and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Car Name - Use car info directly from reservation.car instead of hardcoded values
                    Text(
                        text = reservation.car?.let { "${it.brand} ${it.model}" } ?: "Car Details Unavailable",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Price
                    Text(
                        text = "${reservation.totalPrice}DA / day",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF149459)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add divider here
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Icons - EXACTLY as in Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Color
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ColorLens,
                            contentDescription = "Color",
                            tint = Color(0xFF149459),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = reservation.car?.colour ?: "N/A",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                    
                    // Type
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Type",
                            tint = Color(0xFF149459),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = reservation.car?.type ?: "Hatchback",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                    
                    // Transmission
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Speed,
                            contentDescription = "Transmission",
                            tint = Color(0xFF149459),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = reservation.car?.transmission ?: "Automatic",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                    
                    // Fuel
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalGasStation,
                            contentDescription = "Fuel",
                            tint = Color(0xFF149459),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = reservation.car?.fuel ?: "Petrol",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }

                // Booking specific parts below
                Spacer(modifier = Modifier.height(24.dp))
                
                // Car Location section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Car Location",
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "Navigate",
                        fontSize = 14.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        modifier = Modifier.clickable { /* Navigate action */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Map placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel and Bill buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button
                    Button(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF149459)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // E-Receipt button (was Bill button)
                    Button(
                        onClick = {
                            // Make sure reservation ID is valid
                            val id = reservation.id
                            Log.d("UpcomingBookingItem", "E-Receipt button clicked for reservation ID: $id")
                            if (id > 0) {
                                onBillClick(id)
                            } else {
                                Log.e("UpcomingBookingItem", "Invalid reservation ID: $id")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF149459)
                        )
                    ) {
                        Text(
                            text = "E-Receipt",
                            fontSize = 16.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyBookingsBottomNavBar(
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
            MyBookingsBottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                onClick = onHomeClick
            )

            MyBookingsBottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                isSelected = true,
                onClick = onMyBookingsClick
            )

            MyBookingsBottomNavItem(
                iconRes = R.drawable.heart,
                label = "Favorite",
                onClick = onFavoriteClick
            )

            MyBookingsBottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun MyBookingsBottomNavItem(
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
fun MyBookingsScreenPreview() {
    MyBookingsScreen()
}