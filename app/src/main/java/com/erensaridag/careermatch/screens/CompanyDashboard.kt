package com.erensaridag.careermatch.screens

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
                    Toast.makeText(context, "İlanlar yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
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
                onLogout = onLogout,
                onNavigateToProfile = { currentScreen = "Profile" }
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
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val companyName = auth.currentUser?.email?.substringBefore("@") ?: "Company"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel",
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
                            contentDescription = "Profil"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Çıkış Yap"
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
                        text = "Tekrar hoş geldiniz!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Staj ilanlarınızı ve başvurularınızı yönetin",
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
                        title = "Yayınlanan İlanlar",
                        value = internships.size.toString(),
                        icon = Icons.Default.Work,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Başvurular",
                        value = "0",
                        icon = Icons.Default.People,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Actions
                Text(
                    text = "Hızlı İşlemler",
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
                                text = "Yeni İlan Yayınla",
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
                                text = "İlanları Yönet",
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
                                text = "İpucu",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "En iyi adayları çekmek için detaylı iş tanımları yayınlayın!",
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
                        text = "Staj İlanlarınız",
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                        text = "Aktif",
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
                        text = "0 Başvuru",
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
                            text = "Aktif",
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
                        text = "0 Başvuru",
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
                    text = "Henüz staj ilanı yok",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "İlk staj ilanınızı oluşturarak başlayın",
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
                        text = "Staj İlanı Oluştur",
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
                        text = "Stajları Yönet",
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
                                contentDescription = "Geri",
                                tint = DarkText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Staj Detayları",
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
                            
                            DetailRowPremium("Şirket", internship.company, Icons.Default.Business)
                            DetailRowPremium("Konum", internship.location, Icons.Default.LocationOn)
                            DetailRowPremium("Maaş", internship.salary, Icons.Default.AttachMoney)
                            DetailRowPremium("Süre", "${internship.duration} ay", Icons.Default.Schedule)
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
                                text = "Açıklama",
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
                            text = "Başvuruları Gör",
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

@Composable
fun ApplicantsScreen(
    internship: Internship,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Premium Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
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
                        color = PrimaryGradientStart.copy(alpha = 0.1f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = PrimaryGradientStart,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Başvurular",
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${internship.title} için başvurular",
                        fontSize = 14.sp,
                        color = LightText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryGradientStart,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Başvurular yükleniyor...",
                                    fontSize = 14.sp,
                                    color = LightText
                                )
                            }
                        }
                    }
                }
            }
        }
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
                title = { Text(if (isEditing) "İlanı Düzenle" else "Yeni İlan Yayınla", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("İş Başlığı") },
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
                label = { Text("Konum") },
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
                    label = { Text("Süre") },
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
                    label = { Text("Maaş") },
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
                label = { Text("Açıklama") },
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
                        Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(context, if (isEditing) "İlan başarıyla güncellendi" else "İlan başarıyla yayınlandı", Toast.LENGTH_SHORT).show()
                                    onJobPosted()
                                },
                                onFailure = { e ->
                                    Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Text(if (isEditing) "İlanı Güncelle" else "İlanı Yayınla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                title = { Text("İlanları Yönet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
                                Text("Düzenle")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { showDeleteDialog = internship.id },
                                colors = ButtonDefaults.textButtonColors(contentColor = WarningColor)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sil")
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
                                text = "Henüz ilan yayınlanmadı",
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
                title = { Text("İlanı Sil") },
                text = { Text("Bu ilanı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
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
                                        Toast.makeText(context, "İlan silindi", Toast.LENGTH_SHORT).show()
                                        showDeleteDialog = null
                                    },
                                    onFailure = { e ->
                                        Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningColor)
                    ) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("İptal")
                    }
                }
            )
        }
    }
}
