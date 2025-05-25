package com.example.myapplication.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.Car
import com.example.myapplication.ui.viewmodel.CarDetailsUiState
import com.example.myapplication.ui.viewmodel.CarViewModel
import com.example.myapplication.ui.viewmodel.ReservationUiState
import com.example.myapplication.ui.viewmodel.ReservationViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    carId: Long,
    onReservationComplete: () -> Unit,
    onBackClick: () -> Unit,
    carViewModel: CarViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel()
) {
    val carDetailsState by carViewModel.carDetailsState.collectAsStateWithLifecycle()
    val reservationState by reservationViewModel.reservationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // State for reservation details
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var withDriver by remember { mutableStateOf(false) }
    
    // Load car details when the screen is first displayed
    LaunchedEffect(carId) {
        carViewModel.loadCarDetails(carId)
    }
    
    // Handle reservation state changes
    LaunchedEffect(reservationState) {
        when (reservationState) {
            is ReservationUiState.Success -> {
                onReservationComplete()
            }
            is ReservationUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (reservationState as ReservationUiState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {
                // Do nothing for other states
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Make Reservation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Replace with your back icon
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val carState = carDetailsState) {
                is CarDetailsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CarDetailsUiState.Success -> {
                    ReservationForm(
                        car = carState.car,
                        startDate = startDate,
                        endDate = endDate,
                        withDriver = withDriver,
                        onStartDateChange = { startDate = it },
                        onEndDateChange = { endDate = it },
                        onWithDriverChange = { withDriver = it },
                        onSubmit = {
                            reservationViewModel.makeReservation(
                                carId = carId,
                                startDate = startDate,
                                endDate = endDate,
                                pricePerDay = carState.car.rentalPricePerDay.toDouble(),
                                withDriver = withDriver
                            )
                        },
                        isLoading = reservationState is ReservationUiState.Loading
                    )
                }
                is CarDetailsUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = carState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { carViewModel.loadCarDetails(carId) }) {
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
fun ReservationForm(
    car: Car,
    startDate: LocalDate,
    endDate: LocalDate,
    withDriver: Boolean,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onWithDriverChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
    val basePrice = car.rentalPricePerDay.toDouble() * days
    val driverFee = if (withDriver) basePrice * 0.5 else 0.0
    val totalPrice = basePrice + driverFee
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Car info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${car.brand} ${car.model} (${car.year})",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Base price: $${car.rentalPricePerDay}/day",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // Date selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reservation Dates",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start Date")
                        Text(
                            text = startDate.format(formatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // In a real app, add date picker here
                    }
                    
                    Column {
                        Text("End Date")
                        Text(
                            text = endDate.format(formatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        // In a real app, add date picker here
                    }
                }
                
                Text("Duration: $days days")
            }
        }
        
        // Driver option
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Include Driver",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Additional 50% of base price",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Switch(
                    checked = withDriver,
                    onCheckedChange = onWithDriverChange
                )
            }
        }
        
        // Price summary
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Price Summary",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Base Price ($days days)")
                    Text("$${String.format("%.2f", basePrice)}")
                }
                
                if (withDriver) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Driver Fee")
                        Text("$${String.format("%.2f", driverFee)}")
                    }
                }
                
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && days > 0
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Confirm Reservation")
            }
        }
    }
} 