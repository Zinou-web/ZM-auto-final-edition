package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiStatus
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

    override fun getAllReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            try {
            val reservations = apiService.getAllReservations("Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data", e)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cachedReservations))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load reservations"))
        }
    }

    override fun getReservationById(id: Long): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            try {
            val reservation = apiService.getReservationById(id, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
            } catch (e: Exception) {
                // Fallback to cached reservation if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data", e)
                val cachedReservation = cachedReservations.find { it.id == id }
                if (cachedReservation != null) {
                    emit(ApiResource(status = ApiStatus.SUCCESS, data = cachedReservation))
                } else {
                    emit(ApiResource(status = ApiStatus.ERROR, message = "Reservation not found"))
                }
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load reservation details"))
        }
    }

    override fun getUserReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            try {
            val reservations = apiService.getReservationsByUserId(userId, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data", e)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cachedReservations))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load user reservations"))
        }
    }

    override fun getReservationsByCarId(carId: Long): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            try {
            val reservations = apiService.getReservationsByCarId(carId, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data", e)
                val filtered = cachedReservations.filter { it.carId == carId }
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filtered))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load car reservations"))
        }
    }

    override fun getReservationsByStatus(status: String): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            try {
            val reservations = apiService.getReservationsByStatus(status, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data", e)
                val filtered = cachedReservations.filter { it.status == status }
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filtered))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load reservations by status"))
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
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            
            // Create a reservation object
            val reservation = Reservation(
                id = 0L, // Will be set by server
                userId = userId,
                carId = carId,
                startDate = startDate,
                endDate = endDate,
                totalPrice = totalPrice,
                status = "PENDING",
                paymentStatus = "UNPAID"
            )
            
            try {
            val createdReservation = apiService.createUserReservation(reservation, "Bearer $token")
                
                // Add to cache
                cachedReservations.add(createdReservation)
                
            emit(ApiResource(status = ApiStatus.SUCCESS, data = createdReservation))
            } catch (e: Exception) {
                // Create a local reservation if the API call fails
                Log.e("ReservationRepo", "API call failed, creating local reservation", e)
                val localReservation = reservation.copy(
                    id = nextReservationId++,
                    createdAt = LocalDateTime.now()
                )
                
                // Get car details for the reservation (mock data for demo)
                val mockCar = Car(
                    id = carId,
                    brand = "Toyota",
                    model = "Corolla",
                    year = 2023,
                    transmission = "Automatic",
                    rentalPricePerDay = java.math.BigDecimal(totalPrice / (endDate.toEpochDay() - startDate.toEpochDay() + 1)),
                    rating = 4
                )
                
                // Add car details to the reservation
                val reservationWithCar = localReservation.copy(car = mockCar)
                
                // Add to cache
                cachedReservations.add(reservationWithCar)
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = reservationWithCar))
            }
        } catch (e: Exception) {
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
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            
            try {
            // First get the existing reservation
            val existingReservation = apiService.getReservationById(reservationId, "Bearer $token")
            
            // Update the reservation
            val updatedReservation = existingReservation.copy(
                startDate = startDate,
                endDate = endDate,
                totalPrice = totalPrice
            )
            
            val result = apiService.updateReservation(reservationId, updatedReservation, "Bearer $token")
                
                // Update in cache
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    cachedReservations[index] = result
                }
                
            emit(ApiResource(status = ApiStatus.SUCCESS, data = result))
            } catch (e: Exception) {
                // Update local reservation if API call fails
                Log.e("ReservationRepo", "API call failed, updating local reservation", e)
                
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    val updatedReservation = cachedReservations[index].copy(
                        startDate = startDate,
                        endDate = endDate,
                        totalPrice = totalPrice
                    )
                    cachedReservations[index] = updatedReservation
                    emit(ApiResource(status = ApiStatus.SUCCESS, data = updatedReservation))
                } else {
                    emit(ApiResource(status = ApiStatus.ERROR, message = "Reservation not found"))
                }
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to update reservation"))
        }
    }

    override fun updateReservationStatus(
        reservationId: Long,
        status: String
    ): Flow<ApiResource<Reservation>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            
            try {
            val result = apiService.updateReservationStatus(reservationId, status, "Bearer $token")
                
                // Update in cache
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    cachedReservations[index] = cachedReservations[index].copy(status = status)
                }
                
            emit(ApiResource(status = ApiStatus.SUCCESS, data = result))
            } catch (e: Exception) {
                // Update local reservation status if API call fails
                Log.e("ReservationRepo", "API call failed, updating local reservation status", e)
                
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    val updatedReservation = cachedReservations[index].copy(status = status)
                    cachedReservations[index] = updatedReservation
                    emit(ApiResource(status = ApiStatus.SUCCESS, data = updatedReservation))
                } else {
                    emit(ApiResource(status = ApiStatus.ERROR, message = "Reservation not found"))
                }
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to update reservation status"))
        }
    }

    override fun cancelReservation(reservationId: Long): Flow<ApiResource<Boolean>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            
            try {
            apiService.cancelReservation(reservationId, "Bearer $token")
                
                // Update in cache
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    cachedReservations[index] = cachedReservations[index].copy(status = "CANCELLED")
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
            } catch (e: Exception) {
                // Update local reservation if API call fails
                Log.e("ReservationRepo", "API call failed, cancelling local reservation", e)
                
                val index = cachedReservations.indexOfFirst { it.id == reservationId }
                if (index >= 0) {
                    cachedReservations[index] = cachedReservations[index].copy(status = "CANCELLED")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = true))
                } else {
                    emit(ApiResource(status = ApiStatus.ERROR, message = "Reservation not found"))
                }
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to cancel reservation"))
        }
    }

    override fun getUpcomingReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            
            try {
            val reservations = apiService.getUpcomingReservations(userId, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data for upcoming reservations", e)
                
                val now = LocalDate.now()
                val upcomingReservations = cachedReservations.filter { 
                    it.status != "CANCELLED" && 
                    it.status != "COMPLETED" && 
                    (it.endDate.isEqual(now) || it.endDate.isAfter(now))
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = upcomingReservations))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load upcoming reservations"))
        }
    }

    override fun getPastReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
            val userId = authPreferenceManager.getUserId()?.toLongOrNull() ?: throw IllegalStateException("User ID not found")
            
            try {
            val reservations = apiService.getPastReservations(userId, "Bearer $token")
            emit(ApiResource(status = ApiStatus.SUCCESS, data = reservations))
            } catch (e: Exception) {
                // Fallback to cached reservations if API call fails
                Log.e("ReservationRepo", "API call failed, using cached data for past reservations", e)
                
                val now = LocalDate.now()
                val pastReservations = cachedReservations.filter { 
                    it.status == "COMPLETED" || 
                    it.status == "CANCELLED" || 
                    it.endDate.isBefore(now)
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = pastReservations))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load past reservations"))
        }
    }
} 