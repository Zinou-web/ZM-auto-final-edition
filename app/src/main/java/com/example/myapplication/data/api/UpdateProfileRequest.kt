package com.example.myapplication.data.api

import com.example.myapplication.data.model.Address
import com.example.myapplication.data.model.DrivingLicense

/**
 * Data class representing a request to update user profile information.
 */
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val birthday: String? = null,
    val address: Address? = null,
    val drivingLicense: DrivingLicense? = null
) 