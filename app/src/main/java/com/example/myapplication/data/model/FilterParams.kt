package com.example.myapplication.data.model

data class FilterParams(
    val type: String? = null,
    val brand: String? = null,
    val maxPrice: Int = 5000,
    val minRating: Float = 0f
) 