package com.example.myapplication.ui.screens.BookingCar



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.poppins
import android.widget.Toast
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.myapplication.ui.screens.home.CarUiState
import com.example.myapplication.ui.screens.home.CarViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    carId: String?,
    onBackPressed: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    viewModel: CarViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var isFavorite by remember { mutableStateOf(false) }
    
    // Collect the UI state from the ViewModel
    val carDetailsState by viewModel.carDetailsState.collectAsState()
    
    // State for storing the car details
    var car by remember { mutableStateOf<com.example.myapplication.data.model.Car?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load car details when screen is first displayed
    LaunchedEffect(carId) {
        carId?.toLongOrNull()?.let { id ->
            viewModel.loadCarById(id)
        }
    }
    
    // Use LaunchedEffect to show Toast in a Composable context
    LaunchedEffect(carDetailsState) {
        when (carDetailsState) {
            is CarUiState.SingleCarSuccess -> {
                car = (carDetailsState as CarUiState.SingleCarSuccess).car
            }
            is CarUiState.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (carDetailsState as CarUiState.Error).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        if (carDetailsState is CarUiState.Loading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF149459)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading car details...",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        } else if (carDetailsState is CarUiState.Error) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (carDetailsState as CarUiState.Error).message,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            carId?.toLongOrNull()?.let { id ->
                                viewModel.loadCarById(id)
                            } 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF149459)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else if (car != null) {
            // Car details content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Top Car Image Section with Back Button and Favorite Button
                TopImageSection(
                    imageUrl = car?.picture,
                    isFavorite = isFavorite,
                    onFavoriteClick = { isFavorite = !isFavorite },
                    onBackPressed = onBackPressed,
                    title = "${car?.brand} ${car?.model}",
                    showFavorite = true
                )

                // Car Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F5FA))
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                ) {
                        // Top Row - Transmission and Rating
                        CarTagAndRating(
                            transmission = car?.transmission ?: "Auto",
                            rating = car?.rating?.toFloat() ?: 4.5f
                        )

                    // Car Name
                        CarNameSection(
                            carName = "${car?.brand} ${car?.model}",
                            year = car?.year?.toString() ?: "2024"
                        )

                    // Tabs (About & Gallery)
                    TabsSection(onGalleryClick = onGalleryClick)

                    // Car Renter Section - Moved before Specifications
                    CarRenterSection(
                        onChatClick = {
                            // Toast.makeText(context, "Chat clicked", Toast.LENGTH_SHORT).show()
                            val phoneNumber = "0123456789" // Placeholder
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("sms:$phoneNumber")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCallClick = {
                            // Toast.makeText(context, "Call clicked", Toast.LENGTH_SHORT).show()
                            val phoneNumber = "0123456789" // Placeholder
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No calling app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // Car Details (Specifications)
                    car?.let { carData ->
                        CarDetailsContent(car = carData)
                    }
                }

                // Price and Book Now Section
                PriceAndBookSection(
                    price = "${car?.rentalPricePerDay}DA",
                    onBookNowClick = onBookNowClick
                )
            }
        }
    }
}

@Composable
fun CarDetailsContent(car: com.example.myapplication.data.model.Car) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Car specifications
        Text(
            text = "Specifications",
            fontSize = 18.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Specifications grid
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecificationItem(
                    label = "Brand",
                    value = car.brand,
                    modifier = Modifier.weight(1f)
                )
                
                SpecificationItem(
                    label = "Model",
                    value = car.model,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecificationItem(
                    label = "Year",
                    value = car.year?.toString() ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                
                SpecificationItem(
                    label = "Color",
                    value = car.colour ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecificationItem(
                    label = "Transmission",
                    value = car.transmission ?: "Auto",
                    modifier = Modifier.weight(1f)
                )
                
                SpecificationItem(
                    label = "Fuel Type",
                    value = car.fuel ?: "Petrol",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Row 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecificationItem(
                    label = "Car Type",
                    value = car.type ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                
                SpecificationItem(
                    label = "Number of Seats",
                    value = car.seatingCapacity?.toString() ?: "5",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SpecificationItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@Composable
fun CarTagAndRating(
    transmission: String,
    rating: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transmission,
            fontSize = 14.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // Rating
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.green_star),
                contentDescription = "Rating",
                tint = Color(0xFF149459),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rating.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CarNameSection(
    carName: String,
    year: String
) {
    Text(
        text = "$carName $year",
        fontSize = 19.sp,
        fontFamily = poppins,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 20.dp)
    )
}

@Composable
fun PriceAndBookSection(
    price: String,
    onBookNowClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(top = 30.dp)
            .background(Color(0xFFF2F5FA))
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 20.dp)
            .padding(horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Price",
                    fontSize = 15.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Text(
                    text = "$price/day",
                    fontSize = 21.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onBookNowClick() },
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                )
            ) {
                Text(
                    text = "Book Now",
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TopImageSection(
    imageUrl: String?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackPressed: () -> Unit,
    title: String,
    showFavorite: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
    ) {
        // Main car image - now using AsyncImage
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .placeholder(R.drawable.car_placeholder) // Changed from ic_launcher_background
                .error(R.drawable.car_placeholder) // Changed from error_placeholder
                .build(),
            contentDescription = title, // Use car title as content description
            contentScale = ContentScale.Crop, // Changed to Crop for better fitting generally
            modifier = Modifier.fillMaxSize()
        )

        // Title at the top center
        Text(
            text = title,
            fontSize = 23.sp,
            fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp)
        )

        // Back button
        Box(
            modifier = Modifier
                .padding(start = 15.dp, top = 20.dp)
                .size(45.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F5FA))
                .border(2.dp, Color.White, CircleShape)
                .align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(150.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(33.dp)
                )
            }
        }

        // Favorite button - Only show if showFavorite is true
        if (showFavorite) {
            Box(
                modifier = Modifier
                    .padding(end = 15.dp, top = 20.dp)
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F5FA))
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.TopEnd),
            ) {
                IconButton(
                    onClick = onFavoriteClick,
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Car image thumbnails
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 35.dp)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(6) { index ->
                    val isMore = index == 5

                    Box(
                        modifier = Modifier
                            .size(45.dp, 45.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMore) {
                            Text(
                                text = "+99",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        } else {
                            Image(
                                painter = painterResource(
                                    id = when (index) {
                                        0 -> R.drawable.ic_launcher_background
                                        1 -> R.drawable.ic_launcher_background
                                        2 -> R.drawable.ic_launcher_background
                                        3 -> R.drawable.ic_launcher_background
                                        else -> R.drawable.ic_launcher_background
                                    }
                                ),
                                contentDescription = "Car thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabsSection(onGalleryClick: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "About",
                    fontSize = 18.sp,
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
                    .clickable { onGalleryClick() }
            ) {
                Text(
                    text = "Gallery",
                    fontSize = 18.sp,
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
            // Green divider under "About" (active tab)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(Color(0xFF149459))
            )

            // Gray divider under "Gallery" (inactive tab)
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
fun CarRenterSection(
    onChatClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.renterprofile),
                contentDescription = "User",
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
            )
        }

        // Renter Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "Rental Company",
                fontSize = 16.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Text(
                text = "Customer Service",
                fontSize = 9.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
        }

        // Chat Button - using message.xml
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
                .clickable { onChatClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.message),
                contentDescription = "Chat",
                tint = Color(0xFF007AFF),
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Call Button - using call.xml
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), CircleShape)
                .clickable { onCallClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.call),
                contentDescription = "Call",
                tint = Color(0xFF149459),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun CarImagesSection(car: com.example.myapplication.data.model.Car, onImageClick: () -> Unit = {}) {
    // Implementation of the car images section
    // Make sure any calls to Composable functions are within this Composable function
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CarDetailsScreenPreview() {
    CarDetailsScreen("1")
}