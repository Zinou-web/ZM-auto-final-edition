package com.example.myapplication.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.myapplication.R

/**
 * Screens used in [RentalCarApp].
 */
enum class Screen(
    val route: String,
    @StringRes val title: Int? = null,
    @DrawableRes val icon: Int? = null
) {
    // Splash and Onboarding
    SplashScreen("SplashScreen"),
    Splash2("Splash2"),
    Splash3("Splash3"),
    Splash4("Splash4"),
    Splash5("Splash5"),
    Splash6("Splash6"),
    FirstScreen("FirstScreen"),
    SecondScreen("SecondScreen"),
    ThirdScreen("ThirdScreen"),
    
    // Auth Screens
    Login("Login"),
    CreateAccount("CreateAccount"),
    ForgotPassword("ForgotPassword"),
    OTPVerification("OTPVerification"),
    ResetPassword("ResetPassword"),
    NewPassword("NewPassword"),
    CompleteProfile("CompleteProfile"),
    
    // Main Screens
    Home("Home"),
    CarDetails("CarDetails"),
    CarBooking("CarBooking"),
    MyBooking("MyBooking"),
    CompletedBookings("CompletedBookings"),
    Gallery("Gallery"),
    // Filter, // Removed - now handled directly in HomeScreen
    Notification("Notification"),
    Profile("Profile"),
    Setting("Setting"),
    Favorite("Favorite"),
    
    // Profile Screens
    ProfileGeneral("ProfileGeneral"),
    ProfileLocation("ProfileLocation"),
    HelpCenter("HelpCenter"),
    NotificationSetting("NotificationSetting"),
    PasswordManager("PasswordManager"),
    PrivacyPolicy("PrivacyPolicy"),
    Logout("Logout"),
    
    // Payment Screens
    Bill("Bill"),
    Cancelation("Cancelation"),
    CompleteYourBooking("CompleteYourBooking"),
    
    // New Favorites Screen
    Favorites("Favorites"),

    // New EReceipt Screen
    EReceipt("EReceipt")
} 