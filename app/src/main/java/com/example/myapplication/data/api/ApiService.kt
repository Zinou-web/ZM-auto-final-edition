package com.example.myapplication.data.api

import com.example.myapplication.data.model.Address
import com.example.myapplication.data.model.Car
import com.example.myapplication.data.model.DrivingLicense
import com.example.myapplication.data.model.Reservation
import com.example.myapplication.data.model.User
import okhttp3.MultipartBody
import retrofit2.http.*
import retrofit2.Response
import com.example.myapplication.data.api.NetworkResponse

interface ApiService {
    // User Profile
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long, @Header("Authorization") token: String): User

    @GET("api/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): NetworkResponse<User>

    // Facebook OAuth
    @GET("api/users/oauth2/redirect")
    suspend fun handleOAuth2Redirect(
        @Query("token") token: String, 
        @Query("userId") userId: Long
    ): NetworkResponse<AuthResponse>

    @GET("api/users/oauth2/check-email")
    suspend fun checkEmailExists(@Query("email") email: String): Map<String, Boolean>

    @PUT("api/users/{id}")
    suspend fun updateUserProfile(
        @Path("id") id: Long,
        @Body request: UpdateProfileRequest,
        @Header("Authorization") token: String
    ): User

    // Authentication
    @POST("api/users/login")
    suspend fun login(@Body request: LoginRequest): NetworkResponse<AuthResponse>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): NetworkResponse<AuthResponse>

    /**
     * Verify email using the OTP code sent to the user's email
     */
    @POST("api/users/{userId}/verify-email")
    suspend fun verifyEmail(
        @Path("userId") userId: Long,
        @Body request: VerificationRequest
    ): NetworkResponse<Void>

    /**
     * Resend the email verification code (OTP)
     */
    @POST("api/users/{userId}/resend-otp")
    suspend fun resendOtp(
        @Path("userId") userId: Long
    ): NetworkResponse<Void>

    @POST("api/users/password-reset/request")
    suspend fun requestPasswordReset(@Query("email") email: String): NetworkResponse<Boolean>

    @POST("api/users/password-reset/verify")
    suspend fun verifyPasswordReset(
        @Query("email") email: String,
        @Query("code") code: String,
        @Query("newPassword") newPassword: String
    ): NetworkResponse<PasswordResetResponse>

    @POST("api/users/logout")
    suspend fun logout(): NetworkResponse<Void>

    @POST("api/users/me/change-password")
    suspend fun changePassword(
        @Query("currentPassword") currentPassword: String,
        @Query("newPassword") newPassword: String,
        @Header("Authorization") token: String
    ): NetworkResponse<Void>

    @DELETE("api/users/{id}")
    suspend fun deleteAccount(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): NetworkResponse<Void>

    // Profile Management
    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): NetworkResponse<ImageUploadResponse>

    // Car Management
    @GET("api/cars")
    suspend fun getAllCars(): List<Car>

    @GET("api/cars/paged")
    suspend fun getAllCarsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id",
        @Query("direction") direction: String = "asc",
        @Query("brand") brand: String? = null,
        @Query("model") model: String? = null,
        @Query("minRating") minRating: Long? = null,
        @Query("maxRating") maxRating: Long? = null,
        @Query("rentalStatus") rentalStatus: String? = null
    ): PagedResponse<Car>

    @GET("api/cars/available/paged")
    suspend fun getAvailableCarsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id"
    ): PagedResponse<Car>

    @GET("api/cars/brand/{brand}/paged")
    suspend fun getCarsByBrandPaged(
        @Path("brand") brand: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id"
    ): PagedResponse<Car>

    @GET("api/cars/model/{model}/paged")
    suspend fun getCarsByModelPaged(
        @Path("model") model: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id"
    ): PagedResponse<Car>

    @GET("api/cars/rating/paged")
    suspend fun getCarsByRatingRangePaged(
        @Query("minRating") minRating: Long,
        @Query("maxRating") maxRating: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id"
    ): PagedResponse<Car>

    @GET("api/cars/{id}")
    suspend fun getCarById(@Path("id") id: Long): Car

    // User Car Browsing
    @GET("users/cars")
    suspend fun getAllAvailableCars(@Header("Authorization") token: String): List<Car>

    @GET("users/cars/brand/{brand}")
    suspend fun getCarsByBrand(
        @Path("brand") brand: String,
        @Header("Authorization") token: String
    ): List<Car>

    @GET("users/cars/model/{model}")
    suspend fun getCarsByModel(
        @Path("model") model: String,
        @Header("Authorization") token: String
    ): List<Car>

    @GET("users/cars/rating")
    suspend fun getCarsByRatingRange(
        @Query("minRating") minRating: Long,
        @Query("maxRating") maxRating: Long,
        @Header("Authorization") token: String
    ): List<Car>

    // Reservation Management
    @GET("api/reservations")
    suspend fun getAllReservations(@Header("Authorization") token: String): List<Reservation>

    @GET("api/reservations/{id}")
    suspend fun getReservationById(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Reservation

    @GET("api/reservations/user/{userId}")
    suspend fun getReservationsByUserId(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): List<Reservation>

    @GET("api/reservations/car/{carId}")
    suspend fun getReservationsByCarId(
        @Path("carId") carId: Long,
        @Header("Authorization") token: String
    ): List<Reservation>

    @GET("api/reservations/status/{status}")
    suspend fun getReservationsByStatus(
        @Path("status") status: String,
        @Header("Authorization") token: String
    ): List<Reservation>

    @POST("api/reservations")
    suspend fun createReservation(
        @Body reservation: ReservationRequest,
        @Header("Authorization") token: String
    ): Reservation

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(
        @Path("id") id: Long,
        @Body reservation: ReservationRequest,
        @Header("Authorization") token: String
    ): Reservation

    @PATCH("api/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") id: Long,
        @Body statusUpdate: ReservationStatusUpdateRequest,
        @Header("Authorization") token: String
    ): Reservation

    @POST("api/reservations/{id}/cancel")
    suspend fun cancelReservation(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Reservation

    @GET("api/users/{userId}/reservations/upcoming")
    suspend fun getUpcomingReservations(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): List<Reservation>

    @GET("api/users/{userId}/reservations/past")
    suspend fun getPastReservations(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String
    ): List<Reservation>

    // Address Management
    @PUT("api/users/{id}/address")
    suspend fun updateAddress(
        @Path("id") id: Long,
        @Body address: Address,
        @Header("Authorization") token: String
    ): Address

    @GET("api/users/{id}/address")
    suspend fun getAddress(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Address

    // Driving License Management
    @PUT("users/{id}/driving-license")
    suspend fun updateDrivingLicense(
        @Path("id") id: Long,
        @Body drivingLicense: DrivingLicense,
        @Header("Authorization") token: String
    ): DrivingLicense

    @GET("users/{id}/driving-license")
    suspend fun getDrivingLicense(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): DrivingLicense

    @Multipart
    @POST("users/{id}/driving-license/image")
    suspend fun uploadDrivingLicenseImage(
        @Path("id") id: Long,
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): ImageUploadResponse

    // Favorites Management
    @GET("users/{id}/favorites")
    suspend fun getFavorites(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): List<Long>

    @POST("users/{id}/favorites/{carId}")
    suspend fun addToFavorites(
        @Path("id") id: Long,
        @Path("carId") carId: Long,
        @Header("Authorization") token: String
    ): List<Long>

    @DELETE("users/{id}/favorites/{carId}")
    suspend fun removeFromFavorites(
        @Path("id") id: Long,
        @Path("carId") carId: Long,
        @Header("Authorization") token: String
    ): List<Long>
} 