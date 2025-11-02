package com.erensaridag.careermatch.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erensaridag.careermatch.firebase.AuthManager
import com.erensaridag.careermatch.firebase.InternshipManager
import com.erensaridag.careermatch.firebase.ApplicationManager
import kotlinx.coroutines.launch

@Composable
fun CompanyDashboard(onLogout: () -> Unit, onNavigateToProfile: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }
    val internshipManager = remember { InternshipManager() }
    val applicationManager = remember { ApplicationManager() }

    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var postedCount by remember { mutableStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    val unreadCount by remember { mutableStateOf(4) }
    var isPosting by remember { mutableStateOf(false) }
    var applicantsCount by remember { mutableStateOf(0) }

    // Load company stats
    LaunchedEffect(Unit) {
        scope.launch {
            authManager.getCurrentUser()?.uid?.let { userId ->
                // Get company name from user data
                val userDataResult = authManager.getUserData(userId)
                userDataResult.fold(
                    onSuccess = { userData ->
                        company = userData["name"] as? String ?: ""
                    },
                    onFailure = { }
                )

                // Get posted internships count
                val internshipsResult = internshipManager.getCompanyInternships(userId)
                internshipsResult.fold(
                    onSuccess = { internships ->
                        postedCount = internships.size
                    },
                    onFailure = { }
                )

                // Get pending applications count
                val applicationsResult = applicationManager.getPendingApplicationsCount(userId)
                applicationsResult.fold(
                    onSuccess = { count ->
                        applicantsCount = count
                    },
                    onFailure = { }
                )
            }
        }
    }

    val handlePost = {
        if (jobTitle.isNotBlank() && company.isNotBlank()) {
            isPosting = true
            scope.launch {
                val userId = authManager.getCurrentUser()?.uid
                android.util.Log.d("CompanyDashboard", "Current user ID: $userId")

                if (userId != null) {
                    android.util.Log.d("CompanyDashboard", "Posting internship: $jobTitle")

                    val result = internshipManager.addInternship(
                        title = jobTitle,
                        company = company,
                        location = location.ifBlank { "Not specified" },
                        duration = duration.ifBlank { "Not specified" },
                        salary = salary.ifBlank { "Negotiable" },
                        description = description.ifBlank { "No description" },
                        companyId = userId
                    )

                    result.fold(
                        onSuccess = { docId ->
                            isPosting = false
                            postedCount++
                            android.util.Log.d("CompanyDashboard", "Success! Doc ID: $docId")
                            Toast.makeText(
                                context,
                                "Internship '${jobTitle}' posted successfully! 🎉",
                                Toast.LENGTH_LONG
                            ).show()

                            // Clear form
                            jobTitle = ""
                            location = ""
                            duration = ""
                            salary = ""
                            description = ""
                        },
                        onFailure = { error ->
                            isPosting = false
                            val errorMsg = error.message ?: "Unknown error"
                            android.util.Log.e("CompanyDashboard", "Failed to post: $errorMsg", error)
                            Toast.makeText(
                                context,
                                "Failed: $errorMsg",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                } else {
                    isPosting = false
                    android.util.Log.e("CompanyDashboard", "User not logged in!")
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(
                context,
                "Please fill in required fields",
                Toast.LENGTH_SHORT
            ).show()
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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CareerMatch",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Welcome Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back! 👋",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Post a new opportunity",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Notification Button with Badge
                                Box {
                                    IconButton(
                                        onClick = { showNotifications = true },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                Color(0xFFF5F5F5),
                                                RoundedCornerShape(50)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = Color(0xFF666666),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Red Badge
                                    if (unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .background(Color(0xFFFF5252), RoundedCornerShape(50))
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Profile Button
                                IconButton(
                                    onClick = onNavigateToProfile,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            Color(0xFFF5F5F5),
                                            RoundedCornerShape(50)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color(0xFF666666),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$postedCount",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                    Text(
                                        text = "Posted",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$applicantsCount",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "Applicants",
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Divider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        Text(
                            text = "Post New Internship",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = jobTitle,
                            onValueChange = { jobTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Job Title *") },
                            placeholder = { Text("e.g. Android Developer") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = company,
                            onValueChange = { company = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Company Name *") },
                            placeholder = { Text("Your company name") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Location") },
                                placeholder = { Text("Istanbul") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Duration") },
                                placeholder = { Text("3 months") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { salary = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Salary") },
                            placeholder = { Text("$800/month") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Description") },
                            placeholder = { Text("Describe the internship responsibilities and requirements...") },
                            minLines = 4,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    item {
                        Button(
                            onClick = { handlePost() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isPosting && jobTitle.isNotBlank() && company.isNotBlank()
                        ) {
                            if (isPosting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Post",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Post Internship",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "* Required fields",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Notifications Dialog
        if (showNotifications) {
            AlertDialog(
                onDismissRequest = { showNotifications = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Column {
                        Text(
                            "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount unread",
                                fontSize = 13.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        item {
                            CompanyNotificationItem(
                                title = "New Application",
                                message = "John Doe applied for Android Developer position",
                                time = "1h ago",
                                isNew = true
                            )
                        }
                        item {
                            CompanyNotificationItem(
                                title = "Post Performance",
                                message = "Web Developer post has 45 views and 12 applications",
                                time = "3h ago",
                                isNew = true
                            )
                        }
                        item {
                            CompanyNotificationItem(
                                title = "Profile Featured",
                                message = "Your company profile is now on the homepage",
                                time = "5h ago",
                                isNew = true
                            )
                        }
                        item {
                            CompanyNotificationItem(
                                title = "Candidate Message",
                                message = "Sarah asked about the Data Analyst position",
                                time = "1d ago",
                                isNew = true
                            )
                        }
                        item {
                            CompanyNotificationItem(
                                title = "Post Expiring Soon",
                                message = "iOS Developer post will expire in 3 days",
                                time = "2d ago",
                                isNew = false
                            )
                        }
                        item {
                            CompanyNotificationItem(
                                title = "Milestone Reached",
                                message = "100+ applications received this month",
                                time = "3d ago",
                                isNew = false
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotifications = false }) {
                        Text("Close", color = Color(0xFF2196F3), fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            Toast.makeText(context, "Marked as read", Toast.LENGTH_SHORT).show()
                            showNotifications = false
                        }) {
                            Text("Mark as read", color = Color(0xFF999999))
                        }
                    }
                }
            )
        }
    }
}

// CompanyNotificationItem fonksiyonu - Dosyanın SONUNDA!
@Composable
fun CompanyNotificationItem(
    title: String,
    message: String,
    time: String,
    isNew: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isNew) Color(0xFFF5F5F5) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dot indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isNew) Color(0xFF2196F3) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isNew) FontWeight.SemiBold else FontWeight.Normal,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = time,
                fontSize = 11.sp,
                color = Color(0xFF999999)
            )
        }
    }
}