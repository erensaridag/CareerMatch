package com.erensaridag.careermatch

import androidx.compose.runtime.*
import com.erensaridag.careermatch.screens.LoginScreen
import com.erensaridag.careermatch.screens.StudentDashboard
import com.erensaridag.careermatch.screens.CompanyDashboard

@Composable
fun CareerMatchApp() {
    var currentScreen by remember { mutableStateOf("Login") }
    var userType by remember { mutableStateOf("") }

    when (currentScreen) {
        "Login" -> {
            LoginScreen(
                onLoginSuccess = { type ->
                    userType = type
                    currentScreen = "Dashboard"
                }
            )
        }
        "Dashboard" -> {
            // User type'a göre doğru dashboard'u göster
            when (userType) {
                "Student" -> {
                    StudentDashboard(
                        onLogout = {
                            currentScreen = "Login"
                            userType = ""
                        }
                    )
                }
                "Company" -> {
                    CompanyDashboard(
                        onLogout = {
                            currentScreen = "Login"
                            userType = ""
                        }
                    )
                }
            }
        }
    }
}

