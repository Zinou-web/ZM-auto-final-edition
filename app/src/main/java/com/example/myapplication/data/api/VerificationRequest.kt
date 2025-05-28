package com.example.myapplication.data.api

/**
 * Request body for email verification (OTP)
 */
data class VerificationRequest(
    val verificationCode: String
) 