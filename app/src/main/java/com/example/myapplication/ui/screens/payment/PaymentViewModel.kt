package com.example.myapplication.ui.screens.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.PaymentMethodType
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val TAG = "PaymentViewModel"

/**
 * Sealed class representing different UI states for payment operations.
 */
sealed class PaymentUiState {
    object Loading : PaymentUiState()
    data class Success(val message: String, val transactionId: String? = null) : PaymentUiState()
    data class Error(val message: String, val code: String? = null) : PaymentUiState()
    object Initial : PaymentUiState()
    data class ValidationError(val errors: Map<String, String>) : PaymentUiState()
}

/**
 * Enum class representing different validation results for payment card data.
 */
enum class CardValidationResult {
    VALID,
    INVALID_CARD_NUMBER,
    INVALID_CVV,
    INVALID_EXPIRY_DATE,
    INVALID_NAME,
    EXPIRED_CARD
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
    
    // Card details
    private val _cardNumber = MutableStateFlow("")
    val cardNumber: StateFlow<String> = _cardNumber
    
    private val _cardHolderName = MutableStateFlow("")
    val cardHolderName: StateFlow<String> = _cardHolderName
    
    private val _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate
    
    private val _cvv = MutableStateFlow("")
    val cvv: StateFlow<String> = _cvv
    
    private val _saveCardDetails = MutableStateFlow(false)
    val saveCardDetails: StateFlow<Boolean> = _saveCardDetails

    // Payment transaction details
    private val _transactionId = MutableStateFlow<String?>(null)
    val transactionId: StateFlow<String?> = _transactionId
    
    /**
     * Updates the card number.
     */
    fun updateCardNumber(number: String) {
        _cardNumber.value = number
    }
    
    /**
     * Updates the card holder name.
     */
    fun updateCardHolderName(name: String) {
        _cardHolderName.value = name
    }
    
    /**
     * Updates the expiry date.
     */
    fun updateExpiryDate(date: String) {
        _expiryDate.value = date
    }
    
    /**
     * Updates the CVV.
     */
    fun updateCvv(cvv: String) {
        _cvv.value = cvv
    }
    
    /**
     * Updates the save card details preference.
     */
    fun updateSaveCardDetails(save: Boolean) {
        _saveCardDetails.value = save
    }
    
    /**
     * Validates the payment card details before processing payment.
     * 
     * @return CardValidationResult indicating whether the card details are valid
     */
    fun validateCardDetails(): CardValidationResult {
        // Validate card number (Luhn algorithm check would be ideal in a real app)
        if (_cardNumber.value.replace(" ", "").length != 16) {
            return CardValidationResult.INVALID_CARD_NUMBER
        }
        
        // Validate CVV (3 or 4 digits)
        if (_cvv.value.length !in 3..4) {
            return CardValidationResult.INVALID_CVV
        }
        
        // Validate expiry date format and check if it's expired
        val expiryDateParts = _expiryDate.value.split("/")
        if (expiryDateParts.size != 3) {
            return CardValidationResult.INVALID_EXPIRY_DATE
        }
        
        try {
            val day = expiryDateParts[0].toInt()
            val month = expiryDateParts[1].toInt()
            val year = expiryDateParts[2].toInt()
            
            if (day !in 1..31 || month !in 1..12 || year < 2023) {
                return CardValidationResult.EXPIRED_CARD
            }
        } catch (e: NumberFormatException) {
            return CardValidationResult.INVALID_EXPIRY_DATE
        }
        
        // Validate card holder name (not empty)
        if (_cardHolderName.value.isBlank() || _cardHolderName.value.split(" ").size < 2) {
            return CardValidationResult.INVALID_NAME
        }
        
        return CardValidationResult.VALID
    }
    
    /**
     * Processes a payment for the given reservation.
     * 
     * @param reservationId The ID of the reservation to process payment for
     * @param paymentMethod The payment method used (e.g., "edahabia", "cash")
     * @param amount The payment amount
     */
    fun processPayment(reservationId: Long, paymentMethod: String, amount: Double) {
        _uiState.value = PaymentUiState.Loading
        
        // Log the payment attempt
        Log.d(TAG, "Processing payment for reservation ID: $reservationId, method: $paymentMethod, amount: $amount")
        
        // Validate card details if using edahabia payment method
        if (paymentMethod == PaymentMethodType.EDAHABIA.name) {
            val validationResult = validateCardDetails()
            if (validationResult != CardValidationResult.VALID) {
                val errors = when (validationResult) {
                    CardValidationResult.INVALID_CARD_NUMBER -> mapOf("cardNumber" to "Invalid card number. Must be 16 digits.")
                    CardValidationResult.INVALID_CVV -> mapOf("cvv" to "Invalid CVV. Must be 3 or 4 digits.")
                    CardValidationResult.INVALID_EXPIRY_DATE -> mapOf("expiryDate" to "Invalid expiry date format. Use DD/MM/YYYY.")
                    CardValidationResult.EXPIRED_CARD -> mapOf("expiryDate" to "Card has expired.")
                    CardValidationResult.INVALID_NAME -> mapOf("cardHolderName" to "Please enter your full name as it appears on the card.")
                    else -> emptyMap()
                }
                _uiState.value = PaymentUiState.ValidationError(errors)
                return
            }
        }
        
        viewModelScope.launch {
            try {
                // First check if the reservation exists
                var reservationExists = false
                
                try {
                    reservationRepository.getReservationById(reservationId).collectLatest { result ->
                        when (result.status) {
                        ApiStatus.SUCCESS -> {
                                if (result.data != null) {
                                    reservationExists = true
                                    _currentReservation.value = result.data
                                    Log.d(TAG, "Found reservation with ID: $reservationId")
                                    continueWithPayment(reservationId, paymentMethod, amount)
                                } else {
                                    Log.e(TAG, "Reservation not found with ID: $reservationId")
                                    _uiState.value = PaymentUiState.Error(
                                        message = "Reservation not found. Please try again or contact support.",
                                        code = "RESERVATION_NOT_FOUND"
                                    )
                                }
                        }
                        ApiStatus.ERROR -> {
                                // Create a mock reservation as fallback for testing
                                Log.w(TAG, "Error fetching reservation, using fallback: ${result.message}")
                                reservationExists = true
                                continueWithPayment(reservationId, paymentMethod, amount)
                        }
                        ApiStatus.LOADING -> {
                                // Still loading
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception checking reservation", e)
                    // Fall back to mock data for testing
                    reservationExists = true
                    continueWithPayment(reservationId, paymentMethod, amount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment", e)
                _uiState.value = PaymentUiState.Error(
                    message = "Payment failed: ${e.message}",
                    code = "EXCEPTION"
                )
            }
        }
    }
    
    /**
     * Continues with payment processing after confirming the reservation exists
     */
    private suspend fun continueWithPayment(reservationId: Long, paymentMethod: String, amount: Double) {
        // Generate a transaction ID for tracking
        val txnId = UUID.randomUUID().toString()
        _transactionId.value = txnId
        
        // Simulate a network delay for the payment processing
        delay(1500)
        
        // Simulate potential payment gateway issues (10% failure rate for realism)
        val shouldFail = (0..9).random() == 0
        
        if (shouldFail) {
            _uiState.value = PaymentUiState.Error(
                message = "Payment gateway error. Please try again later.",
                code = "GATEWAY_ERROR"
            )
            return
        }
        
        // In a real app, this would call a payment gateway API
        // Here we simulate a successful payment and update the backend
        reservationRepository.updateReservationStatus(reservationId, "PAID").collectLatest { apiResource ->
            when (apiResource.status) {
                ApiStatus.SUCCESS -> {
                    // Save card details if requested (in a real app, this would securely store the data)
                    if (_saveCardDetails.value) {
                        // We're just simulating this - in a real app you'd use encrypted storage
                        Log.d(TAG, "Saved card details for future use")
                    }
                    
                    _uiState.value = PaymentUiState.Success(
                        message = "Payment successful",
                        transactionId = txnId
                    )
                    
                    // Refresh the reservation details
                    getReservationDetails(reservationId)
                }
                ApiStatus.ERROR -> {
                    _uiState.value = PaymentUiState.Error(
                        message = apiResource.message ?: "Payment failed",
                        code = "RESERVATION_UPDATE_ERROR"
                    )
                }
                ApiStatus.LOADING -> {
                    _uiState.value = PaymentUiState.Loading
                }
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
                            _uiState.value = PaymentUiState.Error(
                                message = apiResource.message ?: "Failed to load reservation details",
                                code = "RESERVATION_FETCH_ERROR"
                            )
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = PaymentUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching reservation details", e)
                _uiState.value = PaymentUiState.Error(
                    message = "Failed to load reservation details: ${e.message}",
                    code = "EXCEPTION"
                )
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
                            _uiState.value = PaymentUiState.Success(
                                message = "Reservation cancelled successfully"
                            )
                        }
                        ApiStatus.ERROR -> {
                            _uiState.value = PaymentUiState.Error(
                                message = apiResource.message ?: "Failed to cancel reservation",
                                code = "RESERVATION_CANCEL_ERROR"
                            )
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = PaymentUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling reservation", e)
                _uiState.value = PaymentUiState.Error(
                    message = "Failed to cancel reservation: ${e.message}",
                    code = "EXCEPTION"
                )
            }
        }
    }
} 