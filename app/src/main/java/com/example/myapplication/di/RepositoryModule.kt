package com.example.myapplication.di

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.preference.AuthPreferenceManager
import com.example.myapplication.data.repository.*
import com.example.myapplication.utils.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // For backward compatibility, we keep the old repository implementation
    @Singleton
    @Binds
    abstract fun provideUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Singleton
    @Binds
    abstract fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Singleton
    @Binds
    abstract fun provideCarRepository(
        carRepositoryImpl: CarRepositoryImpl
    ): CarRepository
    
    @Singleton
    @Binds
    abstract fun provideReservationRepository(
        reservationRepositoryImpl: ReservationRepositoryImpl
    ): ReservationRepository
    
    @Singleton
    @Binds
    abstract fun provideFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
}
