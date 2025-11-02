package com.erensaridag.careermatch.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erensaridag.careermatch.firebase.AuthManager
import kotlinx.coroutines.launch

@Composable
fun CompanyProfile(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load company data
    LaunchedEffect(Unit) {
        scope.launch {
            val userId = authManager.getCurrentUser()?.uid
            if (userId != null) {
                val result = authManager.getUserData(userId)
                result.fold(
                    onSuccess = { userData ->
                        name = userData["name"] as? String ?: ""
                        email = userData["email"] as? String ?: ""
                        phone = userData["phone"] as? String ?: ""
                        address = userData["address"] as? String ?: ""
                        website = userData["website"] as? String ?: ""
                        industry = userData["industry"] as? String ?: ""
                        isLoading = false
                    },
                    onFailure = {
                        isLoading = false
                        Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF1976D2),
                        Color(0xFF0D47A1)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Company Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Avatar & Name
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.firstOrNull()?.uppercase() ?: "C",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = name.ifBlank { "Company" },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = email,
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

                        item {
                            Divider(color = Color(0xFFE0E0E0))
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Company Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF333333)
                                )
                                TextButton(onClick = { isEditing = !isEditing }) {
                                    Text(if (isEditing) "Cancel" else "Edit")
                                }
                            }
                        }

                        item {
                            CompanyProfileField(
                                label = "Company Name",
                                value = name,
                                onValueChange = { name = it },
                                enabled = isEditing,
                                icon = Icons.Default.Business
                            )
                        }

                        item {
                            CompanyProfileField(
                                label = "Phone",
                                value = phone,
                                onValueChange = { phone = it },
                                enabled = isEditing,
                                icon = Icons.Default.Phone
                            )
                        }

                        item {
                            CompanyProfileField(
                                label = "Address",
                                value = address,
                                onValueChange = { address = it },
                                enabled = isEditing,
                                icon = Icons.Default.LocationOn
                            )
                        }

                        item {
                            CompanyProfileField(
                                label = "Website",
                                value = website,
                                onValueChange = { website = it },
                                enabled = isEditing,
                                icon = Icons.Default.Language
                            )
                        }

                        item {
                            CompanyProfileField(
                                label = "Industry",
                                value = industry,
                                onValueChange = { industry = it },
                                enabled = isEditing,
                                icon = Icons.Default.Work
                            )
                        }

                        if (isEditing) {
                            item {
                                Button(
                                    onClick = {
                                        isSaving = true
                                        scope.launch {
                                            val userId = authManager.getCurrentUser()?.uid
                                            if (userId != null) {
                                                val updates = mapOf(
                                                    "name" to name,
                                                    "phone" to phone,
                                                    "address" to address,
                                                    "website" to website,
                                                    "industry" to industry
                                                )
                                                val result = authManager.updateUserData(userId, updates)
                                                result.fold(
                                                    onSuccess = {
                                                        isSaving = false
                                                        isEditing = false
                                                        Toast.makeText(context, "Profile updated! ✅", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onFailure = {
                                                        isSaving = false
                                                        Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isSaving
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text("Save Changes", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompanyProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = Color(0xFF2196F3))
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTextColor = Color(0xFF333333),
                disabledLeadingIconColor = Color(0xFF999999),
                focusedBorderColor = Color(0xFF2196F3)
            ),
            singleLine = true
        )
    }
}