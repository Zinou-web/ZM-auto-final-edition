package com.example.myapplication.ui.viewmodel

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
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for reservation-related functionality
 */
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _reservationState = MutableStateFlow<ReservationUiState>(ReservationUiState.Initial)
    val reservationState: StateFlow<ReservationUiState> = _reservationState
    
    private val _userReservationsState = MutableStateFlow<UserReservationsUiState>(UserReservationsUiState.Initial)
    val userReservationsState: StateFlow<UserReservationsUiState> = _userReservationsState

    /**
     * Make a new reservation
     */
    fun makeReservation(
        carId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        pricePerDay: Double,
        withDriver: Boolean = false
    ) {
        viewModelScope.launch {
            _reservationState.value = ReservationUiState.Loading
            
            // Calculate total price based on number of days
            val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
            val totalPrice = pricePerDay * days * (if (withDriver) 1.5 else 1.0)
            
            reservationRepository.createReservation(
                carId = carId,
                startDate = startDate,
                endDate = endDate,
                totalPrice = totalPrice
            ).collectLatest { result ->
                _reservationState.value = when (result.status) {
                    ApiStatus.SUCCESS -> result.data?.let { 
                        ReservationUiState.Success(it) 
                    } ?: ReservationUiState.Error("Failed to create reservation")
                    ApiStatus.ERROR -> ReservationUiState.Error(result.message ?: "Failed to create reservation")
                    ApiStatus.LOADING -> ReservationUiState.Loading
                }
            }
        }
    }
    
    /**
     * Get user's upcoming reservations
     */
    fun loadUpcomingReservations() {
        viewModelScope.launch {
            _userReservationsState.value = UserReservationsUiState.Loading
            
            reservationRepository.getUpcomingReservations().collectLatest { result ->
                _userReservationsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> UserReservationsUiState.Success(result.data ?: emptyList())
                    ApiStatus.ERROR -> UserReservationsUiState.Error(result.message ?: "Failed to load reservations")
                    ApiStatus.LOADING -> UserReservationsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Get user's past reservations
     */
    fun loadPastReservations() {
        viewModelScope.launch {
            _userReservationsState.value = UserReservationsUiState.Loading
            
            reservationRepository.getPastReservations().collectLatest { result ->
                _userReservationsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> UserReservationsUiState.Success(result.data ?: emptyList())
                    ApiStatus.ERROR -> UserReservationsUiState.Error(result.message ?: "Failed to load reservations")
                    ApiStatus.LOADING -> UserReservationsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Cancel a reservation
     */
    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            _reservationState.value = ReservationUiState.Loading
            
            reservationRepository.cancelReservation(reservationId).collectLatest { result ->
                _reservationState.value = when (result.status) {
                    ApiStatus.SUCCESS -> ReservationUiState.Cancelled
                    ApiStatus.ERROR -> ReservationUiState.Error(result.message ?: "Failed to cancel reservation")
                    ApiStatus.LOADING -> ReservationUiState.Loading
                }
            }
        }
    }
}

/**
 * UI state for reservation actions
 */
sealed class ReservationUiState {
    object Initial : ReservationUiState()
    object Loading : ReservationUiState()
    data class Success(val reservation: Reservation) : ReservationUiState()
    object Cancelled : ReservationUiState()
    data class Error(val message: String) : ReservationUiState()
}

/**
 * UI state for user reservations list
 */
sealed class UserReservationsUiState {
    object Initial : UserReservationsUiState()
    object Loading : UserReservationsUiState()
    data class Success(val reservations: List<Reservation>) : UserReservationsUiState()
    data class Error(val message: String) : UserReservationsUiState()
} 