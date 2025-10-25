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
import com.erensaridag.careermatch.data.getSampleInternships

@Composable
fun StudentDashboard(onLogout: () -> Unit) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val internships = remember { getSampleInternships() }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎓", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Student Portal",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Find your dream internship",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

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
                    .padding(16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "Ready to apply?",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* Profile */ },
                            modifier = Modifier.background(
                                Color(0xFFF5F5F5),
                                RoundedCornerShape(50)
                            )
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                    text = "${filteredInternships.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "Available",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }

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
                                    text = "0",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                Text(
                                    text = "Applied",
                                    fontSize = 12.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section Title
                    Text(
                        text = "Available Internships",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Internship List
                    if (filteredInternships.isEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredInternships) { internship ->
                                InternshipCard(
                                    internship = internship,
                                    onApply = {
                                        Toast.makeText(
                                            context,
                                            "Applied to ${internship.title} at ${internship.company}! ✅",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}