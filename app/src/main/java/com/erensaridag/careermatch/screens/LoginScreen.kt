package com.erensaridag.careermatch.screens

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) } // true = Login, false = Register
    var showForgotPassword by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
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
                            onClick = { isLogin = true },
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
                            onClick = { isLogin = false },
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

                    Spacer(modifier = Modifier.height(24.dp))

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
                                .height(90.dp)
                                .clickable { selectedTab = 0 },
                            shape = RoundedCornerShape(16.dp),
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
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🎓",
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Student",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) Color.White else Color(0xFF666666)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clickable { selectedTab = 1 },
                            shape = RoundedCornerShape(16.dp),
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
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🏢",
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Company",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) Color.White else Color(0xFF666666)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        singleLine = true
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
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }

                            Text(
                                text = "Forgot Password?",
                                fontSize = 11.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    showForgotPassword = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login/Register Button
                    Button(
                        onClick = {
                            // Seçili tab'a göre user type belirlenir
                            val userType = if (selectedTab == 0) "Student" else "Company"

                            // Email ve password kontrolü
                            if (email.isNotBlank() && password.isNotBlank()) {
                                // Student seçiliyse -> Student ekranı
                                // Company seçiliyse -> Company ekranı
                                onLoginSuccess(userType)
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
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(
                            text = if (isLogin) "Sign In" else "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Social Login Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // respect
            Text(
                text = "Made By Khein",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        // Forgot Password Dialog
        if (showForgotPassword) {
            AlertDialog(
                onDismissRequest = { showForgotPassword = false },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        Text("Enter your email address and we'll send you a link to reset your password.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showForgotPassword = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Send Reset Link")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPassword = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}