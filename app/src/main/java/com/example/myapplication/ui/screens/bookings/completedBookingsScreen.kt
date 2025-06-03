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
import com.example.myapplication.R
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.ui.screens.BookingCar.ReservationUiState
import com.example.myapplication.ui.screens.BookingCar.ReservationViewModel
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.ui.screens.home.FavoriteViewModel
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedBookingsScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMyBookingsClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUpcomingTabClick: () -> Unit = {},
    onRebookClick: (String) -> Unit = {},
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
                            onRebookClick = { reservation -> 
                                // Store the selected reservation in the ViewModel
                                viewModel.setSelectedReservation(reservation)
                                // Navigate to car booking screen with the car ID
                                onRebookClick("${reservation.carId}")
                            },
                            onCarClick = { carId -> 
                                /* Navigate to car details */
                            }
                        )
                    }
                }
            }
        }

        // Bottom Navigation Bar
        CompletedBookingsBottomNavBar(
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
    onRebookClick: (Reservation) -> Unit,
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
                onRebookClick = { onRebookClick(reservation) },
                onCarClick = { onCarClick(reservation.carId) },
                favoriteViewModel = hiltViewModel()
            )
        }
    }
}

@Composable
fun CompletedBookingItem(
    reservation: Reservation,
    onRebookClick: (Reservation) -> Unit,
    onCarClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    // Get car ID from reservation
    val carId = reservation.car?.id ?: 0L
    
    // Get favorite status from ViewModel
    val favoriteStatusMap by favoriteViewModel.favoriteStatusMap.collectAsState()
    val isFavorite = favoriteStatusMap[carId] ?: false
    
    // Check favorite status for this car when first displayed
    LaunchedEffect(carId) {
        favoriteViewModel.checkFavoriteStatus(carId)
    }
    
    // Get context for intent actions
    val context = LocalContext.current
    
    // Function to navigate to Batna location
    val navigateToBatna: () -> Unit = {
        val gmmIntentUri = Uri.parse("geo:35.5607,6.1734?q=Batna,Algeria")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://www.google.com/maps/search/?api=1&query=Batna,Algeria"))
            context.startActivity(browserIntent)
        }
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
                            painter = when {
                                reservation.car?.picture?.isNotEmpty() == true -> rememberAsyncImagePainter(reservation.car.picture)
                                reservation.car?.model?.contains("i10", ignoreCase = true) == true -> painterResource(id = R.drawable.car_details_i10)
                                reservation.car?.model?.contains("Yaris", ignoreCase = true) == true -> painterResource(id = R.drawable.yaristoprated)
                                reservation.car?.model?.contains("A3", ignoreCase = true) == true -> painterResource(id = R.drawable.a3mostpopfinal)
                                else -> painterResource(id = R.drawable.car_placeholder)
                            },
                            contentDescription = "${reservation.car?.brand} ${reservation.car?.model}",
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
                    // Car Name
                    Text(
                        text = "${reservation.car?.brand ?: "Toyota"} ${reservation.car?.model ?: "Corolla"}",
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
                            text = reservation.car?.type ?: "Sedan",
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
                            text = "Automatic",
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
                            text = "Petrol",
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
                        color = Color(0xFF149459),
                        modifier = Modifier.clickable { navigateToBatna() }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Map placeholder
                Image(
                    painter = painterResource(id = R.drawable.batna),
                    contentDescription = "Map of Batna",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Book Again button
                Button(
                    onClick = { onRebookClick(reservation) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Book Again",
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