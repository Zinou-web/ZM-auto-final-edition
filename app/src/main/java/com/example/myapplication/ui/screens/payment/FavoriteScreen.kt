package com.example.myapplication.ui.screens.payment

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.SecondaryGreen
import com.example.myapplication.ui.theme.BackgroundGray
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins
import com.example.myapplication.data.model.Car
import com.example.myapplication.ui.screens.home.FavoriteViewModel
import com.example.myapplication.ui.screens.home.FavoriteUiState
import androidx.compose.runtime.collectAsState

data class Car(
    val name: String,
    @DrawableRes val imageRes: Int,
    val rating: Double,
    val transmission: String,
    val fuel: String,
    val seats: Int,
    val price: String
)

// Sample car list with placeholder image resource
val carList = listOf(
    Car("Toyota RAV4 2024", R.drawable.ic_launcher_background, 4.9, "Manual", "Petrol", 5, "00.00DA/day"),
    Car("Audi RS e-tron GT", R.drawable.ic_launcher_background, 4.9, "Manual", "Petrol", 5, "00.00DA/day"),
    Car("BMW X5 M 2023", R.drawable.ic_launcher_background, 4.8, "Auto", "Diesel", 5, "00.00DA/day"),
    Car("Mercedes-Benz GLC", R.drawable.ic_launcher_background, 4.7, "Auto", "Hybrid", 5, "00.00DA/day")
)

val filterTypes = listOf("All", "SUV", "Sedan", "Sports", "Electric")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCarClick: (String) -> Unit = {},
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val selectedFilter = remember { mutableStateOf("All") }
    val isSearchActive = remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }
    
    // Get favorite cars from ViewModel
    val uiState by favoriteViewModel.uiState.collectAsState()
    val favoriteCars by favoriteViewModel.favoriteCars.collectAsState()
    
    // Load favorite cars when the screen is first displayed
    LaunchedEffect(Unit) {
        favoriteViewModel.loadFavoriteCars()
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            Column {
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
                        onClick = {
                            if (isSearchActive.value) {
                                isSearchActive.value = false
                                searchQuery.value = ""
                            } else {
                                    onBackClick()
                            }
                        },
                            modifier = Modifier.size(45.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Title "Favourite" at the center
                    Text(
                        text = "Favourite",
                        fontSize = 23.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Search button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFFFFF))
                    ) {
                        IconButton(
                            onClick = { isSearchActive.value = true },
                            modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                    if (isSearchActive.value) {
                        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                    painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                    tint = Color.Black,
                                    modifier = Modifier.size(25.dp)
                            )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                            TextField(
                                value = searchQuery.value,
                                onValueChange = { searchQuery.value = it },
                                    placeholder = { 
                                        Text(
                                            "Search any car...",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 18.sp,
                                            color = Color.Black.copy(alpha = 0.6f),
                                            letterSpacing = 0.08.sp
                                        ) 
                                    },
                                modifier = Modifier
                                        .fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterTypes) { type ->
                            FilterChip(
                                selected = selectedFilter.value == type,
                                onClick = { selectedFilter.value = type },
                                label = {
                                    Text(
                                        text = type,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedFilter.value == type) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter.value == type,
                                    borderWidth = 1.dp,
                                    borderColor = Color(0xFFE0E0E0),
                                    selectedBorderWidth = 0.dp
                                )
                            )
                        }
                    }
                }

                if (isSearchActive.value && searchQuery.value.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Results for \"${searchQuery.value}\"",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            "00 Results Found",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        bottomBar = {
            FavoriteBottomNavBar(
                modifier = Modifier,
                onHomeClick = onHomeClick,
                onBookingsClick = onBookingsClick,
                onFavoriteClick = { /* Already on Favorites */ },
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Show appropriate content based on UI state
            when (uiState) {
                is FavoriteUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = SecondaryGreen
                            )
                        }
                    }
                }
                is FavoriteUiState.Success -> {
                    // Filter cars based on selected filter and search query
                    val filteredCars = if (searchQuery.value.isNotEmpty()) {
                        favoriteCars.filter { car ->
                            "${car.brand} ${car.model}".contains(searchQuery.value, ignoreCase = true)
                        }
                    } else if (selectedFilter.value != "All") {
                        favoriteCars.filter { car ->
                            car.type == selectedFilter.value
                        }
                    } else {
                        favoriteCars
                    }
                    
                    if (filteredCars.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.value.isNotEmpty()) 
                                        "No cars matching '${searchQuery.value}'" 
                                    else if (selectedFilter.value != "All") 
                                        "No ${selectedFilter.value} cars in favorites"
                                    else 
                                        "No favorite cars yet",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(
                            items = filteredCars,
                            key = { car: com.example.myapplication.data.model.Car -> car.id }
                        ) { car: com.example.myapplication.data.model.Car ->
                            CarCard(
                                car = car,
                                onClick = { onCarClick(car.id.toString()) }
                            )
                        }
                    }
                }
                is FavoriteUiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = (uiState as FavoriteUiState.Error).message,
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                androidx.compose.material3.Button(
                                    onClick = { favoriteViewModel.loadFavoriteCars() },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = SecondaryGreen
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                is FavoriteUiState.Empty -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.heart),
                                    contentDescription = "No favorites",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "No favorite cars yet",
                                    fontSize = 18.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Your favorite cars will appear here",
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteBottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
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
            FavoriteBottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                onClick = onHomeClick
            )

            FavoriteBottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                onClick = onBookingsClick
            )

            FavoriteBottomNavItem(
                iconRes = R.drawable.heart,
                label = "Favorite",
                isSelected = true,
                onClick = onFavoriteClick
            )

            FavoriteBottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun FavoriteBottomNavItem(
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

@Composable
fun CarCard(
    car: com.example.myapplication.data.model.Car,
    onClick: () -> Unit = {}
) {
    // State to track if this car is favorited - always true since this is the favorites screen
    var isFavorite by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick)
    ) {
        // Outer white card containing everything
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .height(160.dp),
                    shape = RoundedCornerShape(5.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = car.picture,
                            contentDescription = "${car.brand} ${car.model}",
                            contentScale = ContentScale.FillWidth,
                            placeholder = painterResource(id = R.drawable.car_placeholder),
                            error = painterResource(id = R.drawable.car_placeholder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        )

                        // Rating Badge
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 8.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Card(
                                shape = RoundedCornerShape(5.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.green_star),
                                        contentDescription = "Rating",
                                        tint = SecondaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = car.rating.toString(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Heart Icon - Now clickable
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp, top = 8.dp)
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { isFavorite = !isFavorite }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.heart),
                                contentDescription = "Remove from favorites",
                                tint = if (isFavorite) Color(0xFFFF4444) else Color.Gray,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Car Title and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Car Name
                    Text(
                        text = "${car.brand} ${car.model}",
                        fontSize = 18.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )

                    // Price
                    Text(
                        text = "${car.rentalPricePerDay}DA/day",
                        fontSize = 15.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Add divider here
                Divider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Transmission
                    FeatureItem(
                        iconRes = R.drawable.manual,
                        text = car.transmission ?: "Manual"
                    )
                    Spacer(modifier = Modifier.width(37.dp))
                    
                    // Fuel
                    FeatureItem(
                        iconRes = R.drawable.petrol,
                        text = car.fuel ?: "Petrol"
                    )
                    Spacer(modifier = Modifier.width(37.dp))
                    
                    // Seats
                    FeatureItem(
                        iconRes = R.drawable.seat,
                        text = "${car.seatingCapacity ?: 5} Seats"
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = SecondaryGreen,
            modifier = Modifier.size(25.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            fontSize = 17.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoriteScreenPreview() {
    MyApplicationTheme {
        FavoriteScreen()
    }
}