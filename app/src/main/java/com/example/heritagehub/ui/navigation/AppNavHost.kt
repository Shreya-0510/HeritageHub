@file:Suppress("EXPERIMENTAL_API_USAGE")
package com.example.heritagehub.ui.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heritagehub.ui.screens.artisan.AddArtworkScreen
import com.example.heritagehub.ui.screens.artisan.ArtisanDashboardScreen
import com.example.heritagehub.ui.screens.artisan.ArtisanProfileScreen
import com.example.heritagehub.ui.screens.artisan.ManageProfileScreen
import com.example.heritagehub.ui.screens.auth.ForgotPasswordScreen
import com.example.heritagehub.ui.screens.auth.LoginScreen
import com.example.heritagehub.ui.screens.auth.SignupScreen
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
    data object Signup : Route("signup")
    data object ForgotPassword : Route("forgot_password")
    data object Home : Route("home")
    data object Cart : Route("cart")
    data object Checkout : Route("checkout")
    data object Payment : Route("payment")
    data object Orders : Route("orders")
    data object ArtisanDashboard : Route("artisan_dashboard")
    data object AddArtwork : Route("add_artwork")
    data object ManageProfile : Route("manage_profile")
    data object ArtisanArtworkDetail : Route("artisan_artwork_detail/{artworkId}") {
        fun createRoute(artworkId: String) = "artisan_artwork_detail/$artworkId"
    }
    data object EditArtwork : Route("edit_artwork/{artworkId}") {
        fun createRoute(artworkId: String) = "edit_artwork/$artworkId"
    }
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
    val isAuthChecking = viewModel.isAuthChecking.value
    val isAuthenticated = viewModel.isAuthenticated.value
    val userRole = viewModel.userRole.value

    LaunchedEffect(isAuthenticated, userRole, isAuthChecking) {
        if (!isAuthChecking && isAuthenticated && userRole != null) {
            val destination = if (userRole == "artisan") {
                Route.ArtisanDashboard.path
            } else {
                Route.Home.path
            }
            navController.navigate(destination) {
                popUpTo(Route.Login.path) { inclusive = true }
            }
        }
    }

    if (isAuthChecking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = Route.Login.path
        ) {
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
                        navController.navigate(Route.Signup.path)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Route.ForgotPassword.path)
                    }
                )
            }

            composable(Route.Signup.path) {
                SignupScreen(
                    viewModel = viewModel,
                    onProceed = {
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
                            popUpTo(Route.Signup.path) { inclusive = true }
                        }
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
                val artisanViewModel: ArtisanViewModel = viewModel()
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
                    },
                    onArtworkClick = { artwork ->
                        navController.navigate(Route.ArtisanArtworkDetail.createRoute(artwork.id))
                    },
                    onManageProfileClick = {
                        navController.navigate(Route.ManageProfile.path)
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

            composable(Route.ManageProfile.path) {
                ManageProfileScreen(
                    viewModel = viewModel<ArtisanViewModel>(),
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Route.ArtisanArtworkDetail.path,
                arguments = listOf(
                    androidx.navigation.navArgument("artworkId") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""
                val artisanViewModel: ArtisanViewModel = viewModel()
                val artwork = artisanViewModel.artworks.value.firstOrNull { it.id == artworkId }
                if (artwork != null) {
                    com.example.heritagehub.ui.screens.artisan.ArtisanArtworkDetailScreen(
                        artwork = artwork,
                        onBack = { navController.popBackStack() },
                        onEdit = {
                            navController.navigate(Route.EditArtwork.createRoute(artworkId))
                        }
                    )
                }
            }

            composable(Route.EditArtwork.path,
                arguments = listOf(
                    androidx.navigation.navArgument("artworkId") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""
                val artisanViewModel: ArtisanViewModel = viewModel()
                val artwork = artisanViewModel.artworks.value.firstOrNull { it.id == artworkId }
                if (artwork != null) {
                    com.example.heritagehub.ui.screens.artisan.EditArtworkScreen(
                        artwork = artwork,
                        viewModel = artisanViewModel,
                        onBack = { navController.popBackStack() },
                        onSave = {
                            navController.popBackStack(Route.ArtisanArtworkDetail.createRoute(artworkId), inclusive = true)
                            navController.navigate(Route.ArtisanArtworkDetail.createRoute(artworkId))
                        }
                    )
                }
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
                val artistNameArg = Uri.decode(backStackEntry.arguments?.getString("artistName") ?: "")
                ArtisanProfileScreen(
                    artistName = artistNameArg,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCustomization = { targetArtist ->
                        navController.navigate("customization_request/${Uri.encode(targetArtist)}")
                    }
                )
            }

            composable(Route.CustomizationRequest.routeTemplate) { backStackEntry ->
                val artistNameArg = Uri.decode(backStackEntry.arguments?.getString("artistName") ?: "")
                CustomizationRequestScreen(
                    artistName = artistNameArg,
                    authViewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
