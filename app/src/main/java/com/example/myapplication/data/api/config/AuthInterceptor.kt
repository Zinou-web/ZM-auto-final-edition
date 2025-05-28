package com.example.myapplication.data.api.config

import com.example.myapplication.data.preference.AuthPreferenceManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferenceManager: AuthPreferenceManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip authentication for login, register, and other public endpoints
        if (isPublicEndpoint(originalRequest)) {
            return chain.proceed(originalRequest)
        }
        
        // Add auth token to requests that need authentication
        val token = authPreferenceManager.getAuthToken()
        if (token.isNullOrEmpty() || authPreferenceManager.isTokenExpired()) {
            if (authPreferenceManager.isTokenExpired()) {
                Timber.w("Auth token is expired. Proceeding without token.")
            }
            return chain.proceed(originalRequest)
        }
        
        // Create a new request with the Authorization header
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
            
        return chain.proceed(newRequest)
    }
    
    private fun isPublicEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath // Example: /api/users/login
        val method = request.method

        // Exact paths should be relative to the base URL (e.g., /api/users/login)
        return when {
            path == "/api/users/login" && method == "POST" -> true
            path == "/api/users/register" && method == "POST" -> true
            path == "/api/users/oauth2/check-email" && method == "GET" -> true // from ApiService.kt, assuming GET
            path == "/api/users/oauth2/redirect" && method == "GET" -> true    // from ApiService.kt, assuming GET
            path == "/api/users/password-reset/request" && method == "POST" -> true
            path == "/api/users/password-reset/verify" && method == "POST" -> true
            
            // For /api/cars and its subpaths, if they are GET and public
            // This covers /api/cars, /api/cars/paged, /api/cars/available/paged, /api/cars/brand/{brand}/paged, etc.
            // and /api/cars/{id} as getCarById does not take a token in ApiService.kt
            path.startsWith("/api/cars") && method == "GET" -> true
            
            // Add any other specific public endpoints here
            // e.g. if there was a public /api/app-config endpoint
            // path == "/api/app-config" && method == "GET" -> true 

            else -> false
        }
    }
} 