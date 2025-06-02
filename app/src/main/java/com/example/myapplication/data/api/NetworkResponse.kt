package com.example.myapplication.data.api

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for JSON responses from the server.
 */
data class NetworkResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("errorCode") val errorCode: String?,
    @SerializedName("message") val message: String?
) 