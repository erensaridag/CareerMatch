package com.erensaridag.careermatch.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.erensaridag.careermatch.data.Internship
import com.erensaridag.careermatch.firebase.InternshipManager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CompanyDashboard(onLogout: () -> Unit, onNavigateToProfile: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var currentScreen by remember { mutableStateOf("Dashboard") }
    var selectedInternship by remember { mutableStateOf<Internship?>(null) }
    var internships by remember { mutableStateOf<List<Internship>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load internships
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val manager = InternshipManager()
            val result = manager.getCompanyInternships(userId)
            internships = result.getOrNull() ?: emptyList()
            isLoading = false
        } else {
            isLoading = false
        }
    }

    when (currentScreen) {
        "Dashboard" -> {
            DashboardView(
                internships = internships,
                isLoading = isLoading,
                onInternshipClick = { internship ->
                    selectedInternship = internship
                    currentScreen = "Details"
                },
                onLogout = onLogout,
                onNavigateToProfile = onNavigateToProfile
            )
        }
        "Details" -> {
            if (selectedInternship != null) {
                InternshipDetailsScreen(
                    internship = selectedInternship!!,
                    onBack = { currentScreen = "Dashboard" },
                    onViewApplicants = { currentScreen = "Applicants" }
                )
            }
        }
        "Applicants" -> {
            if (selectedInternship != null) {
                ApplicantsScreen(
                    internship = selectedInternship!!,
                    onBack = { currentScreen = "Details" }
                )
            }
        }
    }
}

@Composable
fun DashboardView(
    internships: List<Internship>,
    isLoading: Boolean,
    onInternshipClick: (Internship) -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var showNotifications by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3),
                        Color(0xFF1976D2),
                        Color(0xFF1565C0)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CareerMatch",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notifications
                    IconButton(
                        onClick = { showNotifications = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Profile
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Welcome Section
                    Text(
                        text = "Welcome back! 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "Manage your internship postings",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Posted",
                            value = internships.size.toString(),
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Active",
                            value = internships.size.toString(),
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Section Title
                    Text(
                        text = "Your Internships",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Internships List
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2196F3))
                        }
                    } else if (internships.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "📋",
                                    fontSize = 40.sp
                                )
                                Text(
                                    text = "No internships posted yet",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Create Internship", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .height(44.dp)
                                        .width(160.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Create Internship",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(internships) { internship ->
                                InternshipCardCompany(
                                    internship = internship,
                                    onClick = { onInternshipClick(internship) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notifications Dialog
        if (showNotifications) {
            AlertDialog(
                onDismissRequest = { showNotifications = false },
                title = { Text("Notifications") },
                text = { Text("No new notifications") },
                confirmButton = {
                    Button(onClick = { showNotifications = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(70.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
fun InternshipCardCompany(
    internship: Internship,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = internship.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = internship.location,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Status Badge
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Active",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFFE0E0E0),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Applicants: 0",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun InternshipDetailsScreen(
    internship: Internship,
    onBack: () -> Unit,
    onViewApplicants: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                text = "Internship Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = internship.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            item {
                DetailRow("Company", internship.company)
                DetailRow("Location", internship.location)
                DetailRow("Salary", internship.salary)
                DetailRow("Duration", "${internship.duration} months")
            }

            item {
                Text(
                    text = "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = internship.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Button(
                    onClick = onViewApplicants,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "View Applicants",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )
    }
}

@Composable
fun ApplicantsScreen(
    internship: Internship,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
            Text(
                text = "Applicants",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Applicants for: ${internship.title}",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading applicants...",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}