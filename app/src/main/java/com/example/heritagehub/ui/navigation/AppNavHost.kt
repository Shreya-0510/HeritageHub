@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heritagehub.ui.screens.artisan.AddArtworkScreen
import com.example.heritagehub.ui.screens.artisan.ArtisanDashboardScreen
import com.example.heritagehub.ui.screens.artisan.ArtisanProfileScreen
import com.example.heritagehub.ui.screens.auth.ForgotPasswordScreen
import com.example.heritagehub.ui.screens.auth.LoginScreen
import com.example.heritagehub.ui.screens.auth.SignupInitialScreen
import com.example.heritagehub.ui.screens.auth.SignupMethodSelectionScreen
import com.example.heritagehub.ui.screens.auth.SignupEmailPasswordScreen
import com.example.heritagehub.ui.screens.auth.SignupMobileOtpScreen
import com.example.heritagehub.ui.screens.auth.SignupGoogleScreen
import com.example.heritagehub.ui.screens.customization.CustomizationRequestScreen
import com.example.heritagehub.ui.screens.detail.ArtworkDetailScreen
import com.example.heritagehub.ui.screens.home.HomeScreen
import com.example.heritagehub.ui.screens.cart.CartScreen
import com.example.heritagehub.ui.screens.cart.CheckoutScreen
import com.example.heritagehub.ui.screens.cart.PaymentScreen
import com.example.heritagehub.ui.screens.orders.OrdersScreen
import com.example.heritagehub.viewmodel.ArtisanViewModel
import com.example.heritagehub.viewmodel.AuthViewModel

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object SignupInitial : Route("signup_initial")
    data object SignupMethodSelection : Route("signup_method_selection/{username}/{email}") {
        fun createRoute(username: String, email: String) = "signup_method_selection/$username/$email"
    }
    data object SignupEmailPassword : Route("signup_email_password/{username}/{email}/{role}") {
        fun createRoute(username: String, email: String, role: String = "user") = "signup_email_password/$username/$email/$role"
    }
    data object SignupMobileOtp : Route("signup_mobile_otp/{username}/{email}/{role}") {
        fun createRoute(username: String, email: String, role: String) = "signup_mobile_otp/$username/$email/$role"
    }
    data object SignupGoogle : Route("signup_google/{username}/{email}/{role}") {
        fun createRoute(username: String, email: String, role: String) = "signup_google/$username/$email/$role"
    }
    data object ForgotPassword : Route("forgot_password")
    data object Home : Route("home")
    data object Cart : Route("cart")
    data object Checkout : Route("checkout")
    data object Payment : Route("payment")
    data object Orders : Route("orders")
    data object ArtisanDashboard : Route("artisan_dashboard")
    data object AddArtwork : Route("add_artwork")
    data class ArtworkDetail(val artworkId: String) : Route("artwork_detail/$artworkId") {
        companion object {
            const val routeTemplate = "artwork_detail/{artworkId}"
        }
    }
    data class ArtisanProfile(val artistName: String) : Route("artisan_profile/$artistName") {
        companion object {
            const val routeTemplate = "artisan_profile/{artistName}"
        }
    }
    data class CustomizationRequest(val artistName: String) : Route("customization_request/$artistName") {
        companion object {
            const val routeTemplate = "customization_request/{artistName}"
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: AuthViewModel = viewModel(),
    context: Context? = null
) {
    LaunchedEffect(Unit) {
        if (context != null) {
            viewModel.checkAuthStatus(context)
        }
        if (viewModel.isAuthenticated.value) {
            // Route based on user role
            val role = viewModel.userRole.value
            val destination = if (role == "artisan") {
                Route.ArtisanDashboard.path
            } else {
                Route.Home.path
            }
            navController.navigate(destination) {
                popUpTo(Route.Login.path) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Login.path
    ) {
        // ==================== LOGIN & SIGNUP ====================

        composable(Route.Login.path) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    val role = viewModel.userRole.value
                    val destination = if (role == "artisan") {
                        Route.ArtisanDashboard.path
                    } else {
                        Route.Home.path
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Route.SignupInitial.path)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Route.ForgotPassword.path)
                }
            )
        }

        composable(Route.SignupInitial.path) {
            SignupInitialScreen(
                viewModel = viewModel,
                onProceed = { username, email ->
                    // Role is now set in viewModel during signup
                    val role = viewModel.userRole.value
                    val destination = if (role == "artisan") {
                        Route.ArtisanDashboard.path
                    } else {
                        Route.Home.path
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.SignupInitial.path) { inclusive = true }
                    }
                },
                onContinueWithPhone = { username, email, role ->
                    navController.navigate(Route.SignupMobileOtp.createRoute(username, email, role))
                },
                onContinueWithGoogle = { username, email, role ->
                    navController.navigate(Route.SignupGoogle.createRoute(username, email, role))
                }
            )
        }

        composable(
            Route.SignupMethodSelection.path,
            arguments = listOf(
                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""

            SignupMethodSelectionScreen(
                username = username,
                email = email,
                onSelectEmailPassword = {
                    navController.navigate(Route.SignupEmailPassword.createRoute(username, email, "user"))
                },
                onSelectMobileOtp = {
                    navController.navigate(Route.SignupMobileOtp.createRoute(username, email, "user"))
                },
                onSelectGoogle = {
                    navController.navigate(Route.SignupGoogle.createRoute(username, email, "user"))
                },
                onBackToInitial = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Route.SignupEmailPassword.path,
            arguments = listOf(
                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("role") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "user"

            SignupEmailPasswordScreen(
                viewModel = viewModel,
                username = username,
                email = email,
                role = role,
                onSignupSuccess = {
                    val role = viewModel.userRole.value
                    val destination = if (role == "artisan") {
                        Route.ArtisanDashboard.path
                    } else {
                        Route.Home.path
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onBackToMethodSelection = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Route.SignupMobileOtp.path,
            arguments = listOf(
                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("role") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "user"

            SignupMobileOtpScreen(
                viewModel = viewModel,
                username = username,
                email = email,
                role = role,
                onSignupSuccess = {
                    val roleFromViewModel = viewModel.userRole.value ?: role
                    val destination = if (roleFromViewModel == "artisan") {
                        Route.ArtisanDashboard.path
                    } else {
                        Route.Home.path
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onBackToMethodSelection = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Route.SignupGoogle.path,
            arguments = listOf(
                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("role") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "user"

            SignupGoogleScreen(
                viewModel = viewModel,
                username = username,
                email = email,
                role = role,
                onSignupSuccess = {
                    val roleFromViewModel = viewModel.userRole.value ?: role
                    val destination = if (roleFromViewModel == "artisan") {
                        Route.ArtisanDashboard.path
                    } else {
                        Route.Home.path
                    }
                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onBackToMethodSelection = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.ForgotPassword.path) {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.ForgotPassword.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Home.path) {
            HomeScreen(
                viewModel = viewModel,
                context = context,
                onLogout = {
                    if (context != null) {
                        viewModel.logout(context)
                    }
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                },
                onNavigateToDetail = { artworkId ->
                    navController.navigate("artwork_detail/$artworkId")
                },
                onNavigateToProfile = { artistName ->
                    navController.navigate("artisan_profile/${Uri.encode(artistName)}")
                },
                onNavigateToCart = {
                    navController.navigate(Route.Cart.path)
                },
                onNavigateToOrders = {
                    navController.navigate(Route.Orders.path)
                }
            )
        }

        composable(Route.Cart.path) {
            CartScreen(
                onBack = {
                    navController.popBackStack()
                },
                onCheckout = {
                    navController.navigate(Route.Checkout.path)
                },
                onContinueShopping = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Home.path) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Route.Checkout.path) {
            CheckoutScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPayment = {
                    navController.navigate(Route.Payment.path)
                },
                onOrderPlaced = {
                    navController.navigate(Route.Orders.path) {
                        popUpTo(Route.Home.path)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Route.Payment.path) {
            PaymentScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.Orders.path) {
            OrdersScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Route.ArtisanDashboard.path) {
            ArtisanDashboardScreen(
                viewModel = viewModel,
                context = context,
                onLogout = {
                    if (context != null) {
                        viewModel.logout(context)
                    }
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.ArtisanDashboard.path) { inclusive = true }
                    }
                },
                onAddArtworkClick = {
                    navController.navigate(Route.AddArtwork.path)
                }
            )
        }

        composable(Route.AddArtwork.path) {
            AddArtworkScreen(
                viewModel = viewModel<ArtisanViewModel>(),
                authViewModel = viewModel<AuthViewModel>(),
                onBack = {
                    navController.popBackStack()
                },
                onArtworkAdded = {
                    navController.navigate(Route.ArtisanDashboard.path) {
                        popUpTo(Route.AddArtwork.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Route.ArtworkDetail.routeTemplate) { backStackEntry ->
            val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""
            ArtworkDetailScreen(
                artworkId = artworkId,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = { artistName ->
                    navController.navigate("artisan_profile/${Uri.encode(artistName)}")
                },
                onNavigateToCustomization = { artistName ->
                    navController.navigate("customization_request/${Uri.encode(artistName)}")
                },
                onNavigateToCart = {
                    navController.navigate(Route.Cart.path)
                }
            )
        }

        composable(Route.ArtisanProfile.routeTemplate) { backStackEntry ->
            val artistName = Uri.decode(backStackEntry.arguments?.getString("artistName") ?: "")
            ArtisanProfileScreen(
                artistName = artistName,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCustomization = { artistName ->
                    navController.navigate("customization_request/${Uri.encode(artistName)}")
                }
            )
        }

        composable(Route.CustomizationRequest.routeTemplate) { backStackEntry ->
            val artistName = Uri.decode(backStackEntry.arguments?.getString("artistName") ?: "")
            CustomizationRequestScreen(
                artistName = artistName,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

