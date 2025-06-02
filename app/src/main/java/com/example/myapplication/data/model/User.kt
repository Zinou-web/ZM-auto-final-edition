package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    val name: String? = null,
    val email: String,
    @SerializedName("phoneNumber") val phone: String? = null,
    @SerializedName("picture") val profileImage: String? = null,
    val favorites: List<Long> = emptyList(),
    @SerializedName("emailVerified") val isEmailVerified: Boolean = false,
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
