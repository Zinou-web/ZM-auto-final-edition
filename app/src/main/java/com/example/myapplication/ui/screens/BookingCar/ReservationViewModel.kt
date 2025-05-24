package com.example.myapplication.ui.screens.BookingCar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.data.repository.ReservationRepository
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
    private val reservationRepository: ReservationRepository
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
        viewModelScope.launch {
            try {
                reservationRepository.getUpcomingReservations().collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservations ->
                                _upcomingReservations.value = reservations
                                Log.d(TAG, "Loaded ${reservations.size} upcoming reservations")
                            } ?: run {
                                _upcomingReservations.value = emptyList()
                                Log.d(TAG, "No upcoming reservations found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error loading upcoming reservations: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Loading state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading upcoming reservations", e)
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
        _reservationState.value = ReservationUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.getReservationById(reservationId).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { reservation ->
                                _reservationState.value =
                                    ReservationUiState.SingleReservationSuccess(reservation)
                                Log.d(TAG, "Loaded reservation with ID: ${reservation.id}")
                            } ?: run {
                                _reservationState.value =
                                    ReservationUiState.Error("Reservation not found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            _reservationState.value = ReservationUiState.Error(
                                result.message ?: "Failed to load reservation"
                            )
                            Log.e(TAG, "Error loading reservation: ${result.message}")
                        }
                        ApiStatus.LOADING -> {
                            // Already set loading state above
                        }
                    }
                }
            } catch (e: Exception) {
                _reservationState.value = ReservationUiState.Error("Error: ${e.message}")
                Log.e(TAG, "Exception loading reservation", e)
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
} 