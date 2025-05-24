package com.example.myapplication.ui.screens.payment

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
import javax.inject.Inject

private const val TAG = "PaymentViewModel"

/**
 * Sealed class representing different UI states for payment operations.
 */
sealed class PaymentUiState {
    object Loading : PaymentUiState()
    data class Success(val message: String) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
    object Initial : PaymentUiState()
}

/**
 * ViewModel for handling payment operations related to reservations.
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Initial)
    val uiState: StateFlow<PaymentUiState> = _uiState
    
    private val _currentReservation = MutableStateFlow<Reservation?>(null)
    val currentReservation: StateFlow<Reservation?> = _currentReservation
    
    /**
     * Processes a payment for the given reservation.
     * 
     * @param reservationId The ID of the reservation to process payment for
     * @param paymentMethod The payment method used (e.g., "credit_card", "edahabia")
     * @param amount The payment amount
     */
    fun processPayment(reservationId: Long, paymentMethod: String, amount: Double) {
        _uiState.value = PaymentUiState.Loading
        
        viewModelScope.launch {
            try {
                // In a real app, this would call an API to process the payment
                // For now, we'll simulate a successful payment
                reservationRepository.updateReservationStatus(reservationId, "PAID").collectLatest { apiResource ->
                    when (apiResource.status) {
                        ApiStatus.SUCCESS -> {
                            _uiState.value = PaymentUiState.Success("Payment successful")
                            // Refresh the reservation details
                            getReservationDetails(reservationId)
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = PaymentUiState.Error(apiResource.message ?: "Payment failed")
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = PaymentUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment", e)
                _uiState.value = PaymentUiState.Error("Payment failed: ${e.message}")
            }
        }
    }
    
    /**
     * Fetches the details of a specific reservation.
     * 
     * @param reservationId The ID of the reservation to fetch
     */
    fun getReservationDetails(reservationId: Long) {
        _uiState.value = PaymentUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.getReservationById(reservationId).collectLatest { apiResource ->
                    when (apiResource.status) {
                        ApiStatus.SUCCESS -> {
                            _currentReservation.value = apiResource.data
                            _uiState.value = PaymentUiState.Initial
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = PaymentUiState.Error(apiResource.message ?: "Failed to load reservation details")
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = PaymentUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching reservation details", e)
                _uiState.value = PaymentUiState.Error("Failed to load reservation details: ${e.message}")
            }
        }
    }
    
    /**
     * Cancels a reservation.
     * 
     * @param reservationId The ID of the reservation to cancel
     */
    fun cancelReservation(reservationId: Long) {
        _uiState.value = PaymentUiState.Loading
        
        viewModelScope.launch {
            try {
                reservationRepository.updateReservationStatus(reservationId, "CANCELLED").collectLatest { apiResource ->
                    when (apiResource.status) {
                        ApiStatus.SUCCESS -> {
                            _uiState.value = PaymentUiState.Success("Reservation cancelled successfully")
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = PaymentUiState.Error(apiResource.message ?: "Failed to cancel reservation")
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = PaymentUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling reservation", e)
                _uiState.value = PaymentUiState.Error("Failed to cancel reservation: ${e.message}")
            }
        }
    }
} 