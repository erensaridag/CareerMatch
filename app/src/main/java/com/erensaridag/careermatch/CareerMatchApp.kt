package com.erensaridag.careermatch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import com.erensaridag.careermatch.screens.*
import com.erensaridag.careermatch.firebase.AuthManager
import kotlinx.coroutines.launch

@Composable
fun CareerMatchApp() {
    val authManager = remember { AuthManager() }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("Loading") }
    var userType by remember { mutableStateOf("") }

    // Check Firebase session on app start
    LaunchedEffect(Unit) {
        scope.launch {
            if (authManager.isUserLoggedIn()) {
                // User is already logged in, get their type
                val type = authManager.getCurrentUserType()
                if (type != null) {
                    userType = type
                    currentScreen = "Dashboard"
                } else {
                    currentScreen = "Login"
                }
            } else {
                currentScreen = "Login"
            }
        }
    }

    when (currentScreen) {
        "Loading" -> {
            // Show splash or loading screen
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        "Login" -> {
            LoginScreen(
                onLoginSuccess = { type ->
                    userType = type
                    currentScreen = "Dashboard"
                }
            )
        }
        "Dashboard" -> {
            when (userType) {
                "Student" -> {
                    StudentDashboard(
                        onLogout = {
                            authManager.signOut()
                            currentScreen = "Login"
                            userType = ""
                        },
                        onNavigateToProfile = {
                            currentScreen = "StudentProfile"
                        }
                    )
                }
                "Company" -> {
                    CompanyDashboard(
                        onLogout = {
                            authManager.signOut()
                            currentScreen = "Login"
                            userType = ""
                        },
                        onNavigateToProfile = {
                            currentScreen = "CompanyProfile"
                        }
                    )
                }
            }
        }
        "StudentProfile" -> {
            StudentProfile(
                onBack = {
                    currentScreen = "Dashboard"
                }
            )
        }
        "CompanyProfile" -> {
            CompanyProfile(
                onBack = {
                    currentScreen = "Dashboard"
                }
            )
        }
    }
}