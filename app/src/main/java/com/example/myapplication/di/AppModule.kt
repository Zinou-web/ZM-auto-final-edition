package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.utils.NetworkStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideNetworkStateManager(@ApplicationContext context: Context): NetworkStateManager {
        return NetworkStateManager(context)
    }
}
