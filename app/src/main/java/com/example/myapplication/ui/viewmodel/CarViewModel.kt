package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.ApiStatus
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for car listing and details
 */
@HiltViewModel
class CarViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _carsState = MutableStateFlow<CarsUiState>(CarsUiState.Initial)
    val carsState: StateFlow<CarsUiState> = _carsState
    
    private val _carDetailsState = MutableStateFlow<CarDetailsUiState>(CarDetailsUiState.Initial)
    val carDetailsState: StateFlow<CarDetailsUiState> = _carDetailsState

    /**
     * Load all available cars
     */
    fun loadAvailableCars() {
        viewModelScope.launch {
            _carsState.value = CarsUiState.Loading
            
            carRepository.getAllCars().collectLatest { result ->
                _carsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> CarsUiState.Success(result.data ?: emptyList())
                    ApiStatus.ERROR -> CarsUiState.Error(result.message ?: "Failed to load cars")
                    ApiStatus.LOADING -> CarsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Load cars by brand
     */
    fun loadCarsByBrand(brand: String) {
        viewModelScope.launch {
            _carsState.value = CarsUiState.Loading
            
            carRepository.getCarsByBrand(brand).collectLatest { result ->
                _carsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> CarsUiState.Success(result.data ?: emptyList())
                    ApiStatus.ERROR -> CarsUiState.Error(result.message ?: "Failed to load cars")
                    ApiStatus.LOADING -> CarsUiState.Loading
                }
            }
        }
    }
    
    /**
     * Load car details by ID
     */
    fun loadCarDetails(carId: Long) {
        viewModelScope.launch {
            _carDetailsState.value = CarDetailsUiState.Loading
            
            carRepository.getCarById(carId).collectLatest { result ->
                _carDetailsState.value = when (result.status) {
                    ApiStatus.SUCCESS -> result.data?.let { 
                        CarDetailsUiState.Success(it) 
                    } ?: CarDetailsUiState.Error("Car not found")
                    ApiStatus.ERROR -> CarDetailsUiState.Error(result.message ?: "Failed to load car details")
                    ApiStatus.LOADING -> CarDetailsUiState.Loading
                }
            }
        }
    }
}

/**
 * UI state for car listing
 */
sealed class CarsUiState {
    object Initial : CarsUiState()
    object Loading : CarsUiState()
    data class Success(val cars: List<Car>) : CarsUiState()
    data class Error(val message: String) : CarsUiState()
}

/**
 * UI state for car details
 */
sealed class CarDetailsUiState {
    object Initial : CarDetailsUiState()
    object Loading : CarDetailsUiState()
    data class Success(val car: Car) : CarDetailsUiState()
    data class Error(val message: String) : CarDetailsUiState()
} 