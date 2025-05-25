package com.example.myapplication.data.api

import com.example.myapplication.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Base API URL for the Spring Boot backend
    private val BASE_URL = "https://046e-105-105-223-167.ngrok-free.app/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    inline fun <reified T> createService(): T {
        return createService(T::class.java)
    }

    companion object {
        // For direct usage in places where DI is not available
        @Volatile
        private var INSTANCE: RetrofitClient? = null

        fun getInstance(okHttpClient: OkHttpClient): RetrofitClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RetrofitClient(okHttpClient).also { INSTANCE = it }
            }
        }
    }
}

// Extension function to create API service
inline fun <reified T> RetrofitClient.createApiService(): T {
    return this.createService()
}
