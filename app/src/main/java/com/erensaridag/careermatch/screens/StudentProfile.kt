package com.erensaridag.careermatch.screens
// File that creates the student profile screen

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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erensaridag.careermatch.firebase.AuthManager
import com.erensaridag.careermatch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StudentProfile(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }

    // Variables that hold user information
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var cvUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Loading user data from Firebase
    LaunchedEffect(Unit) {
        scope.launch {
            val userId = authManager.getCurrentUser()?.uid
            if (userId != null) {
                val result = authManager.getUserData(userId)
                result.fold(
                    onSuccess = { user ->
                        name = user["name"] as? String ?: ""
                        email = user["email"] as? String ?: ""
                        phone = user["phone"] as? String ?: ""
                        university = user["university"] as? String ?: ""
                        major = user["major"] as? String ?: ""
                        graduationYear = user["graduationYear"] as? String ?: ""
                        cvUrl = user["cvUrl"] as? String ?: ""
                    },
                    onFailure = {
                        Toast.makeText(context, "Loading failed", Toast.LENGTH_SHORT).show()
                    }
                )
                isLoading = false
            }
        }
    }

    // Background and general page structure
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(PrimaryGradientStart, PrimaryGradientEnd)
                )
            )
    ) {
        Column {
            // Top bar
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("My Profile", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Main card
            Card(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    // Loading indicator
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGradientStart)
                    }
                } else {
                    // Profile content
                    LazyColumn(Modifier.padding(24.dp)) {
                        item {
                            // Avatar and name
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.firstOrNull()?.uppercase() ?: "S",
                                        fontSize = 40.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    name.ifBlank { "Student" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = DarkText
                                )
                                Text(email, fontSize = 14.sp, color = LightText)
                            }
                        }

                        item { 
                            Spacer(Modifier.height(24.dp))
                            Divider(color = DividerColor) 
                            Spacer(Modifier.height(16.dp))
                        }

                        // Information fields
                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Personal Information",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkText
                                )
                                TextButton(onClick = { isEditing = !isEditing }) {
                                    Text(
                                        if (isEditing) "Cancel" else "Edit",
                                        color = PrimaryGradientStart,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        item {
                            ProfileField("Full Name", name, { name = it }, isEditing, Icons.Default.Person)
                        }
                        item {
                            ProfileField("Phone", phone, { phone = it }, isEditing, Icons.Default.Phone)
                        }
                        item { 
                            Spacer(Modifier.height(16.dp))
                            Divider(color = DividerColor)
                            Spacer(Modifier.height(16.dp))
                        }
                        item { 
                            Text(
                                "Education",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DarkText
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        item {
                            ProfileField("University", university, { university = it }, isEditing, Icons.Default.School)
                        }
                        item {
                            ProfileField("Major", major, { major = it }, isEditing, Icons.Default.MenuBook)
                        }
                        item {
                            ProfileField("Graduation Year", graduationYear, { graduationYear = it }, isEditing, Icons.Default.CalendarToday)
                        }
                        
                        item { 
                            Spacer(Modifier.height(16.dp))
                            Divider(color = DividerColor)
                            Spacer(Modifier.height(16.dp))
                        }
                        
                        item { 
                            Text(
                                "CV / Resume",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DarkText
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        
                        item {
                            ProfileField(
                                "CV Link (Google Drive, Dropbox, etc.)", 
                                cvUrl, 
                                { cvUrl = it }, 
                                isEditing, 
                                Icons.Default.Description
                            )
                        }

                        // Save button
                        if (isEditing) {
                            item {
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        isSaving = true
                                        scope.launch {
                                            val id = authManager.getCurrentUser()?.uid
                                            if (id != null) {
                                                val updates = mapOf(
                                                    "name" to name,
                                                    "phone" to phone,
                                                    "university" to university,
                                                    "major" to major,
                                                    "graduationYear" to graduationYear,
                                                    "cvUrl" to cvUrl
                                                )
                                                val result = authManager.updateUserData(id, updates)
                                                result.fold(
                                                    onSuccess = {
                                                        isEditing = false
                                                        Toast.makeText(context, "Updated ✅", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onFailure = {
                                                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                                isSaving = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryGradientStart
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSaving) CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    else Text("Save Changes", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper component that draws a single information field
@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(label, fontSize = 12.sp, color = LightText, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            leadingIcon = { 
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) PrimaryGradientStart else LightText
                )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGradientStart,
                unfocusedBorderColor = DividerColor,
                disabledBorderColor = DividerColor,
                focusedTextColor = DarkText,
                unfocusedTextColor = DarkText,
                disabledTextColor = LightText,
                disabledContainerColor = TextFieldBackground.copy(alpha = 0.5f),
                focusedContainerColor = TextFieldBackgroundFocused,
                unfocusedContainerColor = TextFieldBackground,
                focusedLeadingIconColor = PrimaryGradientStart,
                unfocusedLeadingIconColor = LightText,
                cursorColor = PrimaryGradientStart
            )
        )
        Spacer(Modifier.height(12.dp))
    }
}
