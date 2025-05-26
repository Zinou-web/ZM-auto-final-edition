package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.api.ApiResource
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.PagedResponse
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.preference.AuthPreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import com.example.myapplication.BuildConfig

/**
 * Implementation of the CarRepository interface.
 */
@Singleton
class CarRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferenceManager: AuthPreferenceManager
) : CarRepository {

    // Toggle mock data based on build config: debug builds use mock, release uses real backend
    private val useMockData = BuildConfig.DEBUG

    // Mock car data
    private val mockCars = listOf(
        Car(
            id = 1,
            brand = "Toyota",
            model = "Corolla",
            year = 2022,
            rentalPricePerDay = java.math.BigDecimal(55.0),
            transmission = "Automatic",
            rating = 4,
            colour = "White",
            fuel = "Petrol",
            type = "Sedan" // Exact match for filter type
        ),
        Car(
            id = 2,
            brand = "BMW",
            model = "X5",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(120.0),
            transmission = "Automatic",
            rating = 5,
            colour = "Black",
            fuel = "Diesel",
            type = "SUV" // Exact match for filter type
        ),
        Car(
            id = 3,
            brand = "Mercedes",
            model = "S-Class",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(150.0),
            transmission = "Automatic",
            rating = 5,
            colour = "Silver",
            fuel = "Petrol",
            type = "Luxury" // Exact match for filter type
        ),
        Car(
            id = 4,
            brand = "Toyota",
            model = "Yaris",
            year = 2021,
            rentalPricePerDay = java.math.BigDecimal(45.0),
            transmission = "Manual",
            rating = 3,
            colour = "Red",
            fuel = "Petrol",
            type = "Compact" // Exact match for filter type
        ),
        Car(
            id = 5,
            brand = "Tesla",
            model = "Model 3",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(130.0),
            transmission = "Automatic",
            rating = 5,
            colour = "White",
            fuel = "Electric",
            type = "Electric" // Exact match for filter type
        ),
        Car(
            id = 6,
            brand = "Audi",
            model = "A4",
            year = 2022,
            rentalPricePerDay = java.math.BigDecimal(95.0),
            transmission = "Automatic",
            rating = 4,
            colour = "Blue",
            fuel = "Petrol",
            type = "Sedan"
        ),
        Car(
            id = 7,
            brand = "BMW",
            model = "3 Series",
            year = 2021,
            rentalPricePerDay = java.math.BigDecimal(90.0),
            transmission = "Automatic",
            rating = 4,
            colour = "Black",
            fuel = "Diesel",
            type = "Luxury"
        ),
        Car(
            id = 8,
            brand = "Volkswagen",
            model = "Golf",
            year = 2020,
            rentalPricePerDay = java.math.BigDecimal(50.0),
            transmission = "Manual",
            rating = 3,
            colour = "Grey",
            fuel = "Petrol",
            type = "Compact"
        ),
        Car(
            id = 9,
            brand = "Nissan",
            model = "Qashqai",
            year = 2022,
            rentalPricePerDay = java.math.BigDecimal(70.0),
            transmission = "Automatic",
            rating = 4,
            colour = "Orange",
            fuel = "Petrol",
            type = "SUV"
        ),
        Car(
            id = 10,
            brand = "Tesla",
            model = "Model Y",
            year = 2023,
            rentalPricePerDay = java.math.BigDecimal(140.0),
            transmission = "Automatic",
            rating = 5,
            colour = "Black",
            fuel = "Electric",
            type = "Electric"
        )
    )

    init {
        // Debug: Print all available car types
        if (useMockData) {
            Log.d("CarRepository", "Available car types in mock data:")
            val types = mockCars.map { it.type }.distinct()
            Log.d("CarRepository", "Types: $types")
            mockCars.forEach { car ->
                Log.d("CarRepository", "Car ${car.id}: ${car.brand} ${car.model}, Type: '${car.type}'")
            }
        }
    }

    override fun getAllCars(): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock car data")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                emit(ApiResource(status = ApiStatus.SUCCESS, data = mockCars))
            } else {
                val cars = apiService.getAllCars()
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cars))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars"))
        }
    }

    override fun getCarById(id: Long): Flow<ApiResource<Car>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                // Mock implementation
                kotlinx.coroutines.delay(500)
                
                // Find car in our mock database or use a fallback
                val car = mockCars.find { it.id == id } ?: run {
                    Log.w("CarRepo", "Car ID $id not found in mock data. Creating a default car.")
                    // Fallback to creating a dynamic mock car if not found in the predefined list
                    val type = if (id % 2 == 0L) "SUV" else "Sedan"
                    val seats = if (type == "SUV") 7 else 5
                    val brand = when (id % 5) {
                        0L -> "Toyota"
                        1L -> "BMW"
                        2L -> "Mercedes"
                        3L -> "Audi"
                        else -> "Volkswagen"
                    }
                    val model = when (id % 3) {
                        0L -> "Premium"
                        1L -> "Standard"
                        else -> "Economy"
                    }
                    val color = when (id % 6) {
                        0L -> "White"
                        1L -> "Black"
                        2L -> "Silver"
                        3L -> "Blue"
                        4L -> "Red"
                        else -> "Green"
                    }
                    Car(
                        id = id,
                        brand = brand,
                        model = model,
                        year = 2023,
                        transmission = "Automatic",
                        rentalPricePerDay = java.math.BigDecimal(5000 + (id % 10) * 1000),
                        rating = 4L,
                        type = type,
                        seatingCapacity = seats,
                        colour = color
                    )
                }
                
                Log.d("CarRepo", "Returning mock car: ${car.brand} ${car.model} for id $id")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = car))
            } else {
                val car = apiService.getCarById(id)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = car))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e // Re-throw cancellation exceptions
        } catch (e: Exception) {
            Log.e("CarRepo", "Error getting car $id: ${e.message}", e)
            throw e // Re-throw other exceptions to be caught by the collector
        }
    }

    override fun getCarsByBrand(brand: String): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock car data for brand: '$brand'")
                kotlinx.coroutines.delay(500) // Shorter delay for better testing
                
                // Print all available brands for debugging
                val allBrands = mockCars.map { it.brand }.distinct()
                Log.d("CarRepository", "Available brands in mock data: $allBrands")
                
                // Use contains() for partial brand matching with detailed logging
                val filteredCars = mockCars.filter { car ->
                    val matches = car.brand.contains(brand, ignoreCase = true)
                    Log.d("CarRepository", "Checking car ${car.id}: brand='${car.brand}' against '$brand', matches=$matches")
                    matches
                }
                
                Log.d("CarRepository", "Found ${filteredCars.size} cars matching brand '$brand'")
                filteredCars.forEach { car ->
                    Log.d("CarRepository", "Matched car: ${car.brand} ${car.model}, Type: ${car.type}")
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filteredCars))
            } else {
                val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
                val cars = apiService.getCarsByBrand(brand, "Bearer $token")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cars))
            }
        } catch (e: Exception) {
            Log.e("CarRepository", "Error filtering by brand: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by brand"))
        }
    }

    override fun getCarsByModel(model: String): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock car data for model: $model")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                // Use contains() instead of equals() to match partial model names
                val filteredCars = mockCars.filter { 
                    it.model.contains(model, ignoreCase = true) || 
                    it.brand.contains(model, ignoreCase = true)
                }
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filteredCars))
            } else {
                val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
                val cars = apiService.getCarsByModel(model, "Bearer $token")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cars))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by model"))
        }
    }

    override fun getCarsByRatingRange(minRating: Long, maxRating: Long): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock car data for rating range: $minRating - $maxRating")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                val filteredCars = mockCars.filter { it.rating in minRating..maxRating }
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filteredCars))
            } else {
                val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
                val cars = apiService.getCarsByRatingRange(minRating, maxRating, "Bearer $token")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cars))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by rating"))
        }
    }

    override fun getCarsPaged(
        page: Int, 
        size: Int, 
        sort: String, 
        direction: String, 
        brand: String?, 
        model: String?, 
        minRating: Long?, 
        maxRating: Long?, 
        rentalStatus: String?
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged car data")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Apply filters if provided
                var filteredCars = mockCars
                
                if (!brand.isNullOrBlank()) {
                    filteredCars = filteredCars.filter { it.brand.contains(brand, ignoreCase = true) }
                    Log.d("CarRepository", "Filtered by brand '$brand': ${filteredCars.size} cars")
                }
                
                if (!model.isNullOrBlank()) {
                    filteredCars = filteredCars.filter { 
                        it.model.contains(model, ignoreCase = true) || 
                        it.type.equals(model, ignoreCase = true)  // Use equals for exact type matching
                    }
                    Log.d("CarRepository", "Filtered by model/type '$model': ${filteredCars.size} cars")
                    // Print the types of cars in the filtered list to debug
                    filteredCars.forEach { car ->
                        Log.d("CarRepository", "Car: ${car.brand} ${car.model}, Type: ${car.type}")
                    }
                }
                
                if (minRating != null && maxRating != null) {
                    filteredCars = filteredCars.filter { it.rating in minRating..maxRating }
                    Log.d("CarRepository", "Filtered by rating $minRating-$maxRating: ${filteredCars.size} cars")
                }
                
                if (!rentalStatus.isNullOrBlank()) {
                    filteredCars = filteredCars.filter { it.rentalStatus.contains(rentalStatus, ignoreCase = true) }
                    Log.d("CarRepository", "Filtered by status '$rentalStatus': ${filteredCars.size} cars")
                }
                
                // Calculate pagination
                val totalElements = filteredCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    filteredCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                val response = apiService.getAllCarsPaged(
                    page = page,
                    size = size,
                    sort = sort,
                    direction = direction,
                    brand = brand,
                    model = model,
                    minRating = minRating,
                    maxRating = maxRating,
                    rentalStatus = rentalStatus
                )
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load paged cars"))
        }
    }

    override fun getAvailableCarsPaged(
        page: Int, 
        size: Int, 
        sort: String
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged available car data")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                val availableCars = mockCars.filter { it.rentalStatus.equals("Available", ignoreCase = true) }
                
                // Calculate pagination
                val totalElements = availableCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    availableCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                val response = apiService.getAvailableCarsPaged(page, size, sort)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load available cars"))
        }
    }

    override fun getCarsByBrandPaged(
        brand: String,
        page: Int, 
        size: Int, 
        sort: String
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged car data for brand: $brand")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Use contains() instead of equals() for partial brand matching
                val filteredCars = mockCars.filter { it.brand.contains(brand, ignoreCase = true) }
                Log.d("CarRepository", "Found ${filteredCars.size} cars matching brand '$brand'")
                
                // Calculate pagination
                val totalElements = filteredCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    filteredCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                val response = apiService.getCarsByBrandPaged(brand, page, size, sort)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by brand"))
        }
    }

    override fun getCarsByModelPaged(
        model: String,
        page: Int, 
        size: Int, 
        sort: String
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged car data for model: $model")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Use contains() instead of equals() for partial matching
                val filteredCars = mockCars.filter { 
                    it.model.contains(model, ignoreCase = true) || 
                    it.brand.contains(model, ignoreCase = true) 
                }
                
                // Calculate pagination
                val totalElements = filteredCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    filteredCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                val response = apiService.getCarsByModelPaged(model, page, size, sort)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by model"))
        }
    }

    override fun getCarsByRatingRangePaged(
        minRating: Long,
        maxRating: Long,
        page: Int, 
        size: Int, 
        sort: String
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged car data for rating range: $minRating - $maxRating")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                val filteredCars = mockCars.filter { it.rating in minRating..maxRating }
                
                // Calculate pagination
                val totalElements = filteredCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    filteredCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                val response = apiService.getCarsByRatingRangePaged(minRating, maxRating, page, size, sort)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by rating"))
        }
    }

    override fun getCarsByType(type: String): Flow<ApiResource<List<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "FILTER DEBUG: Using mock car data for type: '$type'")
                kotlinx.coroutines.delay(500) // Shorter delay for testing
                
                // Print all available types for debugging
                val allTypes = mockCars.map { it.type }.distinct()
                Log.d("CarRepository", "FILTER DEBUG: Available types in mock data: $allTypes")
                
                // Use exact matching for type with detailed logging
                val filteredCars = mockCars.filter { car ->
                    val matches = car.type.equals(type, ignoreCase = true)
                    Log.d("CarRepository", "FILTER DEBUG: Checking car ${car.id}: type='${car.type}' against '$type', matches=$matches")
                    matches
                }
                
                Log.d("CarRepository", "FILTER DEBUG: Found ${filteredCars.size} cars matching type '$type'")
                filteredCars.forEach { car ->
                    Log.d("CarRepository", "FILTER DEBUG: Matched car: ${car.brand} ${car.model}, Type: ${car.type}")
                }
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = filteredCars))
            } else {
                val token = authPreferenceManager.getAuthToken() ?: throw IllegalStateException("Not authenticated")
                // Use getCarsByModel as a fallback since the API might not have a type endpoint
                val cars = apiService.getCarsByModel(type, "Bearer $token")
                emit(ApiResource(status = ApiStatus.SUCCESS, data = cars))
            }
        } catch (e: Exception) {
            Log.e("CarRepository", "FILTER DEBUG: Error filtering by type: ${e.message}", e)
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by type"))
        }
    }
    
    override fun getCarsByTypePaged(
        type: String,
        page: Int, 
        size: Int, 
        sort: String
    ): Flow<ApiResource<PagedResponse<Car>>> = flow {
        emit(ApiResource(status = ApiStatus.LOADING))
        try {
            if (useMockData) {
                Log.d("CarRepository", "Using mock paged car data for type: $type")
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Use exact matching for type
                val filteredCars = mockCars.filter { it.type.equals(type, ignoreCase = true) }
                Log.d("CarRepository", "Found ${filteredCars.size} cars matching type '$type'")
                
                // Log all cars and their types for debugging
                Log.d("CarRepository", "All car types:")
                mockCars.forEach { car ->
                    Log.d("CarRepository", "Car ${car.id}: ${car.brand} ${car.model}, Type: '${car.type}'")
                }
                
                // Calculate pagination
                val totalElements = filteredCars.size
                val totalPages = (totalElements + size - 1) / size
                val startIndex = page * size
                val endIndex = minOf(startIndex + size, totalElements)
                
                val pagedContent = if (startIndex < totalElements) {
                    filteredCars.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }
                
                val response = PagedResponse(
                    content = pagedContent,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    size = size,
                    number = page,
                    last = page >= totalPages - 1,
                    first = page == 0,
                    empty = pagedContent.isEmpty()
                )
                
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            } else {
                // In a real implementation, you would call apiService.getCarsByTypePaged(...)
                // For now, we'll use getCarsByModelPaged as a fallback
                val response = apiService.getCarsByModelPaged(type, page, size, sort)
                emit(ApiResource(status = ApiStatus.SUCCESS, data = response))
            }
        } catch (e: Exception) {
            emit(ApiResource(status = ApiStatus.ERROR, message = e.message ?: "Failed to load cars by type"))
        }
    }
} 