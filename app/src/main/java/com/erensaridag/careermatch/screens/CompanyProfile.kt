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
import com.erensaridag.careermatch.ui.theme.*
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
                        Toast.makeText(context, "Profile could not be loaded", Toast.LENGTH_SHORT).show()
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
                        PrimaryGradientStart,
                        PrimaryGradientEnd
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    "Company Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Placeholder for empty space
                Box(modifier = Modifier.size(36.dp))
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGradientStart)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.firstOrNull()?.uppercase() ?: "C",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = name.ifBlank { "Company" },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = email,
                                    fontSize = 13.sp,
                                    color = LightText
                                )
                            }
                        }

                        item {
                            Divider(color = DividerColor)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Company Information",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                TextButton(
                                    onClick = { isEditing = !isEditing },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (isEditing) "Cancel" else "Edit",
                                        fontSize = 13.sp,
                                        color = PrimaryGradientStart,
                                        fontWeight = FontWeight.SemiBold
                                    )
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
                                                        Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryGradientStart
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isSaving
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
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
            color = LightText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) PrimaryGradientStart else LightText,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = DividerColor,
                disabledTextColor = DarkText,
                disabledLeadingIconColor = LightText,
                disabledContainerColor = TextFieldBackground.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGradientStart,
                unfocusedBorderColor = DividerColor,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                focusedContainerColor = TextFieldBackgroundFocused,
                unfocusedContainerColor = TextFieldBackground,
                focusedLeadingIconColor = PrimaryGradientStart,
                unfocusedLeadingIconColor = LightText,
                cursorColor = PrimaryGradientStart
            ),
            singleLine = true,
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}