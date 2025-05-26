package com.example.myapplication.ui.screens.BookingCar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.data.repository.ReservationRepository
import com.example.myapplication.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "ReservationViewModel"

// Interface for CarBookingScreen to interact with ReservationViewModel
interface ReservationScreenActions {
    val reservationState: StateFlow<ReservationUiState>
    fun createReservation(
        carId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        totalPrice: Double
    )
    // Add other states/methods if CarBookingScreen uses them
}

/**
 * Sealed class representing different UI states for reservation data.
 */
sealed class ReservationUiState {
    object Loading : ReservationUiState()
    data class Success(val reservations: List<Reservation>) : ReservationUiState()
    data class SingleReservationSuccess(val reservation: Reservation) : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
    object Idle : ReservationUiState()
}

/**
 * ViewModel for reservation-related operations.
 */
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val carRepository: CarRepository
) : ViewModel(), ReservationScreenActions {
    
    // Reservation list UI state
    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    val uiState: StateFlow<ReservationUiState> = _uiState
    
    // Single reservation UI state (for creation/update operations)
    private val _reservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Idle)
    override val reservationState: StateFlow<ReservationUiState> = _reservationState
    
    // Upcoming reservations
    private val _upcomingReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val upcomingReservations: StateFlow<List<Reservation>> = _upcomingReservations
    
    // Past reservations
    private val _pastReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val pastReservations: StateFlow<List<Reservation>> = _pastReservations
    
    // Currently selected reservation for rebooking or modification
    private val _selectedReservation = MutableStateFlow<Reservation?>(null)
    val selectedReservation: StateFlow<Reservation?> = _selectedReservation
    
    init {
        Log.d(TAG, "ReservationViewModel init block ENTERED (no fatal error).")
        // (no network calls here)
    }
    
    /**
     * Load all reservations for the current user.
     */
    fun loadUserReservations() {
        _uiState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.getUserReservations().collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservations ->
                                _uiState.value = ReservationUiState.Success(reservations)
                                Log.d(TAG, "Loaded ${reservations.size} user reservations")
                            } ?: run {
                                _uiState.value = ReservationUiState.Error("No reservations found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = ReservationUiState.Error(
                                result.message ?: "Failed to load reservations"
                            )
                            Log.e(TAG, "Error loading reservations: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception loading reservations", e)
            }
        }
    }
    
    /**
     * Load upcoming reservations for the current user.
     */
    fun loadUpcomingReservations() {
        _uiState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading upcoming reservations...")
                reservationRepository.getUpcomingReservations().collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            val reservations = result.data ?: emptyList()
                            _upcomingReservations.value = reservations
                            
                            // Update the main UI state as well
                            _uiState.value = ReservationUiState.Success(reservations)
                            
                            Log.d(TAG, "Successfully loaded ${reservations.size} upcoming reservations")
                            
                            // Debug log of each reservation
                            reservations.forEach { reservation ->
                                Log.d(TAG, "Upcoming reservation: ${reservation.id}, Car: ${reservation.car?.brand ?: "Unknown"} ${reservation.car?.model ?: ""}, Status: ${reservation.status}")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error loading upcoming reservations: ${result.message}")
                            _upcomingReservations.value = emptyList()
                            _uiState.value = ReservationUiState.Error(result.message ?: "Failed to load reservations")
                        }
                        ApiStatus.LOADING -> {
                            // Keep showing loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading upcoming reservations", e)
                _upcomingReservations.value = emptyList()
                _uiState.value = ReservationUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Load past reservations for the current user.
     */
    fun loadPastReservations() {
        viewModelScope.launch {
            try {
                reservationRepository.getPastReservations().collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservations ->
                                _pastReservations.value = reservations
                                Log.d(TAG, "Loaded ${reservations.size} past reservations")
                            } ?: run {
                                _pastReservations.value = emptyList()
                                Log.d(TAG, "No past reservations found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error loading past reservations: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading past reservations", e)
            }
        }
    }
    
    /**
     * Create a new reservation.
     */
    override fun createReservation(
        carId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        totalPrice: Double
    ) {
        _reservationState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.createReservation(
                    carId = carId,
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                ).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                _reservationState.value =
                                    ReservationUiState.SingleReservationSuccess(reservation)
                                Log.d(TAG, "Created reservation with ID: ${reservation.id}")
                                
                                // Refresh reservation lists
                                loadUpcomingReservations()
                                loadUserReservations()
                            } ?: run {
                                _reservationState.value =
                                    ReservationUiState.Error("Failed to create reservation")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to create reservation"
                            )
                            Log.e(TAG, "Error creating reservation: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception creating reservation", e)
            }
        }
    }
    
    /**
     * Cancel a reservation.
     */
    fun cancelReservation(reservationId: Long) {
        _reservationState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.cancelReservation(reservationId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            if (result.data == true) {
                                Log.d(TAG, "Cancelled reservation with ID: $reservationId")
                                
                                // Refresh reservation lists
                                loadUpcomingReservations()
                                loadUserReservations()
                                loadPastReservations()
                                
                                _reservationState.value = ReservationUiState.Idle
                            } else {
                                _reservationState.value =
                                    ReservationUiState.Error("Failed to cancel reservation")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to cancel reservation"
                            )
                            Log.e(TAG, "Error cancelling reservation: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception cancelling reservation", e)
            }
        }
    }
    
    /**
     * Get reservation details by ID.
     */
    fun getReservationById(reservationId: Long) {
        Log.d(TAG, "Getting reservation by ID: $reservationId")
        _reservationState.value = ReservationUiState.Loading
        
        if (reservationId <= 0) {
            Log.e(TAG, "Invalid reservation ID: $reservationId")
            _reservationState.value = ReservationUiState.Error("Invalid reservation ID: $reservationId")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching reservation with ID: $reservationId")
                
                reservationRepository.getReservationById(reservationId).collect { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            val reservation = result.data
                            
                            if (reservation != null) {
                                Log.d(TAG, "Successfully loaded reservation ${reservation.id}")
                                
                                // If car is null, try to get car details separately
                                if (reservation.car == null && reservation.carId > 0) {
                                    Log.d(TAG, "Car data missing, fetching car ${reservation.carId}")
                                    
                                    try {
                                        // Fetch car details and update reservation
                                        carRepository.getCarById(reservation.carId).collect { carResult ->
                                            when (carResult.status) {
                                                ApiStatus.SUCCESS -> {
                                                    val carData = carResult.data
                                                    if (carData != null) {
                                                        Log.d(TAG, "Got car data: ${carData.brand} ${carData.model}")
                                                        // Create updated reservation with car data
                                                        val updatedReservation = reservation.copy(car = carData)
                                                        _reservationState.value = ReservationUiState.SingleReservationSuccess(updatedReservation)
                                                    } else {
                                                        // Still show reservation even if car details couldn't be loaded
                                                        _reservationState.value = ReservationUiState.SingleReservationSuccess(reservation)
                                                    }
                                                }
                                                ApiStatus.ERROR -> {
                                                    Log.e(TAG, "Failed to get car: ${carResult.message}")
                                                    // Still show reservation even if car details couldn't be loaded
                                                    _reservationState.value = ReservationUiState.SingleReservationSuccess(reservation)
                                                }
                                                ApiStatus.LOADING -> {
                                                    // Wait for car data
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error fetching car details", e)
                                        // Show reservation without car details if fetching failed
                                        _reservationState.value = ReservationUiState.SingleReservationSuccess(reservation)
                                    }
                                } else {
                                    // Car data already included in reservation
                                    _reservationState.value = ReservationUiState.SingleReservationSuccess(reservation)
                                }
                            } else {
                                Log.e(TAG, "Reservation data is null")
                                _reservationState.value = ReservationUiState.Error("Reservation not found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error getting reservation: ${result.message}")
                            _reservationState.value = ReservationUiState.Error(result.message ?: "Failed to load reservation")
                        }
                        ApiStatus.LOADING -> {
                            _reservationState.value = ReservationUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception getting reservation", e)
                _reservationState.value = ReservationUiState.Error("An error occurred: ${e.message}")
            }
        }
    }
    
    /**
     * Update an existing reservation.
     */
    fun updateReservation(
        reservationId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        totalPrice: Double
    ) {
        _reservationState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.updateReservation(
                    reservationId = reservationId,
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                ).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                _reservationState.value =
                                    ReservationUiState.SingleReservationSuccess(reservation)
                                Log.d(TAG, "Updated reservation with ID: ${reservation.id}")
                                
                                // Refresh reservation lists
                                loadUpcomingReservations()
                                loadUserReservations()
                            } ?: run {
                                _reservationState.value =
                                    ReservationUiState.Error("Failed to update reservation")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to update reservation"
                            )
                            Log.e(TAG, "Error updating reservation: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception updating reservation", e)
            }
        }
    }
    
    /**
     * Set a reservation for rebooking or modification.
     */
    fun setSelectedReservation(reservation: Reservation) {
        _selectedReservation.value = reservation
        Log.d(TAG, "Selected reservation with ID: ${reservation.id} for rebooking/modification")
    }
    
    /**
     * Clear the selected reservation.
     */
    fun clearSelectedReservation() {
        _selectedReservation.value = null
        Log.d(TAG, "Cleared selected reservation")
    }
    
    /**
     * Rebook from a past reservation.
     * This creates a new reservation based on the details of a past one,
     * but with updated dates.
     */
    fun rebookFromPastReservation(
        originalReservationId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        // First get the original reservation to use its details
        viewModelScope.launch {
            try {
                reservationRepository.getReservationById(originalReservationId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { originalReservation ->
                                // Calculate new price based on the same daily rate
                                val daysInNewBooking = endDate.toEpochDay() - startDate.toEpochDay() + 1
                                val originalDays = originalReservation.endDate.toEpochDay() - 
                                                  originalReservation.startDate.toEpochDay() + 1
                                val dailyRate = originalReservation.totalPrice / originalDays
                                val newTotalPrice = dailyRate * daysInNewBooking
                                
                                // Create the new reservation
                                createReservation(
                                    carId = originalReservation.carId,
                                    startDate = startDate,
                                    endDate = endDate,
                                    totalPrice = newTotalPrice
                                )
                                Log.d(TAG, "Rebooked from reservation ID: $originalReservationId")
                            } ?: run {
                                _reservationState.value =
                                    ReservationUiState.Error("Original reservation not found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to load original reservation"
                            )
                            Log.e(TAG, "Error loading original reservation: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Handle loading state
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception rebooking reservation", e)
            }
        }
    }
    
    /**
     * Update reservation status.
     */
    fun updateReservationStatus(reservationId: Long, status: String) {
        _reservationState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.updateReservationStatus(reservationId, status).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                _reservationState.value =
                                    ReservationUiState.SingleReservationSuccess(reservation)
                                Log.d(TAG, "Updated status for reservation with ID: ${reservation.id} to $status")
                                
                                // Refresh reservation lists
                                loadUpcomingReservations()
                                loadPastReservations()
                                loadUserReservations()
                            } ?: run {
                                _reservationState.value =
                                    ReservationUiState.Error("Failed to update reservation status")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to update reservation status"
                            )
                            Log.e(TAG, "Error updating reservation status: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception updating reservation status", e)
            }
        }
    }
    
    /**
     * Force refresh all reservations data.
     * Useful after creating new reservations to make sure they appear in lists.
     */
    fun refreshAllReservations() {
        Log.d(TAG, "Forcing refresh of all reservations")
        
        viewModelScope.launch {
            try {
                // Clear cached data first
                _upcomingReservations.value = emptyList()
                _pastReservations.value = emptyList()
                
                // Then load fresh data
                loadUpcomingReservations()
                loadUserReservations()
                loadPastReservations()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing reservations", e)
            }
        }
    }
} 