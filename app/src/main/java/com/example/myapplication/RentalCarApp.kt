package com.example.myapplication

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.myapplication.data.preference.AuthPreferenceManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
open class RentalCarApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var authPreferenceManager: AuthPreferenceManager
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // MultiDex.install(this) // Uncomment if using multidex
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Enable strict mode in debug builds
        if (BuildConfig.DEBUG) {
            enableStrictMode()
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize app components
        initializeApp()
        
        Timber.d("Application started")
    }
    
    private fun initializeApp() {
        // Initialize any third-party libraries here
        // FirebaseApp.initializeApp(this)
        
        // Check if we need to migrate data from old preferences to new auth preferences
        migratePreferencesToAuth()
        
        // No need to manually initialize WorkManager when implementing Configuration.Provider
        // WorkManager will be initialized automatically using getWorkManagerConfiguration()
    }
    
    private fun migratePreferencesToAuth() {
        // TEMPORARILY DISABLED FOR TESTING AUTH FLOW
        // If user is logged in with old preference system, migrate to new auth system
        // This block will be ineffective as preferenceManager is being removed.
        // Consider deleting this migration logic entirely once old PreferenceManager is gone.
        /*
        if (false && preferenceManager.isLoggedIn && !authPreferenceManager.isLoggedIn()) { // preferenceManager will be undefined
            preferenceManager.authToken?.let { token ->
                authPreferenceManager.saveAuthToken(token)
                preferenceManager.userId?.let { userId ->
                    authPreferenceManager.saveUserId(userId)
                }
                authPreferenceManager.setLoggedIn(true)
                // Set a default expiry time (1 day)
                authPreferenceManager.saveTokenExpiry(86400)
                
                Timber.d("Migrated user authentication from old preferences to secure storage")
            }
        }
        */

        // REMOVED: authPreferenceManager.clearAuthData()
        // REMOVED: Timber.d("Auth data cleared for testing purposes")
        Timber.d("migratePreferencesToAuth called. Migration logic is currently disabled/commented out.")
    }
    
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
