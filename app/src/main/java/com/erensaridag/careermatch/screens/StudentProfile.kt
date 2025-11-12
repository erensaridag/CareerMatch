package com.erensaridag.careermatch.screens
// Öğrenci profil ekranını oluşturan dosya

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
import kotlinx.coroutines.launch

@Composable
fun StudentProfile(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }

    // Kullanıcı bilgilerini tutan değişkenler
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Kullanıcı verilerini Firebase'den yükleme
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
                    },
                    onFailure = {
                        Toast.makeText(context, "Load failed", Toast.LENGTH_SHORT).show()
                    }
                )
                isLoading = false
            }
        }
    }

    // Arka plan ve genel sayfa yapısı
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFF2196F3))
                )
            )
    ) {
        Column {
            // Üst bar
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("My Profile", fontSize = 22.sp, color = Color.White)
            }

            // Ana kart
            Card(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                if (isLoading) {
                    // Yükleniyor göstergesi
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Profil içeriği
                    LazyColumn(Modifier.padding(24.dp)) {
                        item {
                            // Avatar ve isim
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(Color(0xFF4CAF50), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.firstOrNull()?.uppercase() ?: "S",
                                        fontSize = 40.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(name.ifBlank { "Student" }, fontWeight = FontWeight.Bold)
                                Text(email, fontSize = 13.sp, color = Color.Gray)
                            }
                        }

                        item { Divider() }

                        // Bilgi alanları
                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Personal Info", fontWeight = FontWeight.SemiBold)
                                TextButton(onClick = { isEditing = !isEditing }) {
                                    Text(if (isEditing) "Cancel" else "Edit")
                                }
                            }
                        }

                        item {
                            ProfileField("Full Name", name, { name = it }, isEditing, Icons.Default.Person)
                        }
                        item {
                            ProfileField("Phone", phone, { phone = it }, isEditing, Icons.Default.Phone)
                        }
                        item { Divider() }
                        item { Text("Education", fontWeight = FontWeight.SemiBold) }
                        item {
                            ProfileField("University", university, { university = it }, isEditing, Icons.Default.School)
                        }
                        item {
                            ProfileField("Major", major, { major = it }, isEditing, Icons.Default.MenuBook)
                        }
                        item {
                            ProfileField("Graduation Year", graduationYear, { graduationYear = it }, isEditing, Icons.Default.CalendarToday)
                        }

                        // Kaydet butonu
                        if (isEditing) {
                            item {
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
                                                    "graduationYear" to graduationYear
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
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSaving) CircularProgressIndicator(color = Color.White)
                                    else Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tek bir bilgi alanını çizen yardımcı bileşen
@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50)) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
