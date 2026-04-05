package com.example.heritagehub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.heritagehub.ui.screens.auth.LoginScreen
import com.example.heritagehub.ui.screens.auth.SignupScreen
import com.example.heritagehub.ui.screens.home.HomeScreen
import com.example.heritagehub.viewmodel.AuthViewModel

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Signup : Route("signup")
    data object Home : Route("home")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: AuthViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus()
        if (viewModel.isAuthenticated.value) {
            navController.navigate(Route.Home.path) {
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
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Signup.path) {
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    navController.navigate(Route.Home.path) {
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
                }
            )
        }
    }
}

