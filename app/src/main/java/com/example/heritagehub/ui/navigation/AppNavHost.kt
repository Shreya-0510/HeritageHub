package com.example.heritagehub.ui.navigation

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
import com.example.heritagehub.ui.screens.auth.LoginScreen
import com.example.heritagehub.ui.screens.auth.SignupScreen
import com.example.heritagehub.ui.screens.customization.CustomizationRequestScreen
import com.example.heritagehub.ui.screens.detail.ArtworkDetailScreen
import com.example.heritagehub.ui.screens.home.HomeScreen
import com.example.heritagehub.viewmodel.ArtisanViewModel
import com.example.heritagehub.viewmodel.AuthViewModel

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Signup : Route("signup")
    data object Home : Route("home")
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
    viewModel: AuthViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus()
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
        composable(Route.Login.path) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignup = {
                    navController.navigate(Route.Signup.path)
                },
                onLoginSuccess = {
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
            )
        }

        composable(Route.Signup.path) {
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
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
            )
        }

        composable(Route.Home.path) {
            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                },
                onNavigateToDetail = { artworkId ->
                    navController.navigate("artwork_detail/$artworkId")
                },
                onNavigateToProfile = { artistName ->
                    navController.navigate("artisan_profile/$artistName")
                }
            )
        }

        composable(Route.ArtisanDashboard.path) {
            ArtisanDashboardScreen(
                viewModel = viewModel,
                onLogout = {
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
                onBack = {
                    navController.popBackStack()
                },
                onArtworkAdded = {
                    navController.popBackStack()
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
                    navController.navigate("artisan_profile/$artistName")
                },
                onNavigateToCustomization = { artistName ->
                    navController.navigate("customization_request/$artistName")
                }
            )
        }

        composable(Route.ArtisanProfile.routeTemplate) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            ArtisanProfileScreen(
                artistName = artistName,
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToCustomization = { artistName ->
                    navController.navigate("customization_request/$artistName")
                }
            )
        }

        composable(Route.CustomizationRequest.routeTemplate) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            CustomizationRequestScreen(
                artistName = artistName,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

