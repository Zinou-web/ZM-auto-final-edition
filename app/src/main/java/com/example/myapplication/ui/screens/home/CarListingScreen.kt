package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Car
import com.example.myapplication.ui.viewmodel.CarViewModel
import com.example.myapplication.ui.viewmodel.CarsUiState
import java.math.BigDecimal
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListingScreen(
    onCarClick: (Long) -> Unit,
    viewModel: CarViewModel = hiltViewModel()
) {
    val carsState by viewModel.carsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadAvailableCars()
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Available Cars") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = carsState) {
                is CarsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CarsUiState.Success -> {
                    if (state.cars.isEmpty()) {
                        Text(
                            text = "No cars available",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        CarList(
                            cars = state.cars,
                            onCarClick = onCarClick
                        )
                    }
                }
                is CarsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadAvailableCars() }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    // Initial state, do nothing
                }
            }
        }
    }
}

@Composable
fun CarList(
    cars: List<Car>,
    onCarClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cars) { car ->
            CarItem(car = car, onClick = { onCarClick(car.id) })
        }
    }
} 