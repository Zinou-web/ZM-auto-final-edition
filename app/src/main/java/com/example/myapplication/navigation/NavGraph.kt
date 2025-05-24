package com.example.myapplication.navigation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.auth.CreateAccountScreen
import com.example.myapplication.ui.screens.auth.NewPasswordScreen
import com.example.myapplication.ui.screens.auth.SignInScreen
import com.example.myapplication.ui.screens.BookingCar.CarDetailsScreen
import com.example.myapplication.ui.screens.BookingCar.GalleryScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.home.NotificationScreen
import com.example.myapplication.ui.screens.bookings.CompletedBookingsScreen
import com.example.myapplication.ui.screens.bookings.MyBookingsScreen
import com.example.myapplication.ui.screens.payment.BillScreen
import com.example.myapplication.ui.screens.bookings.CancelationScreen
import com.example.myapplication.ui.screens.payment.EdahabiaScreen
import com.example.myapplication.ui.screens.payment.FavoriteScreen
import com.example.myapplication.ui.screens.payment.PaymentDoneScreen
import com.example.myapplication.ui.screens.payment.PaymentMethodScreen
import com.example.myapplication.ui.screens.payment.PaymentPending
import com.example.myapplication.ui.screens.payment.UnsuccessfulPaymentScreen
import com.example.myapplication.ui.screens.profile.HelpCenterScreen
import com.example.myapplication.ui.screens.profile.NotificationSettingsScreen
import com.example.myapplication.ui.screens.profile.PrivacyPolicyScreen
import com.example.myapplication.ui.screens.profile.ProfileScreen
import com.example.myapplication.ui.screens.profile.UpdateProfileLocationScreen
import com.example.myapplication.ui.screens.profile.UpdateProfileScreen
import com.example.myapplication.ui.screens.profile.logoutScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen
import com.example.myapplication.ui.screens.welcome.WelcomeScreen
import com.example.myapplication.ui.screens.welcome.SecondScreen
import com.example.myapplication.ui.screens.welcome.ThirdScreen
import com.example.myapplication.ui.screens.splashscreen.SplashScreenSequence
import com.example.myapplication.ui.screens.auth.CompleteProfileScreen
import com.example.myapplication.ui.screens.BookingCar.CarBookingScreen
import com.example.myapplication.ui.screens.password.ChangePasswordScreen
import com.example.myapplication.ui.screens.auth.OTPVerificationScreen
import com.example.myapplication.ui.screens.auth.ForgotPasswordScreen
import com.example.myapplication.ui.screens.auth.ResetPasswordScreen
import com.example.myapplication.ui.screens.home.CompleteYourBookingScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.preference.AuthPreferenceManager
import com.example.myapplication.ui.screens.home.BookingViewModel

/**
 * Enum class that contains all the possible screens in our app
 * This makes it easier to manage screen routes and prevents typos
 */
enum class Screen {
    // Splash Screen Sequence
    SplashSequence,

    // Onboarding Screens
    Welcome,
    Second,
    Third,
    
    // Authentication Screens
    SignIn,
    CreateAccount,
    ForgotPassword,
    ResetPassword,
    NewPassword,
    CompleteProfile,
    OTPVerification,
    
    // Main Screens
    Home,
    CarDetails,
    Filter,
    Notification,
    Profile,
    Gallery,
    
    // Booking Screens
    MyBooking,
    CompletedBooking,
    CompleteYourBooking,
    CarBooking,
    
    // Favorite Screen
    Favorite,
    
    // Settings Screens
    Settings,
    NotificationSettings,
    PasswordManager,
    
    // Payment Screens
    PaymentMethod,
    Edahabia,
    PaymentDone,
    PaymentPending,
    UnsuccessfulPayment,
    Bill,
    Cancelation,
    
    // Profile Screens
    HelpCenter,
    PrivacyPolicy,
    Logout,
    ProfileGeneral,
    ProfileLocation
}

/**
 * The main navigation graph of the application
 * This sets up all possible navigation paths between screens
 *
 * @param navController The navigation controller that handles the navigation
 * @param startDestination The screen to show when the app first launches
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.SplashSequence
) {
    // Add global navigation logging
    navController.addOnDestinationChangedListener { _, destination, _ ->
        Log.d("Navigation", "Navigated to: ${destination.route}")
    }

    // Get the context for creating the AuthPreferenceManager
    val context = LocalContext.current
    
    // Create a shared CarViewModel that will be used by both Home and Filter screens
    val sharedCarViewModel = viewModel<com.example.myapplication.ui.screens.home.CarViewModel>()
    
    // Create a shared BookingViewModel for the booking flow
    val sharedBookingViewModel = viewModel<BookingViewModel>()
    
    NavHost(
        navController = navController,
        startDestination = startDestination.name
    ) {
        // Splash Screen Sequence
        composable(Screen.SplashSequence.name) {
            // Return to original flow: Splash → Welcome
            SplashScreenSequence(
                onNavigateToWelcome = { 
                    // Navigate to Welcome screen as originally intended
                    navController.navigate(Screen.Welcome.name)
                }
            )
        }

        // Onboarding Screens
        composable(Screen.Welcome.name) {
            WelcomeScreen(
                onNextClick = { 
                    // Navigate to Second screen as originally intended
                    navController.navigate(Screen.Second.name)
                }
            )
        }

        composable(Screen.Second.name) {
            SecondScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.Third.name) },
                onSkipClick = { navController.navigate(Screen.SignIn.name) }
            )
        }

        composable(Screen.Third.name) {
            ThirdScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = { 
                    Log.d("Navigation", "ThirdScreen: Next button clicked, navigating to SignIn")
                    navController.navigate(Screen.SignIn.name) {
                        // Clear back stack up to Third screen
                        popUpTo(Screen.Third.name) { inclusive = true }
                    }
                }
            )
        }

        // Authentication Screens
        composable(Screen.SignIn.name) {
            SignInScreen(
                onNavigateToRegister = { navController.navigate(Screen.CreateAccount.name) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.name) },
                onSignInSuccess = { navController.navigateAndClear(Screen.Home.name) }
            )
        }

        composable(Screen.CreateAccount.name) {
            CreateAccountScreen(
                onSignInClick = { navController.navigate(Screen.SignIn.name) },
                onCreateAccountSuccess = { 
                    // Add debug logging
                    Log.d("NavGraph", "Navigating to OTP verification after successful registration")
                    // Navigate to OTP verification and remove CreateAccount from back stack
                    navController.navigate("${Screen.OTPVerification.name}?fromForgotPassword=false") {
                        popUpTo(Screen.CreateAccount.name) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.name) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onResetSent = { 
                    // Navigate to reset password screen
                    navController.navigate("${Screen.ResetPassword.name}") {
                        popUpTo(Screen.ForgotPassword.name) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Screen.ResetPassword.name}?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            
            ResetPasswordScreen(
                email = email,
                onBack = { navController.popBackStack() },
                onResetSuccess = { 
                    // Navigate to sign in screen after successful password reset
                    navController.navigate(Screen.SignIn.name) {
                        popUpTo(Screen.ResetPassword.name) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NewPassword.name) {
            NewPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onPasswordResetSuccess = { navController.navigate(Screen.SignIn.name) }
            )
        }

        composable(Screen.CompleteProfile.name) {
            CompleteProfileScreen(
                onBackClick = { navController.popBackStack() },
                onProfileCompleted = { navController.navigateAndClear(Screen.Home.name) }
            )
        }

        composable(
            route = "${Screen.OTPVerification.name}?fromForgotPassword={fromForgotPassword}",
            arguments = listOf(
                navArgument("fromForgotPassword") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val fromForgotPassword = backStackEntry.arguments?.getBoolean("fromForgotPassword") ?: false
            
            OTPVerificationScreen(
                onBackClick = { navController.popBackStack() },
                onVerifySuccess = { 
                    if (fromForgotPassword) {
                        navController.navigate(Screen.NewPassword.name)
                    } else {
                        navController.navigate(Screen.CompleteProfile.name)
                    }
                },
                fromForgotPassword = fromForgotPassword
            )
        }

        // Main Screens
        composable(Screen.Home.name) {
            HomeScreen(
                viewModel = sharedCarViewModel,
                onCarClick = { carId -> navController.navigate("${Screen.CarDetails.name}/$carId") },
                onProfileClick = { navController.navigate(Screen.Profile.name) },
                onFavoriteClick = { navController.navigate(Screen.Favorite.name) },
                onNotificationClick = { navController.navigate(Screen.Notification.name) },
                onCatalogClick = { navController.navigate(Screen.MyBooking.name) }
            )
        }

        composable(
            route = "${Screen.CarDetails.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailsScreen(
                carId = carId,
                onBackPressed = { navController.popBackStack() },
                onGalleryClick = { navController.navigate("${Screen.Gallery.name}/$carId") },
                onBookNowClick = { 
                    Log.d("NavGraph", "CarDetailsScreen: Navigating to CarBookingScreen with carId: $carId")
                    navController.navigate("${Screen.CarBooking.name}/$carId") // Pass carId to CarBookingScreen
                }
            )
        }

        // Filter Screen - no longer needed since we're handling filters in the HomeScreen
        // composable(Screen.Filter.name) {
        //     Filter(
        //         viewModel = sharedCarViewModel,
        //         onBackClick = { navController.popBackStack() }
        //     )
        // }

        composable(Screen.Notification.name) {
            NotificationScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.Gallery.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            GalleryScreen(
                carId = carId,
                onBackPressed = { navController.popBackStack() },
                onAboutClick = { 
                    if (carId != null) {
                        navController.navigate("${Screen.CarDetails.name}/$carId") {
                            popUpTo("${Screen.Gallery.name}/$carId") { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onBookNowClick = { 
                    Log.d("NavGraph", "GalleryScreen: Navigating to CarBookingScreen with carId: $carId")
                    if (carId != null) {
                        navController.navigate("${Screen.CarBooking.name}/$carId") // Pass carId to CarBookingScreen
                    } else {
                        navController.navigate(Screen.CarBooking.name)
                    }
                }
            )
        }

        composable(Screen.Profile.name) {
            ProfileScreen(
                navController = navController,
                onHomeClick = { navController.navigateAndClear(Screen.Home.name) },
                onBookingsClick = { navController.navigate(Screen.MyBooking.name) },
                onFavoriteClick = { navController.navigate(Screen.Favorite.name) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Booking Screens
        composable(Screen.MyBooking.name) {
            MyBookingsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateAndClear(Screen.Home.name) },
                onFavoriteClick = { navController.navigate(Screen.Favorite.name) },
                onProfileClick = { navController.navigate(Screen.Profile.name) },
                onCompletedTabClick = { navController.navigate(Screen.CompletedBooking.name) }
            )
        }

        composable(Screen.CompletedBooking.name) {
            CompletedBookingsScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateAndClear(Screen.Home.name) },
                onMyBookingsClick = { navController.navigate(Screen.MyBooking.name) },
                onFavoriteClick = { navController.navigate(Screen.Favorite.name) },
                onProfileClick = { navController.navigate(Screen.Profile.name) },
                onUpcomingTabClick = { navController.navigate(Screen.MyBooking.name) },
                onRebookClick = { 
                    navController.navigate("${Screen.CarDetails.name}/rebook") 
                }
            )
        }

        // Favorite Screen
        composable(Screen.Favorite.name) {
            FavoriteScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.navigateAndClear(Screen.Home.name) },
                onBookingsClick = { navController.navigate(Screen.MyBooking.name) },
                onProfileClick = { navController.navigate(Screen.Profile.name) },
                onCarClick = { carId -> 
                    navController.navigate("${Screen.CarDetails.name}/$carId")
                }
            )
        }

        // Payment Screens
        composable(Screen.PaymentMethod.name) {
            PaymentMethodScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.PaymentPending.name) },
                onEdahabiaClick = { navController.navigate(Screen.Edahabia.name) }
            )
        }

        composable(Screen.Edahabia.name) {
            EdahabiaScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.Bill.name) }
            )
        }

        composable(Screen.Bill.name) {
            BillScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { 
                    // On confirm payment, navigate to payment done screen
                    // In a real app, this would check payment success/failure
                    navController.navigate(Screen.PaymentDone.name)
                    // To show unsuccessful payment in case of failure:
                    // navController.navigate(Screen.UnsuccessfulPayment.name)
                },
                viewModel = sharedBookingViewModel
            )
        }

        composable(Screen.Cancelation.name) {
            CancelationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Profile Screens
        composable(Screen.Settings.name) {
            SettingsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificationSettings.name) {
            NotificationSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.HelpCenter.name) {
            HelpCenterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.PrivacyPolicy.name) {
            PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Logout.name) {
            logoutScreen(
                navController = navController,
                onHomeClick = { navController.navigateAndClear(Screen.Home.name) },
                onBookingsClick = { navController.navigate(Screen.MyBooking.name) },
                onFavoriteClick = { navController.navigate(Screen.Favorite.name) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileGeneral.name) {
            UpdateProfileScreen(
                navController = navController
            )
        }

        composable(Screen.ProfileLocation.name) {
            UpdateProfileLocationScreen(
                navController = navController
            )
        }

        composable(Screen.PasswordManager.name) {
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() },
                onPasswordChangeSuccess = { navController.popBackStack() }
            )
        }

        // CarBookingScreen route (with/without driver selection) - updated to accept carId
        composable(
            route = "${Screen.CarBooking.name}/{carId}",
            arguments = listOf(navArgument("carId") { 
                type = NavType.StringType 
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            
            CarBookingScreen(
                carId = carId,
                onBackPressed = { navController.popBackStack() },
                onContinue = {
                    // Navigate to CompleteYourBookingScreen now
                    Log.d("NavGraph", "CarBookingScreen: Continue clicked, navigating to CompleteYourBooking")
                    navController.navigate(Screen.CompleteYourBooking.name)
                },
                bookingViewModel = sharedBookingViewModel
            )
        }
        
        // Add the CompleteYourBookingScreen route back
        composable(Screen.CompleteYourBooking.name) {
            CompleteYourBookingScreen(
                onBackPressed = { navController.popBackStack() },
                onContinue = { 
                    // Navigate to payment method screen
                    Log.d("NavGraph", "CompleteYourBookingScreen: Continue clicked, navigating to PaymentMethod")
                    navController.navigate(Screen.PaymentMethod.name)
                },
                bookingViewModel = sharedBookingViewModel
            )
        }

        composable(Screen.PaymentPending.name) {
            PaymentPending(
                onBackToMainClick = { navController.navigateAndClear(Screen.Home.name) }
            )
        }

        composable(Screen.PaymentDone.name) {
            PaymentDoneScreen(
                onBackToMainClick = { navController.navigateAndClear(Screen.Home.name) }
            )
        }

        composable(Screen.UnsuccessfulPayment.name) {
            UnsuccessfulPaymentScreen(
                onBackClick = { navController.popBackStack() },
                onTryAgainClick = { navController.navigate(Screen.PaymentMethod.name) }
            )
        }
    }
}

/**
 * Extension function to help with navigation and clearing the back stack
 * Use this when you want to navigate to a screen and remove all previous screens from history
 */
fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(0) {
            inclusive = true
        }
    }
} 