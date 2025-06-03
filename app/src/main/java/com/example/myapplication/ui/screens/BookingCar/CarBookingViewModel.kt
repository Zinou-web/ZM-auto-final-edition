package com.example.myapplication.ui.screens.BookingCar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CarBookingUIState(
    val isLoading: Boolean = false,
    val carId: Long = 0,
    val carName: String = "",
    val carPrice: Double = 0.0,
    val carYear: String = "",
    val carTransmission: String = "Automatic",
    val carRating: Float = 4.5f,
    val carPicture: String? = null,
    val carBrand: String = "",
    val carModel: String = "",
    val error: String? = null,
    val car: Car? = null
)

@HiltViewModel
class CarBookingViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    
    var uiState by mutableStateOf(CarBookingUIState(isLoading = true))
        private set
    
    // State for the booking form
    var rentType by mutableStateOf("Self-Driver")
        private set
    
    var pickUpDate by mutableStateOf("Date")
        private set
    
    var pickUpTime by mutableStateOf("Time") 
        private set
    
    var dropOffDate by mutableStateOf("Date")
        private set
    
    var dropOffTime by mutableStateOf("Time")
        private set
    
    var pickUpCalendar by mutableStateOf(Calendar.getInstance())
        private set
    
    var dropOffCalendar by mutableStateOf(Calendar.getInstance().apply { 
        add(Calendar.DAY_OF_MONTH, 3) // Default rental period is 3 days
    })
        private set
    
    // Load car details based on ID
    fun loadCarDetails(carId: String?) {
        if (carId.isNullOrEmpty()) {
            // If no car ID is provided, we should still mark as not loading
            uiState = uiState.copy(isLoading = false)
            return
        }
        
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            
            try {
                // Get the car details from the repository
                val id = carId.toLongOrNull() ?: 0
                carRepository.getCarById(id).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            val car = result.data
                            if (car != null) {
                                uiState = uiState.copy(
                                    isLoading = false,
                                    carId = car.id,
                                    carName = "${car.brand} ${car.model}",
                                    carBrand = car.brand,
                                    carModel = car.model,
                                    carPrice = car.rentalPricePerDay.toDouble(),
                                    carYear = car.year.toString(),
                                    carTransmission = car.transmission ?: "Automatic",
                                    carRating = car.rating.toFloat(),
                                    carPicture = car.picture,
                                    car = car
                                )
                                // Set the rent type based on car transmission
                                updateRentType(if (car.transmission?.lowercase() == "automatic") "With Driver" else "Self-Driver")
                            } else {
                                uiState = uiState.copy(
                                    isLoading = false,
                                    error = "Car not found"
                                )
                            }
                        }
                        ApiStatus.ERROR -> {
                            uiState = uiState.copy(
                                isLoading = false,
                                error = result.message ?: "Error loading car details"
                            )
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state
                        }
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Failed to load car details: ${e.message}"
                )
            }
        }
    }
    
    // Update booking details
    fun updateRentType(type: String) {
        rentType = type
    }
    
    fun updatePickUpDate(date: String, calendar: Calendar) {
        pickUpDate = date
        pickUpCalendar = calendar
        
        // Ensure drop-off date is after pick-up date
        if (pickUpCalendar.timeInMillis > dropOffCalendar.timeInMillis) {
            // Set drop-off to pick-up + 1 day
            val newDropOffCalendar = pickUpCalendar.clone() as Calendar
            newDropOffCalendar.add(Calendar.DAY_OF_MONTH, 1)
            dropOffCalendar = newDropOffCalendar
        }
    }
    
    fun updatePickUpTime(time: String, calendar: Calendar) {
        pickUpTime = time
        pickUpCalendar = calendar
    }
    
    fun updateDropOffDate(date: String, calendar: Calendar) {
        dropOffDate = date
        dropOffCalendar = calendar
    }
    
    fun updateDropOffTime(time: String, calendar: Calendar) {
        dropOffTime = time
        dropOffCalendar = calendar
    }
    
    // Calculate booking duration in days
    fun calculateDurationInDays(): Int {
        return if (pickUpDate != "Date" && dropOffDate != "Date") {
            val diffInMillis = dropOffCalendar.timeInMillis - pickUpCalendar.timeInMillis
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } else {
            3 // Default duration
        }
    }
    
    // Calculate total price
    fun calculateTotalPrice(): Double {
        val basePricePerDay = uiState.carPrice
        val driverCost = if (rentType == "With Driver") 1000.0 else 0.0
        val days = calculateDurationInDays()
        
        return (basePricePerDay + driverCost) * days
    }
    
    // Check if the form is valid
    fun isFormValid(): Boolean {
        return pickUpDate != "Date" && pickUpTime != "Time" && 
               dropOffDate != "Date" && dropOffTime != "Time"
    }
}

// Helper class for car details
data class CarDetails(
    val name: String, 
    val price: Double, 
    val year: String, 
    val transmission: String, 
    val rating: Float
) {
    // Extension properties to make the code more readable when using CarDetails
    val first get() = name
    val second get() = price
    val third get() = year
    val fourth get() = transmission
    val fifth get() = rating
} 