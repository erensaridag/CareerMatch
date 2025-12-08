package com.erensaridag.careermatch.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.erensaridag.careermatch.firebase.AuthManager
import com.erensaridag.careermatch.firebase.InternshipManager
import com.erensaridag.careermatch.firebase.ApplicationManager
import com.erensaridag.careermatch.firebase.Application
import com.erensaridag.careermatch.ui.theme.*
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
    var isLoading by remember { mutableStateOf(true) }
    var applicationCount by remember { mutableStateOf(0) }
    
    // Navigation state
    var currentScreen by remember { mutableStateOf("Dashboard") }
    var selectedInternship by remember { mutableStateOf<Internship?>(null) }
    var applicationSuccess by remember { mutableStateOf(false) }

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
                    Toast.makeText(context, "Staj ilanları yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
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

    when (currentScreen) {
        "Dashboard" -> {
            JobListScreen(
                internships = internships,
                isLoading = isLoading,
                searchQuery = searchText,
                onSearchQueryChange = { searchText = it },
                applicationCount = applicationCount,
                onJobClick = { internship ->
                    selectedInternship = internship
                    applicationSuccess = false
                    currentScreen = "Details"
                },
                onApplicationsClick = { currentScreen = "MyApplications" },
                onLogout = onLogout,
                onNavigateToProfile = onNavigateToProfile
            )
        }
        "Details" -> {
            if (selectedInternship != null) {
                JobDetailScreen(
                    internship = selectedInternship!!,
                    applicationSuccess = applicationSuccess,
                    onApply = {
                        scope.launch {
                            val userId = authManager.getCurrentUser()?.uid
                            if (userId != null) {
                                val result = applicationManager.applyToInternship(
                                    studentId = userId,
                                    internshipId = selectedInternship!!.id.toString(),
                                    internshipTitle = selectedInternship!!.title,
                                    companyName = selectedInternship!!.company
                                )
                                result.fold(
                                    onSuccess = {
                                        applicationCount++
                                        applicationSuccess = true
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(
                                            context,
                                            error.message ?: "Başvuru başarısız",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    },
                    onBack = { currentScreen = "Dashboard" }
                )
            }
        }
        "MyApplications" -> {
            MyApplicationsScreen(
                onBack = { currentScreen = "Dashboard" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    internships: List<Internship>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    applicationCount: Int,
    onJobClick: (Internship) -> Unit,
    onApplicationsClick: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter internships
    val filteredInternships = remember(searchQuery, internships) {
        if (searchQuery.isBlank()) {
            internships
        } else {
            internships.filter { internship ->
                internship.title.contains(searchQuery, ignoreCase = true) ||
                        internship.company.contains(searchQuery, ignoreCase = true) ||
                        internship.location.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mevcut Staj İlanları",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Application count badge
                    if (applicationCount > 0) {
                        IconButton(onClick = onApplicationsClick) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = SuccessColor
                                    ) {
                                        Text(
                                            text = "$applicationCount",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Başvurularım"
                                )
                            }
                        }
                    }
                    
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Staj ara...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PrimaryGradientStart
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Aramayı temizle"
                                )
                            }
                        }
                    },
                    singleLine = true,
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
                        unfocusedTextColor = DarkText,
                        focusedLeadingIconColor = PrimaryGradientStart,
                        unfocusedLeadingIconColor = LightText,
                        focusedPlaceholderColor = LightText.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = LightText.copy(alpha = 0.6f)
                    )
                )
            }
            
            // Job List
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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
                    filteredInternships.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = LightText.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) 
                                    "\"$searchQuery\" ile eşleşen staj ilanı bulunamadı" 
                                else 
                                    "Mevcut staj ilanı yok",
                                style = MaterialTheme.typography.bodyLarge,
                                color = LightText
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
                                    text = "${filteredInternships.size} ${if (filteredInternships.size == 1) "staj ilanı" else "staj ilanı"} bulundu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LightText,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            items(filteredInternships, key = { it.id }) { internship ->
                                InternshipJobCard(
                                    internship = internship,
                                    onClick = { onJobClick(internship) }
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
fun InternshipJobCard(
    internship: Internship,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = internship.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryGradientStart
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = internship.company,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGradientStart,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = LightText
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = DividerColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = LightText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = internship.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightText
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = LightText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = internship.salary,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightText
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    internship: Internship,
    applicationSuccess: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staj Detayları", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
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
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
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
                        Text(
                            text = internship.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = internship.company,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Job Details
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Location and Salary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryGradientStart.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryGradientStart
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Konum",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LightText
                                    )
                                    Text(
                                        text = internship.location,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkText
                                    )
                                }
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = SuccessColor.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = SuccessColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Maaş",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LightText
                                    )
                                    Text(
                                        text = internship.salary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkText
                                    )
                                }
                            }
                        }
                    }
                    
                    // Duration Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AccentColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = AccentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                    Text(
                                        text = "Süre",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LightText
                                    )
                                Text(
                                    text = internship.duration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText
                                )
                            }
                        }
                    }
                    
                    // Description
                    DetailSection(
                        title = "Pozisyon Hakkında",
                        icon = Icons.Default.Description,
                        content = internship.description
                    )
                }
            }
            
            // Bottom Action Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Success Message
                    AnimatedVisibility(
                        visible = applicationSuccess,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SuccessColor.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Başvuru başarıyla gönderildi!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText
                                )
                            }
                        }
                    }
                    
                    // Apply Button
                    if (applicationSuccess) {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = SuccessColor.copy(alpha = 0.3f),
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Başvuruldu",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = onApply,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGradientStart
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hemen Başvur",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = PrimaryGradientStart
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = LightText,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager() }
    val applicationManager = remember { ApplicationManager() }
    
    var applications by remember { mutableStateOf<List<Application>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    
    LaunchedEffect(Unit) {
        scope.launch {
            val userId = authManager.getCurrentUser()?.uid
            if (userId != null) {
                val result = applicationManager.getStudentApplications(userId)
                result.fold(
                    onSuccess = { apps ->
                        // Filter out applications for deleted internships
                        val internshipManager = InternshipManager()
                        val allInternships = internshipManager.getAllInternships().getOrNull() ?: emptyList()
                        val activeInternshipIds = allInternships.map { it.id }.toSet()
                        
                        // Only keep applications where the internship still exists
                        applications = apps.filter { app ->
                            activeInternshipIds.contains(app.internshipId)
                        }
                        isLoading = false
                    },
                    onFailure = {
                        isLoading = false
                        Toast.makeText(context, "Başvurular yüklenemedi", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Başvurularım", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                applications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = LightText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Henüz başvuru yok",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LightText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stajlara başvurmaya başla!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightText.copy(alpha = 0.7f)
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
                                text = "${applications.size} ${if (applications.size == 1) "başvuru" else "başvuru"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(applications, key = { it.id }) { application ->
                            ApplicationCard(application = application)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    application: Application,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.internshipTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryGradientStart
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = application.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGradientStart,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Status Badge
                StatusBadge(status = application.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = DividerColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = LightText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Başvuru Tarihi: ${formatDate(application.appliedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightText
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor, statusText) = when (status) {
        "pending" -> Triple(
            WarningColor.copy(alpha = 0.1f),
            WarningColor,
            "Beklemede"
        )
        "accepted" -> Triple(
            SuccessColor.copy(alpha = 0.1f),
            SuccessColor,
            "Kabul Edildi"
        )
        "rejected" -> Triple(
            ErrorColor.copy(alpha = 0.1f),
            ErrorColor,
            "Reddedildi"
        )
        else -> Triple(
            LightText.copy(alpha = 0.1f),
            LightText,
            "Bilinmiyor"
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

fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days ${if (days == 1L) "gün" else "gün"} önce"
        hours > 0 -> "$hours ${if (hours == 1L) "saat" else "saat"} önce"
        minutes > 0 -> "$minutes ${if (minutes == 1L) "dakika" else "dakika"} önce"
        else -> "Az önce"
    }
}
