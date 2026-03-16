package com.airesume.builder.ui.screens.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TemplateInfo(
    val id: String,
    val name: String,
    val description: String,
    val headerColor: Color,
    val accentColor: Color,
    val isDark: Boolean = false,
    val features: List<String>
)

val templates = listOf(
    TemplateInfo(
        "MODERN_PROFESSIONAL", "Modern Professional",
        "Clean two-column design with bold navy header. Perfect for corporate and business roles.",
        Color(0xFF1A237E), Color(0xFF3949AB), false,
        listOf("Navy header with white text", "Color-coded section dividers", "Skill tags with chip design", "ATS-optimized structure")
    ),
    TemplateInfo(
        "MINIMAL_CLEAN", "Minimal Clean",
        "Typography-focused design with thin dividers. Ideal for design, academic, and creative roles.",
        Color(0xFF37474F), Color(0xFF546E7A), false,
        listOf("Elegant light typography", "Minimal line separators", "High white-space layout", "Clean, scannable format")
    ),
    TemplateInfo(
        "DEVELOPER_RESUME", "Developer Resume",
        "Dark theme with code-style section headings. Built for software engineers and tech roles.",
        Color(0xFF0D1117), Color(0xFF79C0FF), true,
        listOf("GitHub-inspired dark theme", "Code-comment section headers", "Monospace skill tags", "Tech-forward aesthetic")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Templates", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("Choose a template when creating your resume",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(templates.size) { idx ->
                val t = templates[idx]
                TemplatePreviewCard(t)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TemplatePreviewCard(template: TemplateInfo) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Mini preview thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(if (template.isDark) Color(0xFF0D1117) else Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(template.headerColor)
                            .padding(12.dp)
                    ) {
                        Column {
                            Box(Modifier.width(100.dp).height(10.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.9f)))
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(140.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.5f)))
                        }
                    }
                    // Content lines
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { i ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    Modifier.width(50.dp).height(6.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(template.accentColor.copy(alpha = 0.7f))
                                )
                                Box(
                                    Modifier.width((80 + i * 20).dp).height(6.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (template.isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f))
                                )
                            }
                        }
                        // Skill chips row
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(4) {
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(template.accentColor.copy(alpha = 0.15f))
                                        .border(1.dp, template.accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Box(Modifier.width(24.dp).height(5.dp).clip(RoundedCornerShape(2.dp))
                                        .background(template.accentColor.copy(alpha = 0.6f)))
                                }
                            }
                        }
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(16.dp)) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(template.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))
                template.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text(feature, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
