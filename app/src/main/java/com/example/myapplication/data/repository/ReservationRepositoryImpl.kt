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
import com.example.myapplication.BuildConfig
import kotlinx.coroutines.flow.first

/**
 * Implementation of the ReservationRepository interface.
 */
@Singleton
class ReservationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager,
    private val carRepository: CarRepository
) : ReservationRepository {

    // Local cache for reservations when API is not available
    private val cachedReservations = mutableListOf<Reservation>()
    private var nextReservationId = 1000L
    // Toggle mock data based on build config
    private val useMockData = BuildConfig.DEBUG

    init {
        // Removed sample reservation seeding to avoid resetting state on each launch
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
                // Mock implementation: lookup in cachedReservations
                kotlinx.coroutines.delay(500)
                val reservation = cachedReservations.find { it.id == id }
                if (reservation != null) {
                    emit(ApiResource(status = ApiStatus.SUCCESS, data = reservation))
                } else {
                    // If reservation with specific ID not found, return the first cached reservation
                    // This ensures we don't get "reservation not found" errors in the mock implementation
                    if (cachedReservations.isNotEmpty()) {
                        val fallbackReservation = cachedReservations.first()
                        Log.d("ReservationRepo", "Reservation $id not found, using fallback reservation ${fallbackReservation.id}")
                        emit(ApiResource(status = ApiStatus.SUCCESS, data = fallbackReservation))
                    } else {
                        emit(ApiResource(status = ApiStatus.ERROR, message = "No reservations available"))
                    }
                }
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
        Log.d("ReservationRepo", "Attempting to create reservation with totalPrice: $totalPrice")
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(1500)

                val carObject: Car? = try {
                    // Collect the flow and get the car data if successful
                    val carApiResource = carRepository.getCarById(carId).first { it.status != ApiStatus.LOADING }
                    if (carApiResource.status == ApiStatus.SUCCESS) {
                        carApiResource.data
                    } else {
                        // Log the error from carRepository and throw to be caught by the outer block
                        Log.e("ReservationRepo", "Failed to fetch car $carId for mock reservation: ${carApiResource.message}")
                        throw Exception("Failed to fetch car details for mock reservation: ${carApiResource.message}")
                    }
                } catch (carEx: Exception) {
                    // Log and rethrow if car fetching fails, to be caught by the outer try-catch
                    Log.e("ReservationRepo", "Exception while fetching car $carId for mock reservation: ${carEx.message}", carEx)
                    throw carEx // Rethrow to be caught by the main catch block
                }

                val mockReservation = Reservation(
                    id = nextReservationId++,
                    userId = getCurrentUserId(),
                    carId = carId,
                    startDate = startDate,
                    endDate = endDate,
                    status = "PENDING", // Initial status for cash/unpaid Edahabia
                    totalPrice = totalPrice,
                    createdAt = LocalDateTime.now(),
                    car = carObject
                )
                
                // Add the new reservation to the cached list
                cachedReservations.add(mockReservation)
                Log.d("ReservationRepo", "Created mock reservation with ID: ${mockReservation.id} for car ID: $carId, Status: ${mockReservation.status}")
                
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
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // Re-throw cancellation exceptions
        } catch (e: Exception) {
            Log.e("ReservationRepo", "Error creating reservation: ${e.message}", e)
            // Ensure we don't emit if the flow is cancelled
            // Instead, let the exception propagate if it's not a cancellation
            // This will be handled by the ViewModel's .catch operator
            throw e
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
                
                // Find and update the reservation in the cache
                val existingReservation = cachedReservations.find { it.id == reservationId }
                val updatedReservation = if (existingReservation != null) {
                    // Create a copy with the new status
                    val updated = existingReservation.copy(status = status)
                    // Replace in cache
                    val index = cachedReservations.indexOf(existingReservation)
                    if (index >= 0) {
                        cachedReservations[index] = updated
                    }
                    Log.d("ReservationRepo", "Updated reservation ID: $reservationId status to: $status")
                    updated
                } else {
                    // Create a new mock reservation if not found
                    Log.d("ReservationRepo", "Reservation ID: $reservationId not found in cache, creating mock")
                    Reservation(
                        id = reservationId,
                        userId = getCurrentUserId(),
                        carId = 1L,
                        status = status,
                        createdAt = LocalDateTime.now()
                    )
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = updatedReservation))
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
                
                // Find and update the reservation in the cache
                val existingReservation = cachedReservations.find { it.id == reservationId }
                if (existingReservation != null) {
                    // Create a copy with the CANCELLED status
                    val updated = existingReservation.copy(status = "CANCELLED")
                    // Replace in cache
                    val index = cachedReservations.indexOf(existingReservation)
                    if (index >= 0) {
                        cachedReservations[index] = updated
                    }
                    Log.d("ReservationRepo", "Cancelled reservation ID: $reservationId")
                } else {
                    Log.d("ReservationRepo", "Reservation ID: $reservationId not found in cache")
                }
                
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

    /**
     * Get upcoming reservations for the current user.
     */
    override fun getUpcomingReservations(): Flow<ApiResource<List<Reservation>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(800)
                
                // Filter upcoming reservations (today or future dates)
                val today = LocalDate.now()
                Log.d("ReservationRepo", "getUpcomingReservations: Filtering mock data. Today is: $today")
                val upcomingReservations = cachedReservations.filter { reservation ->
                    val isTodayOrFuture = reservation.startDate?.isEqual(today) == true || reservation.startDate?.isAfter(today) == true
                    val isActiveStatus = reservation.status != "COMPLETED" && reservation.status != "CANCELLED"
                    Log.d("ReservationRepo", "  Checking Res ID ${reservation.id}: startDate=${reservation.startDate}, status=${reservation.status}. isTodayOrFuture=$isTodayOrFuture, isActiveStatus=$isActiveStatus")
                    isTodayOrFuture && isActiveStatus
                }
                
                // Add debugging log to see what's in the cache
                Log.d("ReservationRepo", "Cached reservations total count: ${cachedReservations.size}")
                cachedReservations.forEach { reservation ->
                    Log.d("ReservationRepo", "Cached reservation: ID=${reservation.id}, " +
                          "carId=${reservation.carId}, status=${reservation.status}, " +
                          "startDate=${reservation.startDate}, endDate=${reservation.endDate}")
                }
                
                Log.d("ReservationRepo", "Found ${upcomingReservations.size} upcoming reservations")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = upcomingReservations))
            } else {
                // Use the real API implementation
                val token = getAuthHeader()
                val userId = getCurrentUserId()
                val reservations = apiService.getReservationsByUserId(userId, token)
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
                
                // Filter cached reservations to find past ones (completed or end date in the past)
                val pastReservations = cachedReservations.filter { 
                    it.status == "COMPLETED" || 
                    it.endDate.isBefore(LocalDate.now())
                }
                Log.d("ReservationRepo", "Found ${pastReservations.size} past reservations in cache")
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = pastReservations))
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