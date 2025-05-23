package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.CarRepository
import com.example.myapplication.ui.screens.home.CarViewModel
import com.example.myapplication.ui.screens.profile.ProfileViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton
import kotlin.reflect.KClass

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {
    
    @Binds
    @IntoMap
    @HiltViewModelMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel
    
    @Binds
    @IntoMap
    @HiltViewModelMap
    @ViewModelKey(CarViewModel::class)
    abstract fun bindCarViewModel(carViewModel: CarViewModel): ViewModel
    
    // Add other ViewModel bindings here
    // Example:
    // @Binds
    // @IntoMap
    // @ViewModelKey(OtherViewModel::class)
    // abstract fun bindOtherViewModel(otherViewModel: OtherViewModel): ViewModel
}

@Module
@InstallIn(SingletonComponent::class)
object ViewModelFactoryModule {
    @Provides
    @Singleton
    fun provideViewModelFactory(
        creators: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val creator = creators[modelClass] ?: creators.entries.firstOrNull {
                    modelClass.isAssignableFrom(it.key)
                }?.value ?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                
                @Suppress("UNCHECKED_CAST")
                return creator as T
            }
        }
    }
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

typealias HiltViewModelMap = dagger.multibindings.StringKey
