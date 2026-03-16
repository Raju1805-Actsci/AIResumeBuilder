package com.airesume.builder.ui.screens.preview

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesume.builder.data.database.*
import com.airesume.builder.utils.pdfGenerator.PdfShareUtil
import com.airesume.builder.viewmodel.ResumeViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumePreviewScreen(
    resumeId: Long,
    onNavigateBack: () -> Unit,
    onImprove: () -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(resumeId) { viewModel.loadResume(resumeId) }

    // Handle PDF success
    LaunchedEffect(uiState.pdfSuccess, uiState.pdfFile) {
        if (uiState.pdfSuccess && uiState.pdfFile != null) {
            // Trigger share
            val shareIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM,
                        androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", uiState.pdfFile!!
                        )
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share Resume PDF"
            )
            context.startActivity(shareIntent)
            viewModel.clearPdfState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onImprove) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Improve",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onImprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI Improve")
                    }
                    Button(
                        onClick = { viewModel.generatePdf(resumeId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Export PDF")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.isLoading && uiState.resume.id == 0L) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ResumeDocument(resume = uiState.resume)
            }
        }
    }
}

// ─── Resume document rendered in Compose ─────────────────────────────────────

@Composable
fun ResumeDocument(resume: ResumeEntity) {
    when (resume.template) {
        ResumeTemplate.MODERN_PROFESSIONAL -> ModernResumePreview(resume)
        ResumeTemplate.MINIMAL_CLEAN       -> MinimalResumePreview(resume)
        ResumeTemplate.DEVELOPER_RESUME    -> DeveloperResumePreview(resume)
    }
}

@Composable
private fun ModernResumePreview(resume: ResumeEntity) {
    val navy = Color(0xFF1A237E)
    val lightBlue = Color(0xFFE8EAF6)
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        // Header
        Box(Modifier.fillMaxWidth().background(navy).padding(20.dp)) {
            Column {
                Text(resume.personalInfo.fullName.ifEmpty { "Your Name" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                val contact = listOfNotNull(
                    resume.personalInfo.email.takeIf { it.isNotEmpty() },
                    resume.personalInfo.phone.takeIf { it.isNotEmpty() },
                    resume.personalInfo.location.takeIf { it.isNotEmpty() }
                ).joinToString("  ·  ")
                if (contact.isNotEmpty()) Text(contact, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                val links = listOfNotNull(
                    resume.personalInfo.linkedin.takeIf { it.isNotEmpty() },
                    resume.personalInfo.portfolio.takeIf { it.isNotEmpty() }
                ).joinToString("  ·  ")
                if (links.isNotEmpty()) Text(links, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
               verticalArrangement = Arrangement.spacedBy(16.dp)) {

            if (resume.aiContent.professionalSummary.isNotEmpty()) {
                PreviewSection("PROFESSIONAL SUMMARY", navy) {
                    Text(resume.aiContent.professionalSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF424242), lineHeight = 22.sp)
                }
            }

            val allSkills = (resume.skills + resume.aiContent.suggestedSkills).distinct()
            if (allSkills.isNotEmpty()) {
                PreviewSection("SKILLS", navy) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()) {
                        allSkills.take(18).forEach { skill ->
                            AssistChip(
                                onClick = {},
                                label = { Text(skill, style = MaterialTheme.typography.labelSmall) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = lightBlue)
                            )
                        }
                    }
                }
            }

            if (resume.experience.any { it.company.isNotEmpty() }) {
                PreviewSection("EXPERIENCE", navy) {
                    resume.experience.filter { it.company.isNotEmpty() }.forEach { exp ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(exp.role, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall, color = Color(0xFF212121))
                                Text(exp.duration, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                            }
                            Text(exp.company, style = MaterialTheme.typography.bodySmall, color = navy)
                            Spacer(Modifier.height(4.dp))
                            val bullets = exp.aiBulletPoints.takeIf { it.isNotEmpty() } ?: listOf(exp.description)
                            bullets.forEach { bullet ->
                                if (bullet.isNotEmpty()) Text(bullet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF424242), lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                    }
                }
            }

            if (resume.projects.any { it.title.isNotEmpty() }) {
                PreviewSection("PROJECTS", navy) {
                    resume.projects.filter { it.title.isNotEmpty() }.forEach { proj ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(proj.title, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall)
                                if (proj.technologies.isNotEmpty())
                                    Text(proj.technologies, style = MaterialTheme.typography.labelSmall, color = navy)
                            }
                            val desc = proj.aiDescription.ifEmpty { proj.description }
                            if (desc.isNotEmpty()) Text(desc, style = MaterialTheme.typography.bodySmall, color = Color(0xFF424242))
                        }
                    }
                }
            }

            if (resume.education.any { it.degree.isNotEmpty() }) {
                PreviewSection("EDUCATION", navy) {
                    resume.education.filter { it.degree.isNotEmpty() }.forEach { edu ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(edu.degree, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                                Text(edu.college, style = MaterialTheme.typography.bodySmall, color = Color(0xFF616161))
                            }
                            Text(edu.year, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                        }
                    }
                }
            }

            if (resume.certifications.any { it.name.isNotEmpty() }) {
                PreviewSection("CERTIFICATIONS", navy) {
                    resume.certifications.filter { it.name.isNotEmpty() }.forEach { cert ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• ${cert.name} — ${cert.organization}",
                                style = MaterialTheme.typography.bodySmall)
                            Text(cert.year, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalResumePreview(resume: ResumeEntity) {
    val textDark = Color(0xFF212121)
    val textMid  = Color(0xFF616161)
    val divider  = Color(0xFFBDBDBD)

    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(24.dp),
           verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(resume.personalInfo.fullName.ifEmpty { "Your Name" },
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
            color = textDark)
        HorizontalDivider(color = divider, thickness = 0.5.dp)
        val contact = listOfNotNull(
            resume.personalInfo.email.takeIf { it.isNotEmpty() },
            resume.personalInfo.phone.takeIf { it.isNotEmpty() },
            resume.personalInfo.location.takeIf { it.isNotEmpty() },
            resume.personalInfo.linkedin.takeIf { it.isNotEmpty() }
        ).joinToString("  |  ")
        if (contact.isNotEmpty()) Text(contact, style = MaterialTheme.typography.bodySmall, color = textMid)
        if (resume.aiContent.professionalSummary.isNotEmpty())
            Text(resume.aiContent.professionalSummary, style = MaterialTheme.typography.bodyMedium, color = textDark, lineHeight = 22.sp)
        val allSkills = (resume.skills + resume.aiContent.suggestedSkills).distinct()
        if (allSkills.isNotEmpty()) {
            MinimalSection("SKILLS", divider)
            Text(allSkills.joinToString("  ·  "), style = MaterialTheme.typography.bodySmall, color = textDark)
        }
        resume.experience.filter { it.company.isNotEmpty() }.forEach { exp ->
            MinimalSection("EXPERIENCE", divider)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(exp.role, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(exp.duration, style = MaterialTheme.typography.bodySmall, color = textMid)
            }
            Text(exp.company, style = MaterialTheme.typography.bodySmall, color = Color(0xFF37474F))
            exp.aiBulletPoints.takeIf { it.isNotEmpty() }?.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = textMid, lineHeight = 20.sp)
            } ?: Text(exp.description, style = MaterialTheme.typography.bodySmall, color = textMid)
        }
        resume.education.filter { it.degree.isNotEmpty() }.forEach { edu ->
            MinimalSection("EDUCATION", divider)
            Text(edu.degree, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text("${edu.college}  •  ${edu.year}", style = MaterialTheme.typography.bodySmall, color = textMid)
        }
    }
}

@Composable
private fun DeveloperResumePreview(resume: ResumeEntity) {
    val darkBg  = Color(0xFF0D1117)
    val codeGreen = Color(0xFF238636)
    val codeBlue  = Color(0xFF79C0FF)
    val textLight = Color(0xFFC9D1D9)
    val textMuted = Color(0xFF8B949E)

    Column(modifier = Modifier.fillMaxWidth().background(darkBg)) {
        Box(Modifier.fillMaxWidth().background(Color(0xFF161B22)).padding(20.dp)) {
            Column {
                Text(resume.personalInfo.fullName.ifEmpty { "Developer" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                val contact = listOfNotNull(
                    resume.personalInfo.email.takeIf { it.isNotEmpty() },
                    resume.personalInfo.phone.takeIf { it.isNotEmpty() }
                ).joinToString("  ·  ")
                if (contact.isNotEmpty()) Text(contact, style = MaterialTheme.typography.bodySmall, color = codeBlue)
                if (resume.personalInfo.linkedin.isNotEmpty() || resume.personalInfo.portfolio.isNotEmpty())
                    Text(listOfNotNull(resume.personalInfo.linkedin.takeIf { it.isNotEmpty() },
                        resume.personalInfo.portfolio.takeIf { it.isNotEmpty() }).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall, color = textMuted)
            }
        }
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (resume.aiContent.professionalSummary.isNotEmpty()) {
                DevSection("// summary")
                Text(resume.aiContent.professionalSummary, style = MaterialTheme.typography.bodySmall, color = textLight, lineHeight = 20.sp)
            }
            val allSkills = (resume.skills + resume.aiContent.suggestedSkills).distinct()
            if (allSkills.isNotEmpty()) {
                DevSection("// tech_stack")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    allSkills.take(16).forEach { skill ->
                        Box(Modifier.border(1.dp, codeGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(skill, style = MaterialTheme.typography.labelSmall, color = codeGreen)
                        }
                    }
                }
            }
            resume.experience.filter { it.company.isNotEmpty() }.forEach { exp ->
                DevSection("// work_experience")
                Text(exp.role, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = codeBlue)
                Text(exp.company, style = MaterialTheme.typography.bodySmall, color = codeGreen)
                Text(exp.duration, style = MaterialTheme.typography.bodySmall, color = textMuted)
                val bullets = exp.aiBulletPoints.takeIf { it.isNotEmpty() } ?: listOf(exp.description)
                bullets.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = textLight, lineHeight = 20.sp) }
            }
            resume.education.filter { it.degree.isNotEmpty() }.forEach { edu ->
                DevSection("// education")
                Text(edu.degree, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Medium)
                Text("${edu.college}  •  ${edu.year}", style = MaterialTheme.typography.bodySmall, color = textMuted)
            }
        }
    }
}

@Composable
private fun PreviewSection(title: String, accentColor: Color, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(16.dp).background(accentColor))
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = accentColor,
                letterSpacing = 1.sp)
        }
        HorizontalDivider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
        content()
    }
}

@Composable
private fun MinimalSection(title: String, dividerColor: Color) {
    Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF37474F),
        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
}

@Composable
private fun DevSection(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall,
        color = Color(0xFFA371F7), fontWeight = FontWeight.Bold)
}
