package com.airesume.builder.ui.screens.myresumes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesume.builder.ui.components.EmptyState
import com.airesume.builder.ui.components.ResumeCard
import com.airesume.builder.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyResumesScreen(
    onNavigateBack:  () -> Unit,
    onOpenResume:    (Long) -> Unit,
    onEditResume:    (Long) -> Unit,
    onImproveResume: (Long) -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val dashState by viewModel.dashboardState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Long?>(null) }

    // Delete confirmation dialog
    deleteTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Resume?") },
            text  = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteResume(id); deleteTarget = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Resumes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (dashState.resumes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                EmptyState(
                    icon = Icons.Default.Description,
                    title = "No Resumes Yet",
                    subtitle = "Create your first AI-powered resume to get started",
                    actionLabel = "Create Resume",
                    onAction = onNavigateBack
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("${dashState.resumes.size} Resume${if (dashState.resumes.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(dashState.resumes, key = { it.id }) { resume ->
                    ResumeCard(
                        title = resume.title,
                        updatedAt = resume.updatedAt,
                        isComplete = resume.isComplete,
                        template = resume.template.name.replace("_", " ")
                            .lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { onOpenResume(resume.id) },
                        onEdit = { onEditResume(resume.id) },
                        onDelete = { deleteTarget = resume.id },
                        onImprove = { onImproveResume(resume.id) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
