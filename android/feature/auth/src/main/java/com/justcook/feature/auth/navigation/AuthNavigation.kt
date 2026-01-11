package com.justcook.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.justcook.feature.auth.ForgotPasswordScreen
import com.justcook.feature.auth.LoginScreen
import com.justcook.feature.auth.RegisterScreen

const val AUTH_GRAPH_ROUTE = "auth"
const val LOGIN_ROUTE = "auth/login"
const val REGISTER_ROUTE = "auth/register"
const val FORGOT_PASSWORD_ROUTE = "auth/forgot-password"

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation(startDestination = LOGIN_ROUTE, route = AUTH_GRAPH_ROUTE) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack(LOGIN_ROUTE, inclusive = true)
                },
                onNavigateToRegister = {
                    navController.navigate(REGISTER_ROUTE)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        composable(REGISTER_ROUTE) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
