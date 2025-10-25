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

@Composable
fun CompanyDashboard(onLogout: () -> Unit) {
    val context = LocalContext.current
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var postedCount by remember { mutableStateOf(0) }

    val handlePost = {
        if (jobTitle.isNotBlank() && company.isNotBlank()) {
            Toast.makeText(
                context,
                "Internship '${jobTitle}' posted successfully! 🎉",
                Toast.LENGTH_LONG
            ).show()

            postedCount++

            // Clear form after posting
            jobTitle = ""
            company = ""
            location = ""
            duration = ""
            salary = ""
            description = ""
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
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏢", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Company Portal",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Post internship opportunities",
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
                                    tint = Color(0xFF2196F3)
                                )
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
                                        text = "0",
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
                            onClick = handlePost,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = jobTitle.isNotBlank() && company.isNotBlank()
                        ) {
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
    }
}