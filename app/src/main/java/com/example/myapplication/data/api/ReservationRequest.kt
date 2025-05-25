package com.example.myapplication.data.api

import java.time.LocalDate

/**
 * Data class for creating a new reservation
 */
data class ReservationRequest(
    val userId: Long,
    val carId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val withDriver: Boolean = false,
    val totalPrice: Double
)

/**
 * Data class for updating a reservation's status
 */
data class ReservationStatusUpdateRequest(
    val status: String
) 