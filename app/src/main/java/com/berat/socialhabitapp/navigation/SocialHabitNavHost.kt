package com.berat.socialhabitapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.berat.socialhabitapp.domain.model.AuthState
import com.berat.socialhabitapp.feature.auth.login.LoginRoute
import com.berat.socialhabitapp.feature.auth.register.RegisterRoute
import com.berat.socialhabitapp.feature.home.HomeRoute

@Composable
fun SocialHabitAppNavigation(
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    when (authState) {
        is AuthState.Initializing -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is AuthState.Unauthenticated -> {
            AuthNavHost(modifier = modifier)
        }

        is AuthState.Authenticated -> {
            HomeRoute(
                userEmail = authState.email,
                viewModel = hiltViewModel(),
                modifier = modifier
            )
        }
    }
}

@Composable
fun AuthNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginRoute(
                viewModel = hiltViewModel(),
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterRoute(
                viewModel = hiltViewModel(),
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
