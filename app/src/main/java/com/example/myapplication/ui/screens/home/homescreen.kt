package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.poppins
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.myapplication.data.model.FilterParams
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.filled.SearchOff
import com.example.myapplication.ui.screens.home.FavoriteViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCarClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCatalogClick: () -> Unit = {},
    viewModel: CarViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    var expandedBrands by remember { mutableStateOf(false) }
    var selectedBrand by remember { mutableStateOf("All") }
    
    // Collect the UI state from the ViewModel
    val carUiState by viewModel.uiState.collectAsState()
    
    // State for storing the cars to display
    var cars by remember { mutableStateOf<List<com.example.myapplication.data.model.Car>>(emptyList()) }
    
    // Observe current filters
    val filters by viewModel.currentFilters.collectAsState()
    
    // State for the filter bottom sheet
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    
    // Filter states
    var selectedFilterType by remember { mutableStateOf("All") }
    var selectedFilterBrand by remember { mutableStateOf("None") }
    var filterPriceValue by remember { mutableStateOf(0.3f) } // Default to 30% of max price
    var selectedFilterRating by remember { mutableStateOf(0) } // Default to highest rating
    
    // For improved price range display
    val minPrice = 0
    val maxPrice = 5000
    val currentFilterPrice = (minPrice + (maxPrice - minPrice) * filterPriceValue).roundToInt()
    
    // Track if filters are applied
    val isFiltered = remember(filters) {
        filters?.type != null || 
        filters?.brand != null || 
        (filters?.minRating ?: 0f) > 0 || 
        (filters?.maxPrice != null && (filters?.maxPrice ?: 5000) < 5000)
    }
    
    // Reset filters function
    val resetFilters = {
        viewModel.applyFilters(null) // Pass null to reset filters
        selectedBrand = "All"
        searchQuery = ""
    }
    
    // Update UI when filters change
    LaunchedEffect(filters) {
        // We already have the filters observed from the viewModel, no need to update them again
    }
    
    // Process UI state changes
    LaunchedEffect(carUiState) {
        when (carUiState) {
            is CarUiState.Success -> {
                cars = (carUiState as CarUiState.Success).cars
            }
            is CarUiState.PaginatedSuccess -> {
                cars = (carUiState as CarUiState.PaginatedSuccess).pagedResponse.content
            }
            is CarUiState.Error -> {
                // Could handle error with a SnackBar or dialog
                Log.e("HomeScreen", "Error: "+(carUiState as CarUiState.Error).message)
            }
            else -> { /* Loading state handled in the UI */ }
        }
    }
    
    // Initial load of cars
    LaunchedEffect(Unit) {
        viewModel.applyFilters(null) // Initialize with null filters to load all cars
    }

    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .verticalScroll(scrollState),
        ) {
            // Top bar with profile icon and notification
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Field
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchCars(it)
                    },
                    placeholder = {
                        Text(
                            "Search any car...",
                            fontFamily = poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.6f),
                            letterSpacing = 0.08.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                viewModel.loadAllCars()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear Search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Filter Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .clickable { showFilterBottomSheet = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = "Filter",
                        tint = Color.Black,
                        modifier = Modifier.size(25.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Notification icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFFFF))
                        .clickable(onClick = onNotificationClick)
                ) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset((-2).dp, 2.dp)
                    ) {
                        Text(text = "2")
                    }
                    
                    Icon(
                        painter = painterResource(id = R.drawable.notification_icon),
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .padding(10.dp)
                            .size(25.dp),
                        tint = Color(0xFF149459)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top Brands Section - only show when not filtering/searching
            if (!isFiltered && searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Brands",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand Logos Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First brand is always "All"
                item {
                    CarBrandItem(
                        brandName = "All",
                        iconRes = R.drawable.all,
                        isSelected = selectedBrand == "All",
                        onClick = {
                            resetFilters()
                            expandedBrands = !expandedBrands
                        }
                    )
                }

                // Always visible brands
                val visibleBrands = listOf(
                    Pair("Tesla", R.drawable.teslatopbrand),
                    Pair("BMW", R.drawable.bmwtopbrand),
                    Pair("Toyota", R.drawable.toyota),
                    Pair("Audi", R.drawable.auditopbrand)
                )

                items(visibleBrands) { (brand, icon) ->
                    CarBrandItem(
                        brandName = brand,
                        iconRes = icon,
                        isSelected = selectedBrand == brand,
                        onClick = {
                            selectedBrand = brand
                            viewModel.applyFilters(FilterParams(brand = brand))
                        }
                    )
                }

                // Expandable brands - only visible if expanded
                if (expandedBrands) {
                    val extendedBrands = listOf(
                        Pair("Mercedes", R.drawable.mercedestopbrand),
                        Pair("Volkswagen", R.drawable.wolswagen)
                    )

                    items(extendedBrands) { (brand, icon) ->
                        CarBrandItem(
                            brandName = brand,
                            iconRes = icon,
                            isSelected = selectedBrand == brand,
                            onClick = {
                                selectedBrand = brand
                                viewModel.applyFilters(FilterParams(brand = brand))
                            }
                        )
                    }
                }
            }

                Spacer(modifier = Modifier.height(16.dp))

                // Most Popular Cars Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
            Text(
                        text = "Most Popular Cars",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                        color = Color.Black
            )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Popular Cars Horizontal List
                val popularCars by viewModel.popularCars.collectAsState()
                
                if (popularCars.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                        items(popularCars) { car ->
                            PopularCarItem(
                                car = car,
                                onClick = { onCarClick(car.id.toString()) },
                                favoriteViewModel = hiltViewModel()
                            )
                        }
                    }
                } else {
                    // Show loading indicator if popular cars are not loaded yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF149459),
                            strokeWidth = 2.dp
                    )
                }
            }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Rated Cars or Search Results Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Text(
                    text = if (isFiltered || searchQuery.isNotEmpty()) "Search Results" else "Top Rated Cars",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                
                if (carUiState is CarUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF149459),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            // Show active filters
            if (isFiltered) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active filters:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filters?.let { currentFilters ->
                            if (currentFilters.type != null) {
                                FilterChip(
                                    text = "Type: ${currentFilters.type}", 
                                    color = Color(0xFF149459),
                                    onClear = {
                                        // Clear just the type filter
                                        viewModel.applyFilters(currentFilters.copy(type = null))
                                    }
                                )
                            }
                            
                            if (currentFilters.brand != null) {
                                FilterChip(
                                    text = "Brand: ${currentFilters.brand}", 
                                    color = Color(0xFF149459),
                                    onClear = {
                                        // Clear just the brand filter
                                        viewModel.applyFilters(currentFilters.copy(brand = null))
                                        selectedBrand = "All"
                                    }
                                )
                            }
                            
                            if (currentFilters.minRating > 0) {
                                FilterChip(
                                    text = "Rating: ${currentFilters.minRating}+", 
                                    color = Color(0xFF149459),
                                    onClear = {
                                        // Clear just the rating filter
                                        viewModel.applyFilters(currentFilters.copy(minRating = 0f))
                                    }
                                )
                            }
                            
                            if (currentFilters.maxPrice < 5000) {
                                FilterChip(
                                    text = "Max Price: ${currentFilters.maxPrice}DA", 
                                    color = Color(0xFF149459),
                                    onClear = {
                                        // Clear just the price filter
                                        viewModel.applyFilters(currentFilters.copy(maxPrice = 5000))
                                    }
                                )
                            }
                        }
                        
                        Button(
                            onClick = { resetFilters() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear all filters",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Clear All",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Show loading or error state
            when (carUiState) {
                is CarUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF149459)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading cars...",
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                is CarUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (carUiState as CarUiState.Error).message,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Red
                            )
                Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    if (selectedBrand == "All") {
                                        viewModel.loadAllCars()
                                    } else {
                                        viewModel.filterByBrand(selectedBrand)
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
                }
                else -> {
                    // Display cars if we have them
                    if (cars.isNotEmpty()) {
                        CarListSection(cars, onCarClick)
                    } else {
                        // No cars found
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SearchOff,
                                    contentDescription = "No results",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) 
                                        "No cars found matching '$searchQuery'" 
                                    else 
                                        "No cars found with current filters",
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                if (isFiltered || searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { resetFilters() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF149459)
                                        )
                                    ) {
                                        Text("Clear Search & Filters")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = { /* Already on Home */ },
            onCatalogClick = onCatalogClick,
            onFavoriteClick = onFavoriteClick,
            onProfileClick = onProfileClick
        )
        
        // Filter Bottom Sheet
        if (showFilterBottomSheet) {
            val green = Color(0xFF149459)
            val types = listOf("All", "SUV", "Sedan", "Compact", "Luxury", "Electric")
            val brands = listOf(
                "None", "Mercedes", "BMW", "Audi", "Volkswagen", 
                "Tesla", "Toyota", "Nissan", "Peugeot"
            )
            val reviewOptions = listOf(
                Triple(4.5f, 5f, "5 Star"),
                Triple(4.0f, 4.5f, "4.0 - 4.5"),
                Triple(3.5f, 4.0f, "3.0 - 3.5"),
                Triple(3.0f, 3.5f, "2.5 - 3.0"),
                Triple(2.5f, 3.0f, "2.0 - 2.5")
            )
            
            // Update filter states from current filters when sheet opens
            LaunchedEffect(Unit) {
                filters?.let { currentFilters ->
                    selectedFilterType = currentFilters.type ?: "All"
                    selectedFilterBrand = currentFilters.brand ?: "None"
                    selectedFilterRating = when (currentFilters.minRating) {
                        4.5f -> 0
                        4.0f -> 1
                        3.5f -> 2
                        3.0f -> 3
                        2.5f -> 4
                        else -> 0
                    }
                    // Calculate price slider value
                    if (currentFilters.maxPrice < maxPrice) {
                        filterPriceValue = (currentFilters.maxPrice - minPrice).toFloat() / (maxPrice - minPrice)
                    }
                }
            }
            
            ModalBottomSheet(
                onDismissRequest = { showFilterBottomSheet = false },
                containerColor = Color(0xFFF6F7F9),
                sheetState = androidx.compose.material3.rememberModalBottomSheetState(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter",
                            fontSize = 23.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        IconButton(onClick = { showFilterBottomSheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Black
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Types section
                    Text("Types", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        types.forEach { type ->
                            val selected = selectedFilterType == type
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) green else Color.White)
                                    .border(1.dp, if (selected) green else Color.LightGray, RoundedCornerShape(8.dp))
                                    .clickable { selectedFilterType = type }
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
                            val selected = selectedFilterBrand == brand
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable { selectedFilterBrand = brand }
    ) {
        Box(
            modifier = Modifier
                                        .size(48.dp)
                .clip(CircleShape)
                                        .background(if (selected) green else Color.White)
                                        .border(2.dp, if (selected) green else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
                                    Text(
                                        text = brand.first().toString(),
                                        color = if (selected) Color.White else Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(brand, fontSize = 12.sp, color = Color.Black)
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
                                "${currentFilterPrice}DA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = green
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Slider(
                            value = filterPriceValue,
                            onValueChange = { filterPriceValue = it },
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
                        reviewOptions.forEachIndexed { idx, (min, max, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { selectedFilterRating = idx }
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                            ) {
                                // Stars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    repeat(5) { starIdx ->
                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (starIdx < (min + max) / 2) green else Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                        )
                                    }
                                }

                                Spacer(Modifier.width(7.dp))

                        Text(
                                    label,
                                    fontSize = 17.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Radio button
                                RadioButton(
                                    selected = selectedFilterRating == idx,
                                    onClick = { selectedFilterRating = idx },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = green,
                                        unselectedColor = Color.Gray
                                    )
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
                                text = "Type: ${if (selectedFilterType == "All") "Any" else selectedFilterType}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Brand: ${if (selectedFilterBrand == "None") "Any" else selectedFilterBrand}",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Max Price: ${currentFilterPrice}DA",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Min Rating: ${
                                    when (selectedFilterRating) {
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
                                selectedFilterType = "All"
                                selectedFilterBrand = "None"
                                filterPriceValue = 0.3f
                                selectedFilterRating = 0
                                
                                // Reset filters
                                viewModel.loadAllCars()
                                
                                // Show toast
                                android.widget.Toast.makeText(
                                    context,
                                    "Filters reset",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                
                                // Close the sheet
                                showFilterBottomSheet = false
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
                                    type = if (selectedFilterType == "All") null else selectedFilterType,
                                    brand = if (selectedFilterBrand == "None") null else selectedFilterBrand,
                                    maxPrice = currentFilterPrice,
                                    minRating = when (selectedFilterRating) {
                                        0 -> 4.5f
                                        1 -> 4.0f
                                        2 -> 3.5f
                                        3 -> 3.0f
                                        4 -> 2.5f
                                        else -> 0f
                                    }
                                )
                                
                                // Log filter parameters for debugging
                                Log.d("HomeScreen", "FILTER DEBUG: Applying filters:")
                                Log.d("HomeScreen", "FILTER DEBUG: Type: ${filterParams.type ?: "None"}")
                                Log.d("HomeScreen", "FILTER DEBUG: Brand: ${filterParams.brand ?: "None"}")
                                Log.d("HomeScreen", "FILTER DEBUG: Min Rating: ${filterParams.minRating}")
                                Log.d("HomeScreen", "FILTER DEBUG: Max Price: ${filterParams.maxPrice}")
                                
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
                                    
                                    // Close the sheet
                                    showFilterBottomSheet = false
                                } catch (e: Exception) {
                                    Log.e("HomeScreen", "Error applying filters", e)
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
                }
            }
        }
    }
}

@Composable
fun CarBrandItem(
    brandName: String,
    iconRes: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Special case for "All" - only show border when explicitly selected
    val showBorder = when {
        brandName == "All" && isSelected -> true   // "All" is selected, show border
        brandName != "All" && isSelected -> true   // Other brand is selected, show border
        else -> false                              // Not selected, no border
    }
    
            Column(
        horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            .width(60.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (showBorder) 1.5.dp else 0.dp,
                    color = if (showBorder) Color(0xFF149459) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = brandName,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

                Text(
            text = brandName,
            fontSize = 12.sp,
                    fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF149459) else Color.Gray,
            textAlign = TextAlign.Center
                )
    }
}

@Composable
fun CarItem(
    car: com.example.myapplication.data.model.Car,
    onClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    // Get favorite status from ViewModel
    val favoriteStatusMap by favoriteViewModel.favoriteStatusMap.collectAsState()
    val isFavorite = favoriteStatusMap[car.id] ?: false
    
    // Check favorite status for this car when first displayed
    LaunchedEffect(car.id) {
        favoriteViewModel.checkFavoriteStatus(car.id)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        // Outer white card containing everything
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        // Car Image: use specific images for each car ID
                        val imageResource = when(car.id) {
                            1L -> "android.resource://com.example.myapplication/drawable/car_details_i10"
                            2L -> "android.resource://com.example.myapplication/drawable/yaristoprated" 
                            3L -> "android.resource://com.example.myapplication/drawable/audia3topratedcars"
                            else -> car.picture
                        }
                        
                        AsyncImage(
                            model = imageResource,
                            placeholder = painterResource(id = R.drawable.car_placeholder),
                            error = painterResource(id = R.drawable.car_placeholder),
                            contentDescription = "${car.brand} ${car.model}",
                            contentScale = ContentScale.FillWidth,
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
                                        text = car.rating.toString(),
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
                                .clickable { favoriteViewModel.toggleFavorite(car.id) }
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
                        text = "${car.brand} ${car.model}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Price
                    Text(
                        text = "${car.rentalPricePerDay}.0 DA / day",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                    // Type
                    FeatureItem(
                        icon = Icons.Outlined.Settings,
                        text = car.type
                    )
                    
                    // Transmission
                    FeatureItem(
                        icon = Icons.Outlined.Speed,
                        text = car.transmission
                    )
                    
                    // Fuel
                    FeatureItem(
                        icon = Icons.Outlined.LocalGasStation,
                        text = car.fuel
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Book button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Book Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF149459),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onCatalogClick: () -> Unit = {},
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
            BottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                isSelected = true,
                onClick = onHomeClick
            )

            BottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                onClick = onCatalogClick
            )

            BottomNavItem(
                iconRes = R.drawable.heart, // Using heart for Favorite
                label = "Favorite",
                onClick = onFavoriteClick
            )

            BottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun BottomNavItem(
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
fun CarListSection(cars: List<com.example.myapplication.data.model.Car>, onCarClick: (String) -> Unit = {}) {
    cars.forEach { car ->
        CarItem(
            car = car,
            onClick = { onCarClick(car.id.toString()) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun Chip(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    color: Color,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear filter",
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun PopularCarItem(
    car: com.example.myapplication.data.model.Car,
    onClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    // Get favorite status from ViewModel
    val favoriteStatusMap by favoriteViewModel.favoriteStatusMap.collectAsState()
    val isFavorite = favoriteStatusMap[car.id] ?: false
    
    // Check favorite status for this car when first displayed
    LaunchedEffect(car.id) {
        favoriteViewModel.checkFavoriteStatus(car.id)
    }
    
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Car Image: use new full-card images
            val popularImage = when(car.id) {
                1L -> "android.resource://com.example.myapplication/drawable/i10mostpopfinal"
                2L -> "android.resource://com.example.myapplication/drawable/yarismostpopfinal"
                3L -> "android.resource://com.example.myapplication/drawable/a3mostpopfinal"
                else -> car.picture
            }
            
            AsyncImage(
                model = popularImage,
                placeholder = painterResource(id = R.drawable.car_placeholder),
                error = painterResource(id = R.drawable.car_placeholder),
                contentDescription = "${car.brand} ${car.model}",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            
            // Semi-transparent overlay at the bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            // Car Info Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Car name
                Text(
                    text = "${car.brand} ${car.model}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Text(
                        text = "${car.rentalPricePerDay}.0 DA / day",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(2.dp))
                        
                        Text(
                            text = car.rating.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Car Type Badge
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = car.type,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            // Favorite Button
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { favoriteViewModel.toggleFavorite(car.id) }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFFF4444) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true,)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

/**
 * Screen that displays the user's favorite cars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {},
    onCarClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMyBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    // Observe favorite cars
    val uiState by favoriteViewModel.uiState.collectAsState()
    val favoriteCars by favoriteViewModel.favoriteCars.collectAsState()
    val context = LocalContext.current
    
    // Load favorite cars when the screen is first displayed
    LaunchedEffect(Unit) {
        favoriteViewModel.loadFavoriteCars()
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
                .padding(bottom = 80.dp) // Add bottom padding for the nav bar
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

                // Title "Favorites" at the center
                Text(
                    text = "Favorites",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Content based on UI state
            when (uiState) {
                is FavoriteUiState.Loading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF149459))
                    }
                }
                is FavoriteUiState.Error -> {
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
                                text = "Error loading favorites",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uiState as FavoriteUiState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { favoriteViewModel.loadFavoriteCars() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF149459)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is FavoriteUiState.Empty -> {
                    // Show empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "No favorites",
                                tint = Color.Gray,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No favorite cars yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add cars to your favorites to see them here",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is FavoriteUiState.Success -> {
                    // Show favorite cars list
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favoriteCars) { car ->
                            CarItem(
                                car = car,
                                onClick = { onCarClick(car.id.toString()) },
                                favoriteViewModel = favoriteViewModel
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Add the bottom navigation bar
        FavoritesBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onMyBookingsClick = onMyBookingsClick,
            onFavoriteClick = { /* Already on Favorites */ },
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun FavoritesBottomNavBar(
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
            FavoritesBottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                onClick = onHomeClick
            )

            FavoritesBottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                onClick = onMyBookingsClick
            )

            FavoritesBottomNavItem(
                iconRes = R.drawable.heart,
                label = "Favorite",
                isSelected = true,
                onClick = onFavoriteClick
            )

            FavoritesBottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun FavoritesBottomNavItem(
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
fun FavoritesScreenPreview() {
    FavoritesScreen()
}