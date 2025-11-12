    package com.erensaridag.careermatch.screens

    import android.widget.Toast
    import androidx.compose.foundation.background
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
    import com.erensaridag.careermatch.components.InternshipCard
    import com.erensaridag.careermatch.data.Internship
    import com.erensaridag.careermatch.firebase.AuthManager
    import com.erensaridag.careermatch.firebase.InternshipManager
    import com.erensaridag.careermatch.firebase.ApplicationManager
    import kotlinx.coroutines.launch

    @Composable
    fun StudentDashboard(onLogout: () -> Unit, onNavigateToProfile: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val authManager = remember { AuthManager() }
        val internshipManager = remember { InternshipManager() }
        val applicationManager = remember { ApplicationManager() }

        var searchText by remember { mutableStateOf("") }
        var internships by remember { mutableStateOf<List<Internship>>(emptyList()) }
        var showNotifications by remember { mutableStateOf(false) }
        val unreadCount by remember { mutableStateOf(3) }
        var isLoading by remember { mutableStateOf(true) }
        var applicationCount by remember { mutableStateOf(0) }

        // Load internships on start
        LaunchedEffect(Unit) {
            scope.launch {
                isLoading = true
                val result = internshipManager.getAllInternships()
                result.fold(
                    onSuccess = { loadedInternships ->
                        internships = loadedInternships
                        isLoading = false
                    },
                    onFailure = { error ->
                        isLoading = false
                        Toast.makeText(context, "Failed to load internships: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )

                // Load application count
                authManager.getCurrentUser()?.uid?.let { userId ->
                    val countResult = applicationManager.getApplicationCount(userId)
                    countResult.fold(
                        onSuccess = { count -> applicationCount = count },
                        onFailure = { }
                    )
                }
            }
        }

        // Filter internships based on search text
        val filteredInternships = remember(searchText, internships) {
            if (searchText.isBlank()) {
                internships
            } else {
                internships.filter { internship ->
                    internship.title.contains(searchText, ignoreCase = true) ||
                            internship.company.contains(searchText, ignoreCase = true) ||
                            internship.location.contains(searchText, ignoreCase = true)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF2196F3),
                            Color(0xFF1976D2)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Welcome Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back! 👋",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                                Text(
                                    text = "Ready to apply?",
                                    fontSize = 13.sp,
                                    color = Color(0xFF666666),
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Notification Button with Badge
                                Box {
                                    IconButton(
                                        onClick = { showNotifications = true },
                                        modifier = Modifier
                                            .size(40.dp)
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
                                                text = if (unreadCount > 9) "3+" else unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Profile Button
                                IconButton(
                                    onClick = {
                                        onNavigateToProfile()
                                    },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search internships...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF666666)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${filteredInternships.size}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text(
                                        text = "Available",
                                        fontSize = 11.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$applicationCount",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                    Text(
                                        text = "Applied",
                                        fontSize = 11.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section Title
                        Text(
                            text = "Available Internships",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Internship List
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF4CAF50))
                            }
                        } else if (filteredInternships.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "🔍",
                                        fontSize = 48.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No internships found",
                                        fontSize = 16.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(filteredInternships) { internship ->
                                    InternshipCard(
                                        internship = internship,
                                        onApply = {
                                            scope.launch {
                                                val userId = authManager.getCurrentUser()?.uid
                                                if (userId != null) {
                                                    val result = applicationManager.applyToInternship(
                                                        studentId = userId,
                                                        internshipId = internship.id.toString(),
                                                        internshipTitle = internship.title,
                                                        companyName = internship.company
                                                    )
                                                    result.fold(
                                                        onSuccess = {
                                                            applicationCount++
                                                            Toast.makeText(
                                                                context,
                                                                "Applied to ${internship.title} at ${internship.company}! ✅",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        },
                                                        onFailure = { error ->
                                                            Toast.makeText(
                                                                context,
                                                                error.message ?: "Failed to apply",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    )
                                                }
                                            }
                                        }
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
                                NotificationItem(
                                    title = "Application Accepted",
                                    message = "TechCorp accepted your application for Android Developer",
                                    time = "2h ago",
                                    isNew = true
                                )
                            }
                            item {
                                NotificationItem(
                                    title = "New Internship Match",
                                    message = "Web Developer at WebStudio matches your profile",
                                    time = "5h ago",
                                    isNew = true
                                )
                            }
                            item {
                                NotificationItem(
                                    title = "Interview Scheduled",
                                    message = "Interview with DataTech tomorrow at 10:00 AM",
                                    time = "1d ago",
                                    isNew = true
                                )
                            }
                            item {
                                NotificationItem(
                                    title = "Message Received",
                                    message = "DesignCo sent you a message about your portfolio",
                                    time = "2d ago",
                                    isNew = false
                                )
                            }
                            item {
                                NotificationItem(
                                    title = "Profile Views",
                                    message = "5 companies viewed your profile this week",
                                    time = "3d ago",
                                    isNew = false
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotifications = false }) {
                            Text("Close", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
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

    @Composable
    fun NotificationItem(
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
                        color = if (isNew) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(50)
                    )
                    .align(Alignment.Top)
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