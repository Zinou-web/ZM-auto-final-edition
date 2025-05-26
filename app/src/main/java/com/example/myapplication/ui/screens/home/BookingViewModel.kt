package com.example.myapplication.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import java.util.Locale

/**
 * ViewModel to hold all booking information that needs to be shared between screens
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    
    // Booking ID
    var reservationId by mutableStateOf<Long>(0L)
    
    // Car booking details
    var carName by mutableStateOf("")
    var carYear by mutableStateOf("")
    var carPrice by mutableStateOf(0.0)
    var carTransmission by mutableStateOf("")
    var carRating by mutableStateOf(0.0f)
    var carType by mutableStateOf<String?>("SUV")
    var seats by mutableStateOf<String?>("4")
    var paymentMethod by mutableStateOf<String?>("Cash")
    
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
    
    // Car ID for reservation creation
    var carId by mutableStateOf(0L)
    
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
        Log.d("BookingViewModel", "updateCarDetails called with:")
        Log.d("BookingViewModel", "  Name: $name, Year: $year, Price: $price, Transmission: $transmission, Rating: $rating")
        Log.d("BookingViewModel", "  RentType: $rentType, PickUp: $pickUp $pickUpT, DropOff: $dropOff $dropOffT, Days: $days")

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
        Log.d("BookingViewModel", "Calculated totalPrice in updateCarDetails: $totalPrice (carPrice: $carPrice, totalDays: $totalDays, driverFees: $driverFees)")
        
        // Generate a temporary reservation ID (in a real app this would come from the backend)
        if (reservationId == 0L) {
            reservationId = System.currentTimeMillis()
        }
    }
    
    // Update the car ID to use for creating a reservation
    fun updateCarId(id: Long) {
        Log.d("BookingViewModel", "updateCarId called with id: $id")
        carId = id
        Log.d("BookingViewModel", "Car ID updated to: $carId")
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
    
    /**
     * Create a reservation with cash payment status
     * This will be called when user selects cash payment
     */
    fun createCashReservation(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("BookingViewModel", "createCashReservation: Attempting with carId: $carId, totalPrice: $totalPrice")
                if (carId <= 0L) {
                    Log.e("BookingViewModel", "createCashReservation: Invalid carId ($carId). Aborting.")
                    onComplete(false)
                    return@launch
                }
                if (totalPrice <= 0.0 && driverOption != "With Driver") { // Allow 0 price if it's only driver fees (though unlikely)
                    Log.e("BookingViewModel", "createCashReservation: Invalid totalPrice ($totalPrice) for carId $carId. Aborting.")
                    onComplete(false)
                    return@launch
                }

                Log.d("BookingViewModel", "Creating cash reservation for car $carId with name $carName")
                Log.d("BookingViewModel", "Pickup date: '$pickUpDate', Dropoff date: '$dropOffDate'")
                
                // Try different date formats since the app may format dates differently in different places
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
                )
                
                // Parse pickup date using multiple formats
                var startDate = LocalDate.now()
                var parsedPickupDate = false
                
                for (formatter in formatters) {
                    try {
                        startDate = LocalDate.parse(pickUpDate, formatter)
                        Log.d("BookingViewModel", "Successfully parsed pickup date with format: ${formatter.toString()}")
                        parsedPickupDate = true
                        break
                    } catch (e: Exception) {
                        // Try next format
                        Log.d("BookingViewModel", "Failed to parse pickup date with format: ${formatter.toString()}")
                    }
                }
                
                if (!parsedPickupDate) {
                    Log.e("BookingViewModel", "Could not parse pickup date with any format, using current date")
                }
                
                // Parse dropoff date using multiple formats
                var endDate = LocalDate.now().plusDays(totalDays.toLong())
                var parsedDropoffDate = false
                
                for (formatter in formatters) {
                    try {
                        endDate = LocalDate.parse(dropOffDate, formatter)
                        Log.d("BookingViewModel", "Successfully parsed dropoff date with format: ${formatter.toString()}")
                        parsedDropoffDate = true
                        break
                    } catch (e: Exception) {
                        // Try next format
                        Log.d("BookingViewModel", "Failed to parse dropoff date with format: ${formatter.toString()}")
                    }
                }
                
                if (!parsedDropoffDate) {
                    Log.e("BookingViewModel", "Could not parse dropoff date with any format, using current date + $totalDays days")
                }
                
                // Make sure end date is after or equal to start date
                if (endDate.isBefore(startDate)) {
                    Log.e("BookingViewModel", "End date is before start date, adjusting to start date + $totalDays days")
                    endDate = startDate.plusDays(totalDays.toLong())
                }
                
                // Ensure we have a valid car ID
                val validCarId = if (carId > 0) carId else 1L
                
                // Log full reservation details before creating
                Log.d("BookingViewModel", "Creating reservation with:")
                Log.d("BookingViewModel", "  - Car ID: $validCarId")
                Log.d("BookingViewModel", "  - Start: $startDate")
                Log.d("BookingViewModel", "  - End: $endDate")
                Log.d("BookingViewModel", "  - Price: $totalPrice")
                
                // Create the reservation in the repository
                reservationRepository.createReservation(
                    carId = validCarId,
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                ).collect { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                // Update the reservation ID with the one from the created reservation
                                updateReservationId(reservation.id)
                                Log.d("BookingViewModel", "Successfully created reservation with ID: ${reservation.id}")
                                
                                // Update payment method to explicitly mark as CASH
                                paymentMethod = "CASH"
                                
                                // Call onComplete with success
                                onComplete(true)
                            } ?: run {
                                Log.e("BookingViewModel", "Created reservation returned null")
                                onComplete(false)
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e("BookingViewModel", "Error creating reservation: ${result.message}")
                            onComplete(false)
                        }
                        ApiStatus.LOADING -> {
                            // Wait for final state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Exception in createCashReservation", e)
                onComplete(false)
            }
        }
    }

    /**
     * Create a reservation with PENDING status for Edahabia payment.
     * This will be called when user selects Edahabia payment before navigating to payment screen.
     * The callback will provide the success status and the new reservation ID if successful.
     */
    fun createEdahabiaPendingReservation(onComplete: (success: Boolean, reservationId: Long?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("BookingViewModel", "createEdahabiaPendingReservation: Attempting with carId: $carId, totalPrice: $totalPrice")
                if (carId <= 0L) {
                    Log.e("BookingViewModel", "createEdahabiaPendingReservation: Invalid carId ($carId). Aborting.")
                    onComplete(false, null)
                    return@launch
                }
                if (totalPrice <= 0.0 && driverOption != "With Driver") {
                    Log.e("BookingViewModel", "createEdahabiaPendingReservation: Invalid totalPrice ($totalPrice) for carId $carId. Aborting.")
                    onComplete(false, null)
                    return@launch
                }

                Log.d("BookingViewModel", "Creating Edahabia pending reservation for car $carId with name $carName")
                Log.d("BookingViewModel", "Pickup date: '$pickUpDate', Dropoff date: '$dropOffDate'")

                val formatters = listOf(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
                )

                var startDate = LocalDate.now()
                var parsedPickupDate = false
                for (formatter in formatters) {
                    try {
                        startDate = LocalDate.parse(pickUpDate, formatter)
                        Log.d("BookingViewModel", "Successfully parsed pickup date for Edahabia with format: ${formatter.toString()}")
                        parsedPickupDate = true
                        break
                    } catch (e: Exception) {
                        Log.d("BookingViewModel", "Failed to parse pickup date for Edahabia with format: ${formatter.toString()}")
                    }
                }
                if (!parsedPickupDate) {
                    Log.e("BookingViewModel", "Could not parse pickup date for Edahabia, using current date")
                }

                var endDate = LocalDate.now().plusDays(totalDays.toLong())
                var parsedDropoffDate = false
                for (formatter in formatters) {
                    try {
                        endDate = LocalDate.parse(dropOffDate, formatter)
                        Log.d("BookingViewModel", "Successfully parsed dropoff date for Edahabia with format: ${formatter.toString()}")
                        parsedDropoffDate = true
                        break
                    } catch (e: Exception) {
                        Log.d("BookingViewModel", "Failed to parse dropoff date for Edahabia with format: ${formatter.toString()}")
                    }
                }
                if (!parsedDropoffDate) {
                    Log.e("BookingViewModel", "Could not parse dropoff date for Edahabia, using current date + $totalDays days")
                }

                if (endDate.isBefore(startDate)) {
                    Log.e("BookingViewModel", "End date for Edahabia is before start date, adjusting to start date + $totalDays days")
                    endDate = startDate.plusDays(totalDays.toLong())
                }

                val validCarId = if (carId > 0) carId else 1L

                Log.d("BookingViewModel", "Creating Edahabia PENDING reservation with:")
                Log.d("BookingViewModel", "  - Car ID: $validCarId")
                Log.d("BookingViewModel", "  - Start: $startDate")
                Log.d("BookingViewModel", "  - End: $endDate")
                Log.d("BookingViewModel", "  - Price: $totalPrice")

                reservationRepository.createReservation(
                    carId = validCarId,
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                    // Status will be PENDING by default in Repository for mock data
                ).collect { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                updateReservationId(reservation.id) // Update internal VM state if needed
                                Log.d("BookingViewModel", "Successfully created PENDING Edahabia reservation with ID: ${reservation.id}")
                                paymentMethod = "Card" // Set payment method
                                onComplete(true, reservation.id)
                            } ?: run {
                                Log.e("BookingViewModel", "Created PENDING Edahabia reservation returned null")
                                onComplete(false, null)
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e("BookingViewModel", "Error creating PENDING Edahabia reservation: ${result.message}")
                            onComplete(false, null)
                        }
                        ApiStatus.LOADING -> {
                            // Wait for final state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Exception in createEdahabiaPendingReservation", e)
                onComplete(false, null)
            }
        }
    }
} 