package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.FilterParams
import com.example.myapplication.ui.theme.poppins
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Filter(
    onBackClick: () -> Unit = {},
    onApplyFilters: (FilterParams) -> Unit = {},
    onResetFilters: () -> Unit = {},
    viewModel: CarViewModel = hiltViewModel()
) {
    val green = Color(0xFF149459)
    val types = listOf("All", "SUV", "Sedan", "Compact", "Luxury", "Electric")
    val brands = listOf(
        Brand("None", R.drawable.none_withoutcircle),
        Brand("Mercedes", R.drawable.mercedess),
        Brand("BMW", R.drawable.bmw),
        Brand("Audi", R.drawable.audi_lonly),
        Brand("Volkswagen", R.drawable.vols),
        Brand("Tesla", R.drawable.tes_a),
        Brand("Toyota", R.drawable.toyota),
        Brand("Nissan", R.drawable.nissan),
        Brand("Peugeot", R.drawable.peugoet)
    )
    val reviewOptions = listOf(
        ReviewOption(4.5f, 5f, "5 Star", 5),
        ReviewOption(4.0f, 4.5f, "4.0 - 4.5", 4),
        ReviewOption(3.5f, 4.0f, "3.0 - 3.5", 4),
        ReviewOption(3.0f, 3.5f, "2.5 - 3.0", 3),
        ReviewOption(2.5f, 3.0f, "2.0 - 2.5", 2)
    )
    var selectedType by remember { mutableStateOf("All") }
    var selectedBrand by remember { mutableStateOf("None") }
    var priceValue by remember { mutableStateOf(0.3f) }
    var selectedReview by remember { mutableStateOf(0) }
    
    // State to show filtered results directly in the filter screen
    var showTestResults by remember { mutableStateOf(false) }
    var filteredCars by remember { mutableStateOf<List<com.example.myapplication.data.model.Car>>(emptyList()) }
    
    // Collect the UI state from the ViewModel
    val carUiState by viewModel.uiState.collectAsState()
    
    // Update filtered cars when UI state changes
    LaunchedEffect(carUiState) {
        if (carUiState is CarUiState.Success) {
            filteredCars = (carUiState as CarUiState.Success).cars
            Log.d("Filter", "FILTER DEBUG: Got ${filteredCars.size} filtered cars")
        }
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // For improved price range display
    val minPrice = 0
    val maxPrice = 5000
    val currentPrice = (minPrice + (maxPrice - minPrice) * priceValue).roundToInt()
    
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Scaffold(
        containerColor = Color(0xFFF6F7F9),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topPadding)
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
                            painter = painterResource(id = R.drawable.fleche_icon_lonly),
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Title "Filter" at the center
                Text(
                    text = "Filter",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            // Types section
            Text("Types", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                types.forEach { type ->
                    val selected = selectedType == type
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) green else Color.White)
                            .border(1.dp, if (selected) green else Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { 
                                selectedType = type 
                                Log.d("Filter", "Selected type: $type")
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(type, color = if (selected) Color.White else Color.Black, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Brands section
            Text("Brands", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                brands.forEach { brand ->
                    val selected = selectedBrand == brand.name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { 
                                selectedBrand = brand.name 
                                Log.d("Filter", "Selected brand: ${brand.name}")
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (selected) green else Color.White)
                                .border(2.dp, if (selected) green else Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = brand.iconRes),
                                contentDescription = brand.name,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(brand.name, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Price Range Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Price Range (Hourly)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        "${currentPrice}DA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = green
                    )
                }

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = priceValue,
                    onValueChange = { priceValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = green,
                        activeTrackColor = green,
                        inactiveTrackColor = Color.LightGray
                    )
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${minPrice}DA", fontSize = 12.sp, color = Color.Gray)
                    Text("${maxPrice}DA", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Reviews section
            Text("Reviews", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                reviewOptions.forEachIndexed { idx, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { selectedReview = idx }
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                    ) {
                        DynamicStarRow(stars = option.starsToShow, color = green)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            option.label,
                            fontSize = 17.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        CompactRadioButton(
                            selected = selectedReview == idx,
                            onClick = { selectedReview = idx },
                            color = green
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Selected filter summary
            Text(
                "Selected Filters",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            
            Spacer(Modifier.height(8.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Type: ${if (selectedType == "All") "Any" else selectedType}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Brand: ${if (selectedBrand == "None") "Any" else selectedBrand}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Max Price: ${currentPrice}DA",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Min Rating: ${
                            when (selectedReview) {
                                0 -> "4.5"
                                1 -> "4.0"
                                2 -> "3.5"
                                3 -> "3.0"
                                4 -> "2.5"
                                else -> "0"
                            }
                        } stars",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        selectedType = "All"
                        selectedBrand = "None"
                        priceValue = 0.3f
                        selectedReview = 0
                        
                        // Reset filters directly using the ViewModel
                        viewModel.applyFilters(null)
                        
                        // Show toast
                        android.widget.Toast.makeText(
                            context,
                            "Filters reset",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        // Navigate back
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = green),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Reset Filter", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { 
                        // Create filter parameters
                        val filterParams = FilterParams(
                            type = if (selectedType == "All") null else selectedType,
                            brand = if (selectedBrand == "None") null else selectedBrand,
                            maxPrice = currentPrice,
                            minRating = when (selectedReview) {
                                0 -> 4.5f
                                1 -> 4.0f
                                2 -> 3.5f
                                3 -> 3.0f
                                4 -> 2.5f
                                else -> 0f
                            }
                        )
                        
                        // Log filter parameters for debugging
                        Log.d("Filter", "FILTER DEBUG: Applying filters:")
                        Log.d("Filter", "FILTER DEBUG: Type: ${filterParams.type ?: "None"}")
                        Log.d("Filter", "FILTER DEBUG: Brand: ${filterParams.brand ?: "None"}")
                        Log.d("Filter", "FILTER DEBUG: Min Rating: ${filterParams.minRating}")
                        Log.d("Filter", "FILTER DEBUG: Max Price: ${filterParams.maxPrice}")
                        
                        try {
                            // Apply filters directly using the ViewModel
                            viewModel.applyFilters(filterParams)
                            
                            // Show toast with filter summary
                            val filterSummary = buildString {
                                if (filterParams.type != null) append("Type: ${filterParams.type} ")
                                if (filterParams.brand != null) append("Brand: ${filterParams.brand} ")
                                if (filterParams.minRating > 0) append("Rating: ${filterParams.minRating}+ ")
                                if (filterParams.maxPrice < 5000) append("Max Price: ${filterParams.maxPrice}DA ")
                                if (isEmpty()) append("All cars")
                            }
                            
                            android.widget.Toast.makeText(
                                context,
                                "Filters applied: $filterSummary",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            
                            // Navigate back with a delay to ensure the filter is applied
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onBackClick()
                            }, 100)
                        } catch (e: Exception) {
                            Log.e("Filter", "Error applying filters", e)
                            android.widget.Toast.makeText(
                                context,
                                "Error applying filters: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(30.dp))
                ) {
                    Text("Apply", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
            
            // Test button to show filtered results directly in the filter screen
            Button(
                onClick = { 
                    showTestResults = !showTestResults
                    if (showTestResults) {
                        // Apply filters to test
                        val filterParams = FilterParams(
                            type = if (selectedType == "All") null else selectedType,
                            brand = if (selectedBrand == "None") null else selectedBrand,
                            maxPrice = currentPrice,
                            minRating = when (selectedReview) {
                                0 -> 4.5f
                                1 -> 4.0f
                                2 -> 3.5f
                                3 -> 3.0f
                                4 -> 2.5f
                                else -> 0f
                            }
                        )
                        
                        // Apply filters directly to ViewModel
                        viewModel.loadCarsWithFilters(
                            brand = filterParams.brand,
                            model = filterParams.type,
                            minRating = if (filterParams.minRating > 0) filterParams.minRating.toLong() else null
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (showTestResults) "Hide Test Results" else "Test Filter (Show Results)", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            
            // Show test results if enabled
            if (showTestResults) {
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Test Results (${filteredCars.size} cars)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Show loading state
                if (carUiState is CarUiState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = green)
                    }
                }
                // Show error state
                else if (carUiState is CarUiState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${(carUiState as CarUiState.Error).message}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Show filtered cars
                else if (filteredCars.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        filteredCars.forEach { car ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "${car.brand} ${car.model}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Type: ${car.type}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Rating: ${car.rating}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                // Show no results message
                else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cars match the selected filters",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterPreview() {
    Filter()
}

data class Brand(val name: String, val iconRes: Int)
data class ReviewOption(val min: Float, val max: Float, val label: String, val starsToShow: Int)

@Composable
fun DynamicStarRow(stars: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp) // Minimal space between stars
    ) {
        repeat(stars) {
            Icon(
                painter = painterResource(id = R.drawable.star_for_the_review_lonly),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        // Fill remaining stars with empty/gray stars up to 5
        repeat(5 - stars) {
            Icon(
                painter = painterResource(id = R.drawable.star_for_the_review_lonly),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun CompactRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .border(1.dp, if (selected) color else Color.Gray, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}