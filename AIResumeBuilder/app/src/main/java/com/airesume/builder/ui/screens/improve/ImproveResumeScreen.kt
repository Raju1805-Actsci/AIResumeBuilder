package com.airesume.builder.ui.screens.improve

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesume.builder.ui.components.AiGenerateButton
import com.airesume.builder.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImproveResumeScreen(
    resumeId: Long,
    onNavigateBack: () -> Unit,
    onDone: (Long) -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var targetRole by remember { mutableStateOf("") }
    var customFeedback by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("") }

    LaunchedEffect(resumeId) { viewModel.loadResume(resumeId) }

    // Navigate when improvement is done
    LaunchedEffect(uiState.isGeneratingAi, uiState.isSaved) {
        if (!uiState.isGeneratingAi && uiState.isSaved && uiState.resume.id != 0L) {
            onDone(uiState.resume.id)
        }
    }

    val quickGoals = listOf(
        "Make it more ATS-friendly",
        "Target senior-level roles",
        "Emphasize leadership skills",
        "Focus on technical achievements",
        "Make it more concise",
        "Add more quantified metrics"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Improve Resume", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI Resume Improvement", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Tell the AI what to improve. The more specific, the better the result.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Target Role (optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = targetRole,
                            onValueChange = { targetRole = it },
                            label = { Text("e.g. Senior Android Engineer at Google") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Quick Improvement Goals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        quickGoals.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { goal ->
                                    FilterChip(
                                        selected = selectedGoal == goal,
                                        onClick = { selectedGoal = if (selectedGoal == goal) "" else goal },
                                        label = { Text(goal, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Custom Instructions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customFeedback,
                            onValueChange = { customFeedback = it },
                            label = { Text("Describe what to improve…") },
                            placeholder = { Text("e.g. I'm applying for machine learning roles, emphasize my Python and TensorFlow experience") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            uiState.error?.let { err ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                val feedback = buildString {
                    if (targetRole.isNotEmpty()) append("Target role: $targetRole. ")
                    if (selectedGoal.isNotEmpty()) append("Goal: $selectedGoal. ")
                    if (customFeedback.isNotEmpty()) append(customFeedback)
                }.ifEmpty { "Improve overall quality and ATS optimization" }

                AiGenerateButton(
                    onClick = { viewModel.improveResume(resumeId, feedback) },
                    isLoading = uiState.isGeneratingAi,
                    progress = uiState.aiProgress
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
