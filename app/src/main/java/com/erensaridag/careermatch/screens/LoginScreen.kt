package com.erensaridag.careermatch.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erensaridag.careermatch.firebase.AuthManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a237e).copy(alpha = 0.9f + animatedOffset * 0.1f),
                        Color(0xFF0d47a1),
                        Color(0xFF01579b)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Text(
                    text = "💼",
                    fontSize = 42.sp,
                    modifier = Modifier.scale(1f + animatedOffset * 0.1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CareerMatch",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Your future starts here",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login / Register Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFF5F5F5),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                isLogin = true
                                password = ""
                                name = ""
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLogin) Color(0xFF1976D2) else Color.Transparent,
                                contentColor = if (isLogin) Color.White else Color(0xFF666666)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isLogin) 2.dp else 0.dp
                            )
                        ) {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isLogin = false
                                password = ""
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLogin) Color(0xFF1976D2) else Color.Transparent,
                                contentColor = if (!isLogin) Color.White else Color(0xFF666666)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (!isLogin) 2.dp else 0.dp
                            )
                        ) {
                            Text("Register", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // User Type Selector
                    Text(
                        text = "I am a",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable { selectedTab = 0 },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedTab == 0)
                                    Color(0xFF4CAF50)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (selectedTab == 0) 4.dp else 0.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🎓",
                                    fontSize = 23.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Student",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) Color.White else Color(0xFF666666)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedTab == 1)
                                    Color(0xFF2196F3)
                                else
                                    Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (selectedTab == 1) 4.dp else 0.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🏢",
                                    fontSize = 23.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Company",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) Color.White else Color(0xFF666666)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Name Field (only for register)
                    AnimatedVisibility(visible = !isLogin) {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full Name") },
                                placeholder = { Text("Enter your full name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1976D2),
                                    focusedLabelColor = Color(0xFF1976D2),
                                    cursorColor = Color(0xFF1976D2)
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email Address") },
                        placeholder = { Text("you@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2),
                            cursorColor = Color(0xFF1976D2)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Text(
                                    text = if (isPasswordVisible) "👁️" else "🙈",
                                    fontSize = 20.sp
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2),
                            cursorColor = Color(0xFF1976D2)
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me & Forgot Password Row
                    AnimatedVisibility(visible = isLogin) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF1976D2)
                                    )
                                )
                                Text(
                                    "Remember me",
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666)
                                )
                            }

                            Text(
                                text = "Forgot Password?",
                                fontSize = 10.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    showForgotPassword = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Login/Register Button
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (!isLogin && name.isBlank()) {
                                Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            val userType = if (selectedTab == 0) "Student" else "Company"

                            scope.launch {
                                try {
                                    if (isLogin) {
                                        // Login
                                        val result = authManager.signIn(email, password)
                                        result.fold(
                                            onSuccess = { (userId, savedUserType) ->
                                                isLoading = false
                                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess(savedUserType)
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        // Register
                                        val result = authManager.signUp(email, password, name, userType)
                                        result.fold(
                                            onSuccess = { userId ->
                                                isLoading = false
                                                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess(userType)
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                Toast.makeText(context, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (isLogin || name.isNotBlank())
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isLogin) "Sign In" else "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // mbKhein
            Text(
                text = "made by khein",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        // Forgot Password Dialog
        if (showForgotPassword) {
            var resetEmail by remember { mutableStateOf(email) }
            var isResetting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showForgotPassword = false },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        Text("Enter your email address and we'll send you a link to reset your password.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isResetting
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isResetting = true
                            scope.launch {
                                val result = authManager.sendPasswordResetEmail(resetEmail)
                                result.fold(
                                    onSuccess = {
                                        isResetting = false
                                        Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_LONG).show()
                                        showForgotPassword = false
                                    },
                                    onFailure = { error ->
                                        isResetting = false
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        ),
                        enabled = !isResetting
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Send Reset Link")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPassword = false },
                        enabled = !isResetting
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}