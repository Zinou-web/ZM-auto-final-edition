package com.example.myapplication.data.model

data class User(
    val id: Long,
    val name: String? = null,
    val email: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val address: Address? = null,
    val drivingLicense: DrivingLicense? = null,
    val favorites: List<Long> = emptyList(),
    val isEmailVerified: Boolean = false,
    val profileImageUrl: String? = null
)

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

data class DrivingLicense(
    val number: String,
    val expiryDate: String,
    val issuingCountry: String,
    val licenseImage: String? = null
)
