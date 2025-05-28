package com.example.myapplication.di

import com.example.myapplication.BuildConfig
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.api.config.AuthInterceptor
import com.example.myapplication.data.preference.AuthPreferenceManager
import com.example.myapplication.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.net.CookieManager
import java.net.CookiePolicy
import okhttp3.JavaNetCookieJar

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(authPreferenceManager: AuthPreferenceManager): AuthInterceptor {
        return AuthInterceptor(authPreferenceManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        // Setup CookieManager to accept all cookies for session management
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .connectTimeout(60, TimeUnit.SECONDS)  // Increased timeout
            .readTimeout(60, TimeUnit.SECONDS)     // Increased timeout
            .writeTimeout(60, TimeUnit.SECONDS)    // Increased timeout
            .retryOnConnectionFailure(true)        // Retry on connection failure
            .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS)) // Better connection pooling

        // Add network error interceptor
        builder.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                try {
                    val request = chain.request()
                    
                    // Log request details
                    Timber.d("Making request: ${request.url}")
                    
                    // Try to execute the request
                    val response = chain.proceed(request)
                    
                    // Log response details
                    Timber.d("Received response: ${response.code}")
                    
                    return response
                } catch (e: Exception) {
                    // Log the error
                    Timber.e("Network error: ${e.message}")
                    throw e
                }
            }
        })

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // Add auth interceptor
        builder.addInterceptor(authInterceptor)

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): RetrofitClient {
        return RetrofitClient.getInstance(okHttpClient)
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: RetrofitClient): ApiService {
        return retrofit.createService()
    }
}
