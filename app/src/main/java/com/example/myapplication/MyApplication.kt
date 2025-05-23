package com.example.myapplication

import android.app.Application
import android.content.Context

/**
 * Application class that provides a static instance reference
 * for components that need application context but don't have
 * direct access to it.
 */
class MyApplication : RentalCarApp() {
    
    companion object {
        lateinit var instance: MyApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
} 