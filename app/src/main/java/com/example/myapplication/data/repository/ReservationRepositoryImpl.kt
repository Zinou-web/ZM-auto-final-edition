package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.ReservationRequest
import com.example.myapplication.data.api.ReservationStatusUpdateRequest
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.data.preference.AuthPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the ReservationRepository interface.
 */
@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager
) : ReservationRepository {

    // Local cache for reservations when API is not available
    private val cachedReservations = mutableListOf<Reservation>()
    private var nextReservationId = 1000L
    private val useMockData = false

    init {
        // Add some sample reservations for testing
        val sampleCar = Car(
            id = 1L,
            brand = "Toyota",
            model = "Corolla",
            year = 2023,
            transmission = "Automatic",
            rentalPricePerDay = java.math.BigDecimal(5000),
            rating = 4
        )
        
        // Add a sample upcoming reservation
        cachedReservations.add(
            Reservation(
                id = nextReservationId++,
                userId = 1L,
                carId = 1L,
                car = sampleCar,
                startDate = LocalDate.now().plusDays(2),
                endDate = LocalDate.now().plusDays(5),
                totalPrice = 15000.0,
                status = "CONFIRMED",
                paymentStatus = "PAID",
                createdAt = LocalDateTime.now()
            )
        )
        
        // Add a sample completed reservation
        cachedReservations.add(
            Reservation(
                id = nextReservationId++,
                userId = 1L,
                carId = 2L,
                car = sampleCar.copy(id = 2L, brand = "Honda", model = "Civic"),
                startDate = LocalDate.now().minusDays(10),
                endDate = LocalDate.now().minusDays(5),
                totalPrice = 25000.0,
                status = "COMPLETED",
                paymentStatus = "PAID",
                createdAt = LocalDateTime.now().minusDays(15)
            )
        )
    }

    /**
     * Helper function to get the auth header
     */
    private fun getAuthHeader(): String {
        return "Bearer ${authPreferenceManager.getAuthToken()}"
    }
    
    /**
     * Helper function to get the current user ID
     */
    private fun getCurrentUserId(): Long {
        return authPreferenceManager.getUserId()?.toLong() ?: 0
    }

    override fun getAllReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val reservations = apiService.getAllReservations(token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting all reservations: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get reservations"))
        }
    }

    override fun getReservationById(id: Long): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(500)
                emit(ApiResource(status = ApiStatus.ERROR, message = "Reservation not found"))
            } else {
                val token = getAuthHeader()
                val reservation = apiService.getReservationById(id, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting reservation $id: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get reservation"))
        }
    }

    override fun getUserReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val reservations = apiService.getReservationsByUserId(userId, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting user reservations: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get user reservations"))
        }
    }

    override fun getReservationsByCarId(carId: Long): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val reservations = apiService.getReservationsByCarId(carId, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting reservations for car $carId: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get reservations"))
        }
    }

    override fun getReservationsByStatus(status: String): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val reservations = apiService.getReservationsByStatus(status, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting reservations with status $status: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get reservations"))
        }
    }

    override fun createReservation(
        carId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        totalPrice: Double
    ): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1500)
                val mockReservation = Reservation(
                    id = 1,
                    userId = getCurrentUserId(),
                    carId = carId,
                    startDate = startDate,
                    endDate = endDate,
                    status = "PENDING",
                    totalPrice = totalPrice
                )
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockReservation))
            } else {
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val request = ReservationRequest(
                    userId = userId,
                    carId = carId,
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                )
                val reservation = apiService.createReservation(request, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error creating reservation: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to create reservation"))
        }
    }

    override fun updateReservation(
        reservationId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        totalPrice: Double
    ): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                val mockReservation = Reservation(
                    id = reservationId,
                    userId = getCurrentUserId(),
                    carId = 1,
                    startDate = startDate,
                    endDate = endDate,
                    status = "PENDING",
                    totalPrice = totalPrice
                )
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockReservation))
            } else {
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val request = ReservationRequest(
                    userId = userId,
                    carId = 0, // This will be ignored by the backend for updates
                    startDate = startDate,
                    endDate = endDate,
                    totalPrice = totalPrice
                )
                val reservation = apiService.updateReservation(reservationId, request, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error updating reservation $reservationId: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to update reservation"))
        }
    }

    override fun updateReservationStatus(
        reservationId: Long,
        status: String
    ): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                val mockReservation = Reservation(
                    id = reservationId,
                    userId = getCurrentUserId(),
                    status = status
                )
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockReservation))
            } else {
                val token = getAuthHeader()
                val statusUpdate = ReservationStatusUpdateRequest(status)
                val reservation = apiService.updateReservationStatus(reservationId, statusUpdate, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error updating status for reservation $reservationId: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to update reservation status"))
        }
    }

    override fun cancelReservation(reservationId: Long): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
            } else {
                val token = getAuthHeader()
                apiService.cancelReservation(reservationId, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error canceling reservation $reservationId: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to cancel reservation"))
        }
    }

    override fun getUpcomingReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val reservations = apiService.getUpcomingReservations(userId, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting upcoming reservations: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get upcoming reservations"))
        }
    }

    override fun getPastReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1000)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = emptyList()))
            } else {
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val reservations = apiService.getPastReservations(userId, token)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            }
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error getting past reservations: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to get past reservations"))
        }
    }
} 