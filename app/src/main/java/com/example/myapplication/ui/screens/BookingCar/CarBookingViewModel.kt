package com.example.myapplication.ui.screens.BookingCar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

data class CarBookingUIState(
    val isLoading: Boolean = false,
    val carName: String = "",
    val carPrice: Double = 0.0,
    val carYear: String = "",
    val carTransmission: String = "Automatic",
    val carRating: Float = 4.5f,
    val error: String? = null
)

class CarBookingViewModel : ViewModel() {
    
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
            // If no car ID is provided, use default values for testing
            uiState = CarBookingUIState(
                isLoading = false,
                carName = "Toyota Corolla",
                carPrice = 5000.0,
                carYear = "2023",
                carTransmission = "Automatic",
                carRating = 4.5f
            )
            return
        }
        
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            
            try {
                // In a real app, we would fetch car details from a repository
                // For now, simulate fetching based on carId
                val carDetails = fetchCarDetails(carId)
                uiState = uiState.copy(
                    isLoading = false,
                    carName = carDetails.first,
                    carPrice = carDetails.second,
                    carYear = carDetails.third,
                    carTransmission = carDetails.fourth,
                    carRating = carDetails.fifth
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Failed to load car details: ${e.message}"
                )
            }
        }
    }
    
    // Simulate fetching car details - in a real app, this would come from a repository
    private fun fetchCarDetails(carId: String): CarDetails {
        // Simulate network delay
        Thread.sleep(500)
        
        // Return mock data based on carId
        return when (carId) {
            "1" -> CarDetails("Toyota Corolla", 5000.0, "2023", "Automatic", 4.5f)
            "2" -> CarDetails("Honda Civic", 4800.0, "2022", "Automatic", 4.3f)
            "3" -> CarDetails("Hyundai Elantra", 4500.0, "2023", "Manual", 4.2f)
            "4" -> CarDetails("Kia Forte", 4200.0, "2022", "Automatic", 4.0f)
            "5" -> CarDetails("Mazda 3", 5200.0, "2023", "Automatic", 4.7f)
            else -> CarDetails("Car #$carId", 5000.0, "2023", "Automatic", 4.5f)
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