package com.erensaridag.careermatch.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erensaridag.careermatch.data.Internship
import com.erensaridag.careermatch.firebase.ApplicationManager
import com.erensaridag.careermatch.firebase.InternshipManager
import com.erensaridag.careermatch.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CompanyDashboard(onLogout: () -> Unit, onNavigateToProfile: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var currentScreen by remember { mutableStateOf("Dashboard") }
    var selectedInternship by remember { mutableStateOf<Internship?>(null) }
    var applicantsBackScreen by remember { mutableStateOf("Details") }
    var internships by remember { mutableStateOf<List<Internship>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Load internships
    LaunchedEffect(userId, refreshTrigger) {
        if (userId.isNotEmpty()) {
            val manager = InternshipManager()
            val result = manager.getCompanyInternships(userId)
            result.fold(
                onSuccess = { list ->
                    internships = list
                },
                onFailure = { e ->
                    Toast.makeText(context, "Error loading job postings: ${e.message}", Toast.LENGTH_LONG).show()
                    internships = emptyList()
                }
            )
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
                onPostJobClick = { currentScreen = "PostJob" },
                onManageJobsClick = { currentScreen = "ManageJobs" },
                onViewAllApplicationsClick = { currentScreen = "AllApplications" },
                onLogout = onLogout,
                onNavigateToProfile = { currentScreen = "Profile" }
            )
        }
        "AllApplications" -> {
            AllApplicationsScreen(
                internships = internships,
                onBack = { currentScreen = "Dashboard" },
                onInternshipClick = { internship ->
                    selectedInternship = internship
                    applicantsBackScreen = "AllApplications"
                    currentScreen = "Applicants"
                }
            )
        }
        "Details" -> {
            if (selectedInternship != null) {
                InternshipDetailsScreen(
                    internship = selectedInternship!!,
                    onBack = { currentScreen = "Dashboard" },
                    onViewApplicants = {
                        applicantsBackScreen = "Details"
                        currentScreen = "Applicants"
                    }
                )
            }
        }
        "Applicants" -> {
            if (selectedInternship != null) {
                ApplicantsScreen(
                    internship = selectedInternship!!,
                    onBack = { currentScreen = applicantsBackScreen }
                )
            }
        }
        "PostJob" -> {
            PostJobScreen(
                onBack = { currentScreen = "Dashboard" },
                onJobPosted = {
                    // Refresh list
                    isLoading = true
                    refreshTrigger++
                    currentScreen = "Dashboard"
                }
            )
        }
        "ManageJobs" -> {
            ManageJobsScreen(
                internships = internships,
                onBack = { currentScreen = "Dashboard" },
                onEdit = { internship ->
                    selectedInternship = internship
                    currentScreen = "EditJob"
                },
                onDelete = { internshipId ->
                    refreshTrigger++
                }
            )
        }
        "EditJob" -> {
            if (selectedInternship != null) {
                PostJobScreen(
                    internshipToEdit = selectedInternship,
                    onBack = { currentScreen = "ManageJobs" },
                    onJobPosted = {
                        isLoading = true
                        refreshTrigger++
                        currentScreen = "Dashboard"
                    }
                )
            }
        }
        "Profile" -> {
            CompanyProfile(
                onBack = { currentScreen = "Dashboard" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    internships: List<Internship>,
    isLoading: Boolean,
    onInternshipClick: (Internship) -> Unit,
    onPostJobClick: () -> Unit,
    onManageJobsClick: () -> Unit,
    onViewAllApplicationsClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val companyName = auth.currentUser?.email?.substringBefore("@") ?: "Company"
    val scope = rememberCoroutineScope()
    val applicationManager = remember { ApplicationManager() }

    var totalApplicationsCount by remember { mutableStateOf(0) }
    var applicationCountsByInternshipId by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var hasShownCountErrorToast by remember { mutableStateOf(false) }

    LaunchedEffect(internships) {
        scope.launch {
            val internshipIds = internships.map { it.id }
            Log.d("CompanyDashboard", "Loading application counts. internshipIds=$internshipIds")

            // Total applications count
            val totalResult = applicationManager.getTotalApplicationsCountForInternships(internshipIds)
            totalResult.fold(
                onSuccess = {
                    totalApplicationsCount = it
                    Log.d("CompanyDashboard", "Total applications count=$it")
                },
                onFailure = { e ->
                    totalApplicationsCount = 0
                    Log.e("CompanyDashboard", "Failed to load total applications count", e)
                    Toast.makeText(context, "Applications count error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )

            // Per internship counts
            val countsMap = mutableMapOf<String, Int>()
            internships.forEach { internship ->
                val countResult = applicationManager.getInternshipApplicationCount(internship.id)
                countResult.fold(
                    onSuccess = { count ->
                        countsMap[internship.id] = count
                        Log.d("CompanyDashboard", "internshipId=${internship.id} applicationCount=$count")
                    },
                    onFailure = { e ->
                        countsMap[internship.id] = 0
                        Log.e(
                            "CompanyDashboard",
                            "Failed to load application count for internshipId=${internship.id}",
                            e
                        )
                        if (!hasShownCountErrorToast) {
                            hasShownCountErrorToast = true
                            Toast.makeText(context, "Application query failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            applicationCountsByInternshipId = countsMap
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGradientStart,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manage your internship postings and applications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Published Posts",
                        value = internships.size.toString(),
                        icon = Icons.Default.Work,
                        containerColor = PrimaryGradientStart.copy(alpha = 0.12f),
                        contentColor = PrimaryGradientStart,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Applications",
                        value = totalApplicationsCount.toString(),
                        icon = Icons.Default.People,
                        containerColor = PrimaryGradientEnd.copy(alpha = 0.12f),
                        contentColor = PrimaryGradientEnd,
                        modifier = Modifier.weight(1f),
                        onClick = onViewAllApplicationsClick
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Actions
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPostJobClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGradientStart
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Post New Job",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onManageJobsClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            border = BorderStroke(2.dp, PrimaryGradientStart),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryGradientStart
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Manage Postings",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tip",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Post detailed job descriptions to attract the best candidates!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Internships List Section
                if (!isLoading && internships.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your Internship Postings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(internships) { internship ->
                            SimpleInternshipCard(
                                internship = internship,
                                applicationCount = applicationCountsByInternshipId[internship.id] ?: 0,
                                onClick = { onInternshipClick(internship) }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryGradientStart
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SimpleInternshipCard(
    internship: Internship,
    applicationCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = internship.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LightText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = internship.location,
                            fontSize = 13.sp,
                            color = LightText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        color = SuccessColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = DividerColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$applicationCount Applications",
                        fontSize = 13.sp,
                        color = PrimaryGradientStart,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View",
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EnhancedStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.1f) }
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = gradientColors[0].copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = gradientColors[0],
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumInternshipCard(
    internship: Internship,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            }
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = internship.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LightText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = internship.location,
                            fontSize = 13.sp,
                            color = LightText
                        )
                    }
                }

                // Premium Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            color = SuccessColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = DividerColor,
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "0 Applications",
                        fontSize = 13.sp,
                        color = PrimaryGradientStart,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Surface(
                    shape = CircleShape,
                    color = PrimaryGradientStart.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View",
                            tint = PrimaryGradientStart,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EmptyStateView(
    onCreateClick: () -> Unit,
    onManageClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = PrimaryGradientStart.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "📋",
                        fontSize = 48.sp
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No internship postings yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Start by creating your first internship posting",
                    fontSize = 14.sp,
                    color = LightText
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier
                        .height(52.dp)
                        .width(220.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGradientStart
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Internship Posting",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                OutlinedButton(
                    onClick = onManageClick,
                    modifier = Modifier
                        .height(52.dp)
                        .width(220.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryGradientStart
                    ),
                    border = BorderStroke(2.dp, PrimaryGradientStart),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manage Internships",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryGradientStart.copy(alpha = 0.1f),
                        CardBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Premium Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = DarkText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Internship Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = internship.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            DetailRowPremium("Company", internship.company, Icons.Default.Business)
                            DetailRowPremium("Location", internship.location, Icons.Default.LocationOn)
                            DetailRowPremium("Salary", internship.salary, Icons.Default.AttachMoney)
                            DetailRowPremium("Duration", "${internship.duration} months", Icons.Default.Schedule)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Description",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = internship.description,
                                fontSize = 14.sp,
                                color = LightText,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = onViewApplicants,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGradientStart
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "View Applications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun DetailRowPremium(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryGradientStart.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = label,
                fontSize = 14.sp,
                color = LightText,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantsScreen(
    internship: Internship,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applicationManager = remember { com.erensaridag.careermatch.firebase.ApplicationManager() }
    
    var applicants by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load applicants
    LaunchedEffect(internship.id) {
        scope.launch {
            val result = applicationManager.getInternshipApplicantsWithDetails(internship.id)
            result.fold(
                onSuccess = { list ->
                    applicants = list
                    isLoading = false
                },
                onFailure = { e ->
                    Toast.makeText(context, "Error loading applicants: ${e.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Applications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .background(CardBackground)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGradientStart)
                    }
                }
                applicants.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = LightText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No applications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Applications for ${internship.title} will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightText.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "${applicants.size} ${if (applicants.size == 1) "application" else "applications"} for ${internship.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(applicants.size) { index ->
                            ApplicantCard(applicant = applicants[index])
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
fun ApplicantCard(
    applicant: Map<String, Any>,
    modifier: Modifier = Modifier
) {
    val studentName = applicant["studentName"] as? String ?: "Unknown"
    val studentEmail = applicant["studentEmail"] as? String ?: ""
    val studentPhone = applicant["studentPhone"] as? String ?: ""
    val studentUniversity = applicant["studentUniversity"] as? String ?: ""
    val studentMajor = applicant["studentMajor"] as? String ?: ""
    val studentGraduationYear = applicant["studentGraduationYear"] as? String ?: ""
    val studentCvUrl = applicant["studentCvUrl"] as? String ?: ""
    val appliedAt = applicant["appliedAt"] as? Long ?: 0L
    val status = applicant["status"] as? String ?: "pending"
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = studentName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatApplicationDate(appliedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = LightText
                        )
                    }
                }
                
                // Status Badge
                ApplicantStatusBadge(status = status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = DividerColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Education Information
            if (studentUniversity.isNotEmpty() || studentMajor.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGradientStart
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        if (studentUniversity.isNotEmpty()) {
                            Text(
                                text = studentUniversity,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkText
                            )
                        }
                        if (studentMajor.isNotEmpty()) {
                            Text(
                                text = studentMajor + if (studentGraduationYear.isNotEmpty()) " • Class of $studentGraduationYear" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightText
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Contact Information
            if (studentEmail.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGradientStart
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = studentEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkText
                    )
                }
            }
            
            if (studentPhone.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGradientStart
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = studentPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkText
                    )
                }
            }
            
            // CV Link
            if (studentCvUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(studentCvUrl))
                            context.startActivity(intent)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryGradientStart.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = PrimaryGradientStart
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View CV / Resume",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryGradientStart
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PrimaryGradientStart
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicantStatusBadge(status: String) {
    val (backgroundColor, textColor, statusText) = when (status) {
        "pending" -> Triple(
            WarningColor.copy(alpha = 0.15f),
            WarningColor,
            "Pending"
        )
        "accepted" -> Triple(
            SuccessColor.copy(alpha = 0.15f),
            SuccessColor,
            "Accepted"
        )
        "rejected" -> Triple(
            ErrorColor.copy(alpha = 0.15f),
            ErrorColor,
            "Rejected"
        )
        else -> Triple(
            LightText.copy(alpha = 0.15f),
            LightText,
            "Unknown"
        )
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

fun formatApplicationDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days ${if (days == 1L) "day" else "days"} ago"
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        else -> "Just now"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    internshipToEdit: Internship? = null,
    onBack: () -> Unit,
    onJobPosted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val internshipManager = remember { InternshipManager() }

    var title by remember { mutableStateOf(internshipToEdit?.title ?: "") }
    var location by remember { mutableStateOf(internshipToEdit?.location ?: "") }
    var duration by remember { mutableStateOf(internshipToEdit?.duration ?: "") }
    var salary by remember { mutableStateOf(internshipToEdit?.salary ?: "") }
    var description by remember { mutableStateOf(internshipToEdit?.description ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

    val isEditing = internshipToEdit != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Posting" else "Post New Job", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Job Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGradientStart,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = PrimaryGradientStart,
                    unfocusedLabelColor = LightText,
                    cursorColor = PrimaryGradientStart,
                    focusedContainerColor = TextFieldBackgroundFocused,
                    unfocusedContainerColor = TextFieldBackground,
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText
                )
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGradientStart,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = PrimaryGradientStart,
                    unfocusedLabelColor = LightText,
                    cursorColor = PrimaryGradientStart,
                    focusedContainerColor = TextFieldBackgroundFocused,
                    unfocusedContainerColor = TextFieldBackground,
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = AccentColor,
                        unfocusedLabelColor = LightText,
                        cursorColor = AccentColor,
                        focusedContainerColor = TextFieldBackgroundFocused,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText
                    )
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = DividerColor,
                        focusedLabelColor = AccentColor,
                        unfocusedLabelColor = LightText,
                        cursorColor = AccentColor,
                        focusedContainerColor = TextFieldBackgroundFocused,
                        unfocusedContainerColor = TextFieldBackground,
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText
                    )
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGradientStart,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = PrimaryGradientStart,
                    unfocusedLabelColor = LightText,
                    cursorColor = PrimaryGradientStart,
                    focusedContainerColor = TextFieldBackgroundFocused,
                    unfocusedContainerColor = TextFieldBackground,
                    focusedTextColor = DarkText,
                    unfocusedTextColor = DarkText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank() || location.isBlank() || duration.isBlank() || salary.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    scope.launch {
                        val userId = auth.currentUser?.uid
                        val companyName = auth.currentUser?.email?.substringBefore("@") ?: "Company"

                        if (userId != null) {
                            val result = if (isEditing) {
                                internshipManager.updateInternship(
                                    internshipId = internshipToEdit!!.id,
                                    data = mapOf(
                                        "title" to title,
                                        "location" to location,
                                        "duration" to duration,
                                        "salary" to salary,
                                        "description" to description
                                    )
                                )
                            } else {
                                internshipManager.addInternship(
                                    title = title,
                                    company = companyName,
                                    location = location,
                                    duration = duration,
                                    salary = salary,
                                    description = description,
                                    companyId = userId
                                )
                            }

                            result.fold(
                                onSuccess = {
                                    Toast.makeText(context, if (isEditing) "Posting updated successfully" else "Posting published successfully", Toast.LENGTH_SHORT).show()
                                    onJobPosted()
                                },
                                onFailure = { e ->
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    isSubmitting = false
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGradientStart),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditing) "Update Posting" else "Publish Posting", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageJobsScreen(
    internships: List<Internship>,
    onBack: () -> Unit,
    onEdit: (Internship) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val internshipManager = remember { InternshipManager() }
    var list by remember { mutableStateOf(internships) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(internships) {
        list = internships
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Postings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list) { internship ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = internship.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = internship.location,
                            fontSize = 14.sp,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onEdit(internship) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { showDeleteDialog = internship.id },
                                colors = ButtonDefaults.textButtonColors(contentColor = WarningColor)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            if (list.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.WorkOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No postings published yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Posting") },
                text = { Text("Are you sure you want to delete this posting? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val idToDelete = showDeleteDialog!!
                            scope.launch {
                                val result = internshipManager.deleteInternship(idToDelete)
                                result.fold(
                                    onSuccess = {
                                        list = list.filter { it.id != idToDelete }
                                        onDelete(idToDelete) // Optional callback if parent needs to know
                                        Toast.makeText(context, "Posting deleted", Toast.LENGTH_SHORT).show()
                                        showDeleteDialog = null
                                    },
                                    onFailure = { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllApplicationsScreen(
    internships: List<Internship>,
    onBack: () -> Unit,
    onInternshipClick: (Internship) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applicationManager = remember { com.erensaridag.careermatch.firebase.ApplicationManager() }
    
    var allApplications by remember { mutableStateOf<Map<String, List<Map<String, Any>>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalApplicationsCount by remember { mutableStateOf(0) }
    
    // Load all applications for all internships
    LaunchedEffect(internships) {
        scope.launch {
            isLoading = true
            val applicationsMap = mutableMapOf<String, List<Map<String, Any>>>()
            var total = 0
            
            internships.forEach { internship ->
                val result = applicationManager.getInternshipApplicantsWithDetails(internship.id)
                result.fold(
                    onSuccess = { applicants ->
                        if (applicants.isNotEmpty()) {
                            applicationsMap[internship.id] = applicants
                            total += applicants.size
                        }
                    },
                    onFailure = { e ->
                        android.util.Log.e("AllAppsScreen", "Error loading apps: ${e.message}", e)
                        // Toast showing on main thread might be tricky here without context, relying on Log
                    }
                )
            }
            
            if (applicationsMap.isEmpty() && total == 0 && internships.isNotEmpty()) {
                 android.util.Log.d("AllAppsScreen", "No applications found for ${internships.size} internships")
            }
            
            allApplications = applicationsMap
            totalApplicationsCount = total
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Applications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .background(CardBackground)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGradientStart)
                    }
                }
                totalApplicationsCount == 0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = LightText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No applications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Applications will appear here when students apply to your internships",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightText.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = PrimaryGradientStart.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total Applications",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DarkText
                                        )
                                        Text(
                                            text = "$totalApplicationsCount",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryGradientStart
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = PrimaryGradientStart.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                        
                        // Group applications by internship
                        allApplications.forEach { (internshipId, applicants) ->
                            val internship = internships.find { it.id == internshipId }
                            if (internship != null) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onInternshipClick(internship) },
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = internship.title,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DarkText
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = LightText,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = internship.location,
                                                            fontSize = 13.sp,
                                                            color = LightText
                                                        )
                                                    }
                                                }
                                                
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = PrimaryGradientStart.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "${applicants.size} ${if (applicants.size == 1) "application" else "applications"}",
                                                        fontSize = 11.sp,
                                                        color = PrimaryGradientStart,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "View Details",
                                                    fontSize = 13.sp,
                                                    color = PrimaryGradientStart,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = null,
                                                    tint = PrimaryGradientStart,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
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
