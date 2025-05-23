package com.example.myapplication.ui.screens.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.api.PagedResponse
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.FilterParams
import com.example.myapplication.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CarViewModel"

/**
 * Sealed class representing different UI states for car data.
 */
sealed class CarUiState {
    object Loading : CarUiState()
    data class Success(val cars: List<Car>) : CarUiState()
    data class PaginatedSuccess(val pagedResponse: PagedResponse<Car>) : CarUiState()
    data class SingleCarSuccess(val car: Car) : CarUiState()
    data class Error(val message: String) : CarUiState()
    object Idle : CarUiState()
}

/**
 * ViewModel for car-related operations.
 */
@HiltViewModel
class CarViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    
    // Car list UI state
    private val _uiState = MutableStateFlow<CarUiState>(CarUiState.Idle)
    val uiState: StateFlow<CarUiState> = _uiState
    
    // Current active filters
    private val _currentFilters = MutableStateFlow<FilterParams?>(null)
    val currentFilters: StateFlow<FilterParams?> = _currentFilters
    
    // Popular cars
    private val _popularCars = MutableStateFlow<List<Car>>(emptyList())
    val popularCars: StateFlow<List<Car>> = _popularCars
    
    // Car details UI state
    private val _carDetailsState = MutableStateFlow<CarUiState>(CarUiState.Idle)
    val carDetailsState: StateFlow<CarUiState> = _carDetailsState
    
    // Pagination
    private var currentPage = 0
    private var totalPages = 1
    private var isLastPage = false
    private val pageSize = 10
    
    // Filters
    private var brandFilter: String? = null
    private var modelFilter: String? = null
    private var minRatingFilter: Long? = null
    private var maxRatingFilter: Long? = null
    
    init {
        loadAllCars()
        getPopularCars()
    }
    
    /**
     * Load cars with pagination.
     */
    private fun loadCarsPaged(page: Int, resetFilters: Boolean = true) {
        if (resetFilters) {
            brandFilter = null
            modelFilter = null
            minRatingFilter = null
            maxRatingFilter = null
        }
        
        currentPage = page
        _uiState.value = CarUiState.Loading
        
        if (brandFilter != null) {
            brandFilter?.let { filterByBrand(it, page) }
        } else if (modelFilter != null) {
            modelFilter?.let { filterByModel(it, page) }
        } else if (minRatingFilter != null && maxRatingFilter != null) {
            filterByRatingRange(minRatingFilter!!, maxRatingFilter!!, page)
        } else {
            // Load all cars or apply available filters
            loadCarsWithFilters(
                page = page,
                size = pageSize,
                brand = brandFilter,
                model = modelFilter,
                minRating = minRatingFilter,
                maxRating = maxRatingFilter
            )
        }
    }
    
    /**
     * Debug function to print all car types
     */
    private fun debugPrintAllCarTypes() {
        viewModelScope.launch {
            carRepository.getAllCars().collectLatest { result ->
                if (result.status == ApiStatus.SUCCESS) {
                    result.data?.let { cars ->
                        val types = cars.map { it.type }.distinct().sorted()
                        Log.d(TAG, "All available car types: $types")
                        cars.forEach { car ->
                            Log.d(TAG, "Car ${car.id}: ${car.brand} ${car.model}, Type: '${car.type}'")
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Load all cars from the repository.
     */
    fun loadAllCars() {
        _uiState.value = CarUiState.Loading
        
        // Clear filters
        _currentFilters.value = null
        
        // Debug: print all car types
        debugPrintAllCarTypes()
        
        viewModelScope.launch {
            carRepository.getAllCars().collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { cars ->
                            _uiState.value = CarUiState.Success(cars)
                            Log.d(TAG, "Loaded ${cars.size} cars")
                        } ?: run {
                            _uiState.value = CarUiState.Error("No cars found")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to load cars")
                        Log.e(TAG, "Error loading cars: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Load car details by ID.
     */
    fun loadCarById(carId: Long) {
        _carDetailsState.value = CarUiState.Loading
        
        viewModelScope.launch {
            carRepository.getCarById(carId).collectLatest { result ->
                _carDetailsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> {
                        val car = result.data
                        if (car != null) {
                            CarUiState.Success(listOf(car))
                        } else {
                            CarUiState.Error("Car not found")
                        }
                    }
                    ApiStatus.ERROR -> {
                        CarUiState.Error(result.message ?: "Failed to load car details")
                    }
                    ApiStatus.LOADING -> {
                        CarUiState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Load the next page of cars.
     */
    fun loadNextPage() {
        if (!isLastPage) {
            currentPage++
            loadCarsPaged(currentPage, false)
        }
    }
    
    /**
     * Load the previous page of cars.
     */
    fun loadPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            loadCarsPaged(currentPage, false)
        }
    }
    
    /**
     * Get the current page number.
     */
    fun getCurrentPage(): Int = currentPage
    
    /**
     * Get the total number of pages.
     */
    fun getTotalPages(): Int = totalPages
    
    /**
     * Check if this is the last page.
     */
    fun isLastPage(): Boolean = isLastPage
    
    /**
     * Load cars with filters.
     */
    fun loadCarsWithFilters(
        page: Int = 0,
        size: Int = 10,
        brand: String? = null,
        model: String? = null,
        minRating: Long? = null,
        maxRating: Long? = null
    ) {
        _uiState.value = CarUiState.Loading
        
        Log.d(TAG, "FILTER DEBUG: Loading cars with filters - brand: '$brand', model/type: '$model', rating: $minRating-$maxRating")
        
        // Store filter parameters for debugging
        brandFilter = brand
        modelFilter = model
        minRatingFilter = minRating
        
        // Simplify filtering logic for more reliable results
        viewModelScope.launch {
            try {
                // First, get all cars
                carRepository.getAllCars().collect { result ->
                    if (result.status == ApiStatus.SUCCESS) {
                        val allCars = result.data ?: emptyList()
                        Log.d(TAG, "FILTER DEBUG: Got ${allCars.size} total cars before filtering")
                        
                        // Apply filters manually
                        var filteredCars = allCars
                        
                        // Filter by brand if specified
                        if (!brand.isNullOrBlank()) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by brand: '$brand'")
                            filteredCars = filteredCars.filter { car ->
                                val matches = car.brand.equals(brand, ignoreCase = true)
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) brand match: $matches")
                                matches
                            }
                            Log.d(TAG, "FILTER DEBUG: After brand filter: ${filteredCars.size} cars")
                        }
                        
                        // Filter by model/type if specified
                        if (!model.isNullOrBlank()) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by model/type: '$model'")
                            filteredCars = filteredCars.filter { car ->
                                // Check both type and model for a match
                                val typeMatch = car.type.equals(model, ignoreCase = true)
                                val modelMatch = car.model.contains(model, ignoreCase = true)
                                val matches = typeMatch || modelMatch
                                
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}, type=${car.type}) type/model match: $matches")
                                matches
                            }
                            Log.d(TAG, "FILTER DEBUG: After model/type filter: ${filteredCars.size} cars")
                        }
                        
                        // Filter by rating if specified
                        if (minRating != null) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by min rating: $minRating")
                            filteredCars = filteredCars.filter { car ->
                                val matches = car.rating >= minRating
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) rating ${car.rating} >= $minRating: $matches")
                                matches
                            }
                            Log.d(TAG, "FILTER DEBUG: After rating filter: ${filteredCars.size} cars")
                        }
                        
                        // Update UI state with filtered cars
                        Log.d(TAG, "FILTER DEBUG: Final filtered cars: ${filteredCars.size}")
                        filteredCars.forEach { car ->
                            Log.d(TAG, "FILTER DEBUG: Filtered car: ${car.id} - ${car.brand} ${car.model} (${car.type})")
                        }
                        
                        _uiState.value = CarUiState.Success(filteredCars)
                    } else if (result.status == ApiStatus.ERROR) {
                        Log.e(TAG, "FILTER DEBUG: Error getting cars: ${result.message}")
                        _uiState.value = CarUiState.Error(result.message ?: "Error filtering cars")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "FILTER DEBUG: Exception applying filters", e)
                _uiState.value = CarUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Get available cars.
     */
    fun getAvailableCars(page: Int = 0, size: Int = 10) {
        _uiState.value = CarUiState.Loading
        
        viewModelScope.launch {
            carRepository.getAvailableCarsPaged(
                page = page,
                size = size,
                sort = "brand" // Default sorting
            ).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { pagedResponse ->
                            _uiState.value = CarUiState.PaginatedSuccess(pagedResponse)
                            totalPages = pagedResponse.totalPages
                            isLastPage = pagedResponse.last
                            Log.d(TAG, "Loaded available cars: ${pagedResponse.content.size} items")
                        } ?: run {
                            _uiState.value = CarUiState.Error("No available cars found")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to load available cars")
                        Log.e(TAG, "Error loading available cars: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Filter cars by brand with pagination.
     */
    fun filterByBrand(brand: String, page: Int = 0, size: Int = 10) {
        if (brand.isEmpty()) {
            loadAllCars()
            return
        }
        
        _uiState.value = CarUiState.Loading
        
        viewModelScope.launch {
            carRepository.getCarsByBrandPaged(
                brand = brand,
                page = page,
                size = size,
                sort = "model" // Default sorting
            ).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { pagedResponse ->
                            _uiState.value = CarUiState.PaginatedSuccess(pagedResponse)
                            totalPages = pagedResponse.totalPages
                            isLastPage = pagedResponse.last
                            Log.d(TAG, "Filtered cars by brand '$brand': ${pagedResponse.content.size} items")
                        } ?: run {
                            _uiState.value = CarUiState.Error("No cars found for brand: $brand")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to filter cars by brand")
                        Log.e(TAG, "Error filtering cars by brand: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Filter cars by model with pagination.
     */
    fun filterByModel(model: String, page: Int = 0, size: Int = 10) {
        if (model.isEmpty()) {
            loadAllCars()
            return
        }
        
        _uiState.value = CarUiState.Loading
        
        viewModelScope.launch {
            carRepository.getCarsByModelPaged(
                model = model,
                page = page,
                size = size,
                sort = "rating" // Default sorting
            ).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { pagedResponse ->
                            _uiState.value = CarUiState.PaginatedSuccess(pagedResponse)
                            totalPages = pagedResponse.totalPages
                            isLastPage = pagedResponse.last
                            Log.d(TAG, "Filtered cars by model '$model': ${pagedResponse.content.size} items")
                        } ?: run {
                            _uiState.value = CarUiState.Error("No cars found for model: $model")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to filter cars by model")
                        Log.e(TAG, "Error filtering cars by model: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Filter cars by rating range with pagination.
     */
    fun filterByRatingRange(minRating: Long, maxRating: Long, page: Int = 0, size: Int = 10) {
        _uiState.value = CarUiState.Loading
        
        viewModelScope.launch {
            carRepository.getCarsByRatingRangePaged(
                minRating = minRating,
                maxRating = maxRating,
                page = page,
                size = size,
                sort = "rating" // Default sorting
            ).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        result.data?.let { pagedResponse ->
                            _uiState.value = CarUiState.PaginatedSuccess(pagedResponse)
                            totalPages = pagedResponse.totalPages
                            isLastPage = pagedResponse.last
                            Log.d(TAG, "Filtered cars by rating range $minRating-$maxRating: ${pagedResponse.content.size} items")
                        } ?: run {
                            _uiState.value = CarUiState.Error("No cars found for rating range: $minRating-$maxRating")
                        }
                    }
                    ApiStatus.ERROR -> {
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to filter cars by rating")
                        Log.e(TAG, "Error filtering cars by rating: ${result.message}")
                    }
                    ApiStatus.LOADING -> {
                        // Already set loading state above
                    }
                }
            }
        }
    }
    
    /**
     * Filter cars by type (SUV, Sedan, etc.) with pagination.
     */
    fun filterByType(type: String, page: Int = 0, size: Int = 10) {
        if (type.isEmpty() || type == "All") {
            Log.d(TAG, "Type is empty or 'All', loading all cars")
            loadAllCars()
            return
        }
        
        _uiState.value = CarUiState.Loading
        Log.d(TAG, "Filtering cars by type: '$type'")
        
        viewModelScope.launch {
            try {
                // Use the dedicated type filter method
                carRepository.getCarsByType(type).collectLatest { result ->
                    when (result.status) {
                        ApiStatus.SUCCESS -> {
                            result.data?.let { cars ->
                                Log.d(TAG, "Got ${cars.size} cars matching type '$type'")
                                cars.forEach { car ->
                                    Log.d(TAG, "Car: ${car.brand} ${car.model}, Type: '${car.type}'")
                                }
                                _uiState.value = CarUiState.Success(cars)
                            } ?: run {
                                Log.d(TAG, "No cars found for type: $type")
                                _uiState.value = CarUiState.Error("No cars found")
                            }
                        }
                        ApiStatus.ERROR -> {
                            Log.e(TAG, "Error filtering by type: ${result.message}")
                            _uiState.value = CarUiState.Error(result.message ?: "Failed to load cars")
                        }
                        ApiStatus.LOADING -> {
                            _uiState.value = CarUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception filtering by type: ${e.message}", e)
                _uiState.value = CarUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Apply filters directly from FilterParams
     */
    fun applyFilters(filters: FilterParams?) {
        Log.d(TAG, "FILTER DEBUG: applyFilters called with filters: $filters")
        
        // Update current filters state
        _currentFilters.value = filters
        
        if (filters == null) {
            // If filters are null, load all cars
            Log.d(TAG, "FILTER DEBUG: Filters are null, loading all cars")
            loadAllCars()
            return
        }
        
        Log.d(TAG, "FILTER DEBUG: Applying filters directly:")
        Log.d(TAG, "FILTER DEBUG: Type: ${filters.type ?: "None"}")
        Log.d(TAG, "FILTER DEBUG: Brand: ${filters.brand ?: "None"}")
        Log.d(TAG, "FILTER DEBUG: Min Rating: ${filters.minRating}")
        Log.d(TAG, "FILTER DEBUG: Max Price: ${filters.maxPrice}")
        
        // Force UI update by setting loading state
        _uiState.value = CarUiState.Loading
        
        // Apply filters
        viewModelScope.launch {
            try {
                Log.d(TAG, "FILTER DEBUG: Starting filter application")
                
                // Get all cars from repository
                carRepository.getAllCars().collect { result ->
                    if (result.status == ApiStatus.SUCCESS) {
                        val allCars = result.data ?: emptyList()
                        Log.d(TAG, "FILTER DEBUG: Got ${allCars.size} cars from repository")
                        
                        // Apply filters to the cars
                        var filteredCars = allCars
                        
                        // Apply type filter
                        if (!filters.type.isNullOrBlank()) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by type: '${filters.type}'")
                            filteredCars = filteredCars.filter { car ->
                                val match = car.type.equals(filters.type, ignoreCase = true)
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) type=${car.type}, match=$match")
                                match
                            }
                            Log.d(TAG, "FILTER DEBUG: After type filter: ${filteredCars.size} cars")
                        }
                        
                        // Apply brand filter
                        if (!filters.brand.isNullOrBlank()) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by brand: '${filters.brand}'")
                            filteredCars = filteredCars.filter { car ->
                                val match = car.brand.equals(filters.brand, ignoreCase = true)
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) brand match=$match")
                                match
                            }
                            Log.d(TAG, "FILTER DEBUG: After brand filter: ${filteredCars.size} cars")
                        }
                        
                        // Apply rating filter
                        if (filters.minRating > 0) {
                            Log.d(TAG, "FILTER DEBUG: Filtering by min rating: ${filters.minRating}")
                            filteredCars = filteredCars.filter { car ->
                                val match = car.rating >= filters.minRating
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) rating=${car.rating}, match=$match")
                                match
                            }
                            Log.d(TAG, "FILTER DEBUG: After rating filter: ${filteredCars.size} cars")
                        }
                        
                        // Apply price filter if needed
                        if (filters.maxPrice < 5000) {  // Only apply if it's not the default max
                            Log.d(TAG, "FILTER DEBUG: Filtering by max price: ${filters.maxPrice}")
                            filteredCars = filteredCars.filter { car ->
                                val price = car.rentalPricePerDay.toInt()
                                val match = price <= filters.maxPrice
                                Log.d(TAG, "FILTER DEBUG: Car ${car.id} (${car.brand} ${car.model}) price=$price, match=$match")
                                match
                            }
                            Log.d(TAG, "FILTER DEBUG: After price filter: ${filteredCars.size} cars")
                        }
                        
                        // Update UI state with filtered cars
                        Log.d(TAG, "FILTER DEBUG: Filter application complete, returning ${filteredCars.size} cars")
                        _uiState.value = CarUiState.Success(filteredCars)
                        
                    } else {
                        Log.e(TAG, "FILTER DEBUG: Error getting cars: ${result.message}")
                        _uiState.value = CarUiState.Error(result.message ?: "Failed to load cars")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "FILTER DEBUG: Error applying filters", e)
                _uiState.value = CarUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Create a mock list of cars for testing
     */
    private fun createMockCarList(): List<com.example.myapplication.data.model.Car> {
        return listOf(
            com.example.myapplication.data.model.Car(
                id = 1,
                brand = "Toyota",
                model = "Corolla",
                year = 2022,
                rentalPricePerDay = java.math.BigDecimal(55.0),
                transmission = "Automatic",
                rating = 4,
                colour = "White",
                fuel = "Petrol",
                type = "Sedan"
            ),
            com.example.myapplication.data.model.Car(
                id = 2,
                brand = "BMW",
                model = "X5",
                year = 2023,
                rentalPricePerDay = java.math.BigDecimal(120.0),
                transmission = "Automatic",
                rating = 5,
                colour = "Black",
                fuel = "Diesel",
                type = "SUV"
            ),
            com.example.myapplication.data.model.Car(
                id = 3,
                brand = "Mercedes",
                model = "S-Class",
                year = 2023,
                rentalPricePerDay = java.math.BigDecimal(150.0),
                transmission = "Automatic",
                rating = 5,
                colour = "Silver",
                fuel = "Petrol",
                type = "Luxury"
            ),
            com.example.myapplication.data.model.Car(
                id = 4,
                brand = "Toyota",
                model = "Yaris",
                year = 2021,
                rentalPricePerDay = java.math.BigDecimal(45.0),
                transmission = "Manual",
                rating = 3,
                colour = "Red",
                fuel = "Petrol",
                type = "Compact"
            ),
            com.example.myapplication.data.model.Car(
                id = 5,
                brand = "Tesla",
                model = "Model 3",
                year = 2023,
                rentalPricePerDay = java.math.BigDecimal(130.0),
                transmission = "Automatic",
                rating = 5,
                colour = "White",
                fuel = "Electric",
                type = "Electric"
            )
        )
    }
    
    /**
     * Get popular cars - for now just returns a subset of all cars
     * In a real app, this would fetch cars based on popularity metrics
     */
    fun getPopularCars() {
        viewModelScope.launch {
            try {
                carRepository.getAllCars().collect { result ->
                    if (result.status == ApiStatus.SUCCESS) {
                        val allCars = result.data ?: emptyList()
                        // For demo purposes, just take a few cars and consider them "popular"
                        // In a real app, we would fetch cars based on popularity metrics
                        val popularCars = allCars.take(3)
                        Log.d(TAG, "POPULAR CARS: Got ${popularCars.size} popular cars")
                        
                        // Update the popular cars state
                        _popularCars.value = popularCars
                        
                        popularCars.forEach { car ->
                            Log.d(TAG, "Popular car: ${car.id} - ${car.brand} ${car.model}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting popular cars", e)
            }
        }
    }
    
    /**
     * Search cars by query string across multiple fields (brand, model, type)
     */
    fun searchCars(query: String) {
        if (query.isEmpty()) {
            loadAllCars()
            return
        }
        
        _uiState.value = CarUiState.Loading
        Log.d(TAG, "SEARCH DEBUG: Searching for cars with query: '$query'")
        
        viewModelScope.launch {
            try {
                // Get all cars
                carRepository.getAllCars().collect { result ->
                    if (result.status == ApiStatus.SUCCESS) {
                        val allCars = result.data ?: emptyList()
                        Log.d(TAG, "SEARCH DEBUG: Got ${allCars.size} cars to search through")
                        
                        // Search across multiple fields
                        val searchResults = allCars.filter { car ->
                            val matchesBrand = car.brand.contains(query, ignoreCase = true)
                            val matchesModel = car.model.contains(query, ignoreCase = true)
                            val matchesType = car.type.contains(query, ignoreCase = true)
                            
                            val matches = matchesBrand || matchesModel || matchesType
                            
                            // Log detailed search results for debugging
                            if (matches) {
                                Log.d(TAG, "SEARCH DEBUG: Match found - ${car.brand} ${car.model} (${car.type})")
                                Log.d(TAG, "SEARCH DEBUG:   Brand match: $matchesBrand, Model match: $matchesModel, Type match: $matchesType")
                            }
                            
                            matches
                        }
                        
                        Log.d(TAG, "SEARCH DEBUG: Found ${searchResults.size} matching cars")
                        
                        if (searchResults.isNotEmpty()) {
                            _uiState.value = CarUiState.Success(searchResults)
                        } else {
                            _uiState.value = CarUiState.Error("No cars found matching '$query'")
                        }
                    } else {
                        _uiState.value = CarUiState.Error(result.message ?: "Error searching cars")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "SEARCH DEBUG: Error searching cars", e)
                _uiState.value = CarUiState.Error("Error: ${e.message}")
            }
        }
    }
} 