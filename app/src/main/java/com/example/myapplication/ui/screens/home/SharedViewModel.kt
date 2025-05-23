package com.example.myapplication.ui.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Shared ViewModel to maintain filter state across screens
 */
@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    
    // Filter state
    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType
    
    private val _filterBrand = MutableStateFlow<String?>(null)
    val filterBrand: StateFlow<String?> = _filterBrand
    
    private val _filterMinRating = MutableStateFlow<Long?>(null)
    val filterMinRating: StateFlow<Long?> = _filterMinRating
    
    /**
     * Set filter parameters
     */
    fun setFilters(type: String?, brand: String?, minRating: Long?) {
        _filterType.value = type
        _filterBrand.value = brand
        _filterMinRating.value = minRating
    }
    
    /**
     * Reset all filters
     */
    fun resetFilters() {
        _filterType.value = null
        _filterBrand.value = null
        _filterMinRating.value = null
    }
} 