package com.example.myapplication.data.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/**
 * Data class representing a car in the rental system.
 * This model corresponds to the Car entity in the backend.
 */
data class Car(
    val id: Long = 0,
    val licensePlate: String = "",
    val description: String = "",
    val picture: String = "",
    val brand: String = "",
    val condition: String = "Mint",
    val model: String = "",
    val mileage: Long = 0,
    val type: String = "Hatchback",
    val year: Long = 0,
    val colour: String = "",
    val transmission: String = "Manual",
    val fuel: String = "Petrol",
    val seatingCapacity: Int = 4,
    val rentalPricePerDay: BigDecimal = BigDecimal.ZERO,
    val rentalPricePerHour: BigDecimal? = null,
    val rentalStatus: String = "Available",
    val currentLocation: String = "",
    val lastServiceDate: String? = null,
    val nextServiceDate: String = "",
    val insuranceExpiryDate: String = "",
    val gpsEnabled: Boolean = true,
    val rating: Long = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)