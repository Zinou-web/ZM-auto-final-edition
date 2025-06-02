package com.example.myapplication.ui.screens.BookingCar

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.myapplication.data.model.Car
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.screens.home.CarUiState
import com.example.myapplication.ui.screens.home.CarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    carId: String?,
    onBackPressed: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {},
    viewModel: CarViewModel = hiltViewModel()
) {
    var isFavorite by remember { mutableStateOf(false) }
    val carDetailsState by viewModel.carDetailsState.collectAsState()
    var car by remember { mutableStateOf<Car?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(carId) {
        carId?.toLongOrNull()?.let {
            viewModel.loadCarById(it)
        }
    }

    LaunchedEffect(carDetailsState) {
        when (val state = carDetailsState) {
            is CarUiState.SingleCarSuccess -> {
                car = state.car
                // Potentially update isFavorite based on car.isFavorited or similar
            }
            is CarUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        if (carDetailsState is CarUiState.Loading && car == null) {
            // Show loading indicator only if car is not yet loaded
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF149459))
            }
        } else if (car != null) {
            // Scrollable content when car data is available
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp) // Add padding at the bottom for the fixed price section
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Car Image Section with Back Button and Favorite Button
                TopImageSection(
                    imageUrl = car!!.picture, // Pass the car's picture URL
                    isFavorite = isFavorite, 
                    onFavoriteClick = { isFavorite = !isFavorite },
                    onBackPressed = onBackPressed,
                    title = "${car!!.brand} ${car!!.model}", 
                    showFavorite = true
                )

                // Car Info Section (before tabs)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F5FA))
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                ) {
                    // Top Row - Auto tag and Rating
                    GalleryCarTagAndRating(
                        transmission = car!!.transmission ?: "N/A",
                        rating = car!!.rating?.toFloat() ?: 0f
                    )

                    // Car Name
                    GalleryCarNameSection(
                        carName = car!!.brand ?: "",
                        model = car!!.model ?: "",
                        year = car!!.year?.toString() ?: ""
                    )

                    // Tab titles
                    GalleryTabTitles(onAboutClick = onAboutClick) // onAboutClick navigates to CarDetails
                }

                // Tab indicator/divider (full width)
                GalleryTabDivider()

                // Provide multiple images for i10 (ID 11), otherwise a single image
                val imageUrls = if (car!!.id == 11L) {
                    listOf(
                        "android.resource://com.example.myapplication/drawable/car_details_i10",
                        "android.resource://com.example.myapplication/drawable/grandi10grandi10frontview",
                        "android.resource://com.example.myapplication/drawable/grandi10grandi10dashboard",
                        "android.resource://com.example.myapplication/drawable/grandi10grandi10frontrowseats",
                        "android.resource://com.example.myapplication/drawable/grandi10grandi10rearview"
                    )
                } else listOf(car!!.picture).filterNotNull()
                GalleryContent(imageUrls = imageUrls)
            }

            // Fixed Price and Book Now Section at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Ensures it's at the bottom of the Box
                    // .padding(top = 720.dp) // This fixed padding might not be ideal, let's see
                    .fillMaxWidth()
            ) {
                GalleryPriceAndBookSection(
                    pricePerDay = car!!.rentalPricePerDay.toString(),
                    onBookNowClick = onBookNowClick
                )
            }
        } else if (carDetailsState is CarUiState.Error && car == null) {
            // Optional: Show error if car is null and there was an error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Failed to load car details. Please try again.", color = Color.Red)
            }
        }
        // If car is null and not loading and not error, it might be an empty screen before carId is processed.
        // Or after an error that didn't set carDetailsState to Error explicitly.
    }
}

@Composable
fun GalleryTopImageSection(
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
    ) {
        // Main car image
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Car Image",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Title "Car Details" at the top center
        Text(
            text = "Car Details",
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

        // Favorite button
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
                items(listOf(0, 1, 2, 3, 4, 5)) { index ->
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
fun GalleryCarTagAndRating(
    transmission: String,
    rating: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Auto",
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
                text = "$rating",
                fontSize = 17.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun GalleryCarNameSection(
    carName: String,
    model: String,
    year: String
) {
    Text(
        text = "$carName $model $year",
        fontSize = 19.sp,
        fontFamily = poppins,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )
}

@Composable
fun GalleryTabTitles(onAboutClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { onAboutClick() }
        ) {
            Text(
                text = "About",
                fontSize = 18.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(90.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Gallery",
                fontSize = 18.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GalleryTabDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        // Gray divider under "About" (half width)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        // Green divider under "Gallery" (half width)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(Color(0xFF149459))
        )
    }
}

@Composable
fun GalleryContent(
    imageUrls: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        // Photos Header with View All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photos",
                fontSize = 20.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            // "View all" can be made clickable later if needed
            Text(
                text = "View all",
                fontSize = 14.sp,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
        }

        if (imageUrls.isEmpty()) {
            Text("No images available for this car.", modifier = Modifier.padding(16.dp))
        } else {
            // Display photos in a grid (2 columns)
            val rows = imageUrls.chunked(2)

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp) // Spacing between rows
            ) {
                rows.forEach { rowPhotos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                            // .padding(horizontal = 8.dp), // Padding for items within the row
                        horizontalArrangement = Arrangement.spacedBy(25.dp), // Spacing between items in a row
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowPhotos.forEach { imageUrl ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .placeholder(R.drawable.car_placeholder) // Default placeholder
                                    .error(R.drawable.car_placeholder) // Default error placeholder (ensure this exists)
                                    .build(),
                                contentDescription = "Car Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .weight(1f) // Each image takes equal space in the row
                                    .aspectRatio(1f) // Make images square, or adjust as needed
                                    // .size(170.dp,130.dp) // Replaced by weight and aspectRatio
                                    .clip(RoundedCornerShape(15.dp))
                            )
                        }
                        // If there's only one photo in the last row, add a spacer to maintain alignment
                        if (rowPhotos.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryPriceAndBookSection(
    pricePerDay: String,
    onBookNowClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(top = 30.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(Color(0xFFF2F5FA))
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 20.dp)
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
                    text = "$pricePerDay/day",
                    fontSize = 21.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Book Now Button
            Button(
                onClick = onBookNowClick,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GalleryScreenPreview() {
    GalleryScreen(
        carId = "1" // Example carId, or null
        // photos = listOf( // This parameter no longer exists
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        //     R.drawable.ic_launcher_background,
        // )
    )
}