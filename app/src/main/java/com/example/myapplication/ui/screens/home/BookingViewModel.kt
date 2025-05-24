package com.example.myapplication.ui.screens.home

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel to hold all booking information that needs to be shared between screens
 */
class BookingViewModel : ViewModel() {
    
    // Booking ID
    var reservationId by mutableStateOf<Long>(0L)
    
    // Car booking details
    var carName by mutableStateOf("")
    var carYear by mutableStateOf("")
    var carPrice by mutableStateOf(0.0)
    var carTransmission by mutableStateOf("")
    var carRating by mutableStateOf(0.0f)
    
    // Booking dates and times
    var pickUpDate by mutableStateOf("")
    var pickUpTime by mutableStateOf("")
    var dropOffDate by mutableStateOf("")
    var dropOffTime by mutableStateOf("")
    
    // Driver option
    var driverOption by mutableStateOf("Self-Driver")
    
    // Booking duration and cost
    var totalDays by mutableStateOf(0)
    var driverFees by mutableStateOf(0.0)
    var totalPrice by mutableStateOf(0.0)
    
    // Renter information
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var email by mutableStateOf("")
    var wilaya by mutableStateOf("")
    var driverLicenseUri by mutableStateOf<Uri?>(null)
    var driverLicenseFileName by mutableStateOf("")
    
    // Update car details from car booking screen
    fun updateCarDetails(
        name: String,
        year: String,
        price: Double,
        transmission: String,
        rating: Float,
        rentType: String,
        pickUp: String,
        pickUpT: String,
        dropOff: String,
        dropOffT: String,
        days: Int
    ) {
        carName = name
        carYear = year
        carPrice = price
        carTransmission = transmission
        carRating = rating
        driverOption = rentType
        pickUpDate = pickUp
        pickUpTime = pickUpT
        dropOffDate = dropOff
        dropOffTime = dropOffT
        totalDays = days
        
        // Calculate costs
        driverFees = if (driverOption == "With Driver") 1000.0 * totalDays else 0.0
        totalPrice = (carPrice * totalDays) + driverFees
        
        // Generate a temporary reservation ID (in a real app this would come from the backend)
        if (reservationId == 0L) {
            reservationId = System.currentTimeMillis()
        }
    }
    
    // Update renter information
    fun updateRenterInfo(
        first: String,
        last: String,
        phone: String,
        emailAddress: String,
        selectedWilaya: String,
        licenseUri: Uri?,
        licenseFileName: String
    ) {
        firstName = first
        lastName = last
        phoneNumber = phone
        email = emailAddress
        wilaya = selectedWilaya
        driverLicenseUri = licenseUri
        driverLicenseFileName = licenseFileName
    }
    
    // Update reservation ID when it's created
    fun updateReservationId(id: Long) {
        reservationId = id
    }
    
    // Car ID for reservation creation
    var carId by mutableStateOf(0L)
} 