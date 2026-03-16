package com.airesume.builder.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesume.builder.viewmodel.ResumeViewModel
import com.airesume.builder.ui.components.ResumeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCreateResume:  () -> Unit,
    onMyResumes:     () -> Unit,
    onTemplates:     () -> Unit,
    onImproveResume: () -> Unit,
    onSettings:      () -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val dashState by viewModel.dashboardState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Resume Builder", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateResume,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Resume") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Hero banner ───────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text("✨ AI-Powered Resumes", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(4.dp))
                        Text("Create ATS-Optimized\nResumes in Minutes",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onCreateResume,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create Resume", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Quick action grid ─────────────────────────────────────────────
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard("My Resumes", Icons.Default.Folder,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        "${dashState.resumes.size} saved",
                        onMyResumes, Modifier.weight(1f))
                    DashboardActionCard("Templates", Icons.Default.GridView,
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer,
                        "3 styles",
                        onTemplates, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionCard("AI Improve", Icons.Default.AutoAwesome,
                        MaterialTheme.colorScheme.tertiaryContainer ?: MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer ?: MaterialTheme.colorScheme.onPrimaryContainer,
                        "Upgrade existing",
                        onImproveResume, Modifier.weight(1f))
                    DashboardActionCard("Settings", Icons.Default.Settings,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        "API & prefs",
                        onSettings, Modifier.weight(1f))
                }
            }

            // ── Recent resumes ────────────────────────────────────────────────
            if (dashState.resumes.isNotEmpty()) {
                item {
                    Text("Recent Resumes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(minOf(dashState.resumes.size, 3)) { idx ->
                    val resume = dashState.resumes[idx]
                    ResumeCard(
                        title = resume.title,
                        updatedAt = resume.updatedAt,
                        isComplete = resume.isComplete,
                        template = resume.template.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { /* handled by onOpenResume */ },
                        onEdit = { /* navigate to edit */ },
                        onDelete = { viewModel.deleteResume(resume.id) },
                        onImprove = onImproveResume
                    )
                }
                if (dashState.resumes.size > 3) {
                    item {
                        TextButton(onClick = onMyResumes, modifier = Modifier.fillMaxWidth()) {
                            Text("View All ${dashState.resumes.size} Resumes →")
                        }
                    }
                }
            }

            // Bottom padding for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
        }
    }
}
