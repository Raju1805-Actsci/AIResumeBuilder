package com.airesume.builder.ui.screens.form

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesume.builder.data.database.*
import com.airesume.builder.ui.components.*
import com.airesume.builder.viewmodel.FormStep
import com.airesume.builder.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeFormScreen(
    resumeId: Long?,
    onNavigateBack: () -> Unit,
    onPreviewResume: (Long) -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val steps = FormStep.values().toList()

    LaunchedEffect(resumeId) {
        if (resumeId != null) viewModel.loadResume(resumeId)
        else viewModel.resetForm()
    }

    // Navigate to preview when AI generation completes successfully
    LaunchedEffect(uiState.isGeneratingAi, uiState.resume.isComplete) {
        if (!uiState.isGeneratingAi && uiState.resume.isComplete && uiState.resume.id != 0L) {
            onPreviewResume(uiState.resume.id)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentStep.title,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.saveResume() }) {
                            Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
                // Step progress
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                    StepProgressIndicator(
                        steps = steps,
                        currentStep = uiState.currentStep,
                        onStepClick = { viewModel.goToStep(it) }
                    )
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentStep.index > 0) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back")
                        }
                    }

                    if (uiState.currentStep == FormStep.REVIEW) {
                        AiGenerateButton(
                            onClick = { viewModel.generateAiContent() },
                            isLoading = uiState.isGeneratingAi,
                            progress = uiState.aiProgress,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        // Error snackbar
        uiState.error?.let { err ->
            LaunchedEffect(err) {
                // shown inline below
            }
        }

        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                if (targetState.index > initialState.index) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "step_anim"
        ) { step ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Error banner
                uiState.error?.let { err ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        }
                    }
                }

                when (step) {
                    FormStep.PERSONAL      -> item { PersonalInfoStep(uiState.resume.personalInfo, uiState.resume.title, viewModel) }
                    FormStep.EDUCATION     -> item { EducationStep(uiState.resume.education, viewModel) }
                    FormStep.SKILLS        -> item { SkillsStep(uiState.resume.skills, viewModel) }
                    FormStep.EXPERIENCE    -> item { ExperienceStep(uiState.resume.experience, viewModel) }
                    FormStep.PROJECTS      -> item { ProjectsStep(uiState.resume.projects, viewModel) }
                    FormStep.CERTIFICATIONS-> item { CertificationsStep(uiState.resume.certifications, viewModel) }
                    FormStep.REVIEW        -> item { ReviewStep(uiState.resume, viewModel) }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─── Step: Personal Info ──────────────────────────────────────────────────────

@Composable
private fun PersonalInfoStep(info: PersonalInfo, title: String, vm: ResumeViewModel) {
    SectionCard("Personal Information", Icons.Default.Person) {
        ResumeTextField(title, { vm.updateTitle(it) }, "Resume Title", isRequired = true,
            leadingIcon = Icons.Default.Title)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.fullName, { vm.updatePersonalInfo(info.copy(fullName = it)) },
            "Full Name", isRequired = true, leadingIcon = Icons.Default.Person)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.email, { vm.updatePersonalInfo(info.copy(email = it)) },
            "Email", isRequired = true, keyboardType = KeyboardType.Email, leadingIcon = Icons.Default.Email)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.phone, { vm.updatePersonalInfo(info.copy(phone = it)) },
            "Phone", keyboardType = KeyboardType.Phone, leadingIcon = Icons.Default.Phone)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.location, { vm.updatePersonalInfo(info.copy(location = it)) },
            "Location", placeholder = "City, State", leadingIcon = Icons.Default.LocationOn)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.linkedin, { vm.updatePersonalInfo(info.copy(linkedin = it)) },
            "LinkedIn URL", leadingIcon = Icons.Default.Link)
        Spacer(Modifier.height(12.dp))
        ResumeTextField(info.portfolio, { vm.updatePersonalInfo(info.copy(portfolio = it)) },
            "Portfolio / Website", leadingIcon = Icons.Default.Language)
    }
}

// ─── Step: Education ──────────────────────────────────────────────────────────

@Composable
private fun EducationStep(education: List<Education>, vm: ResumeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        education.forEach { edu ->
            EducationCard(edu,
                onUpdate = { vm.updateEducation(it) },
                onRemove = { vm.removeEducation(edu.id) },
                canRemove = education.size > 1)
        }
        OutlinedButton(
            onClick = { vm.addEducation(Education()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Education")
        }
    }
}

@Composable
private fun EducationCard(edu: Education, onUpdate: (Education) -> Unit, onRemove: () -> Unit, canRemove: Boolean) {
    SectionCard("Education", Icons.Default.School) {
        if (canRemove) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
        ResumeTextField(edu.degree, { onUpdate(edu.copy(degree = it)) }, "Degree / Program", isRequired = true)
        Spacer(Modifier.height(10.dp))
        ResumeTextField(edu.college, { onUpdate(edu.copy(college = it)) }, "Institution", isRequired = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResumeTextField(edu.year, { onUpdate(edu.copy(year = it)) }, "Graduation Year",
                keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            ResumeTextField(edu.gpa, { onUpdate(edu.copy(gpa = it)) }, "GPA (opt.)",
                modifier = Modifier.weight(1f))
        }
    }
}

// ─── Step: Skills ─────────────────────────────────────────────────────────────

@Composable
private fun SkillsStep(skills: List<String>, vm: ResumeViewModel) {
    var inputText by remember { mutableStateOf("") }

    SectionCard("Skills", Icons.Default.Psychology) {
        Text("Enter your skills and press ＋ to add them",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResumeTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = "Add a skill",
                placeholder = "e.g. Python, React, AWS",
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty() && !skills.contains(trimmed)) {
                        vm.updateSkills(skills + trimmed)
                        inputText = ""
                    }
                },
                enabled = inputText.trim().isNotEmpty()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add skill")
            }
        }
        Spacer(Modifier.height(12.dp))
        if (skills.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                skills.forEach { skill ->
                    SkillChip(skill, onRemove = { vm.updateSkills(skills - skill) })
                }
            }
        } else {
            Text("No skills added yet. Add at least 3-5 key skills.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text("💡 Quick add popular skills:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        val suggestions = listOf("Python","JavaScript","React","Node.js","SQL","Git","Docker","AWS","Java","Kotlin","TypeScript","Figma")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions.filter { !skills.contains(it) }) { s ->
                SuggestionChip(
                    onClick = { vm.updateSkills(skills + s) },
                    label = { Text(s, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

// ─── Step: Experience ─────────────────────────────────────────────────────────

@Composable
private fun ExperienceStep(experience: List<Experience>, vm: ResumeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        experience.forEach { exp ->
            ExperienceCard(exp,
                onUpdate = { vm.updateExperience(it) },
                onRemove = { vm.removeExperience(exp.id) },
                canRemove = experience.size > 1)
        }
        OutlinedButton(
            onClick = { vm.addExperience(Experience()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Experience")
        }
    }
}

@Composable
private fun ExperienceCard(exp: Experience, onUpdate: (Experience) -> Unit, onRemove: () -> Unit, canRemove: Boolean) {
    SectionCard("Work Experience", Icons.Default.Work) {
        if (canRemove) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
        ResumeTextField(exp.company, { onUpdate(exp.copy(company = it)) }, "Company Name", isRequired = true)
        Spacer(Modifier.height(10.dp))
        ResumeTextField(exp.role, { onUpdate(exp.copy(role = it)) }, "Job Title / Role", isRequired = true)
        Spacer(Modifier.height(10.dp))
        ResumeTextField(exp.duration, { onUpdate(exp.copy(duration = it)) }, "Duration",
            placeholder = "Jan 2022 – Present")
        Spacer(Modifier.height(10.dp))
        ResumeTextField(exp.description, { onUpdate(exp.copy(description = it)) },
            "Brief Description",
            placeholder = "Describe your responsibilities and achievements…",
            singleLine = false, maxLines = 5)
        Text("💡 AI will enhance this into professional bullet points",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp))
    }
}

// ─── Step: Projects ───────────────────────────────────────────────────────────

@Composable
private fun ProjectsStep(projects: List<Project>, vm: ResumeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        projects.forEach { proj ->
            ProjectCard(proj,
                onUpdate = { vm.updateProject(it) },
                onRemove = { vm.removeProject(proj.id) },
                canRemove = projects.size > 1)
        }
        OutlinedButton(
            onClick = { vm.addProject(Project()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Project")
        }
    }
}

@Composable
private fun ProjectCard(proj: Project, onUpdate: (Project) -> Unit, onRemove: () -> Unit, canRemove: Boolean) {
    SectionCard("Project", Icons.Default.Code) {
        if (canRemove) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
        ResumeTextField(proj.title, { onUpdate(proj.copy(title = it)) }, "Project Title", isRequired = true)
        Spacer(Modifier.height(10.dp))
        ResumeTextField(proj.technologies, { onUpdate(proj.copy(technologies = it)) },
            "Technologies Used", placeholder = "React, Node.js, MongoDB")
        Spacer(Modifier.height(10.dp))
        ResumeTextField(proj.githubUrl, { onUpdate(proj.copy(githubUrl = it)) },
            "GitHub URL", leadingIcon = Icons.Default.Link)
        Spacer(Modifier.height(10.dp))
        ResumeTextField(proj.description, { onUpdate(proj.copy(description = it)) },
            "Short Description", singleLine = false, maxLines = 4,
            placeholder = "Brief description of what you built and its impact…")
    }
}

// ─── Step: Certifications ─────────────────────────────────────────────────────

@Composable
private fun CertificationsStep(certifications: List<Certification>, vm: ResumeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        certifications.forEach { cert ->
            CertCard(cert,
                onUpdate = { vm.updateCertification(it) },
                onRemove = { vm.removeCertification(cert.id) },
                canRemove = certifications.size > 1)
        }
        OutlinedButton(
            onClick = { vm.addCertification(Certification()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add Certification")
        }
    }
}

@Composable
private fun CertCard(cert: Certification, onUpdate: (Certification) -> Unit, onRemove: () -> Unit, canRemove: Boolean) {
    SectionCard("Certification", Icons.Default.EmojiEvents) {
        if (canRemove) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
        ResumeTextField(cert.name, { onUpdate(cert.copy(name = it)) }, "Certification Name", isRequired = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResumeTextField(cert.organization, { onUpdate(cert.copy(organization = it)) },
                "Organization", modifier = Modifier.weight(2f))
            ResumeTextField(cert.year, { onUpdate(cert.copy(year = it)) },
                "Year", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
        }
    }
}

// ─── Step: Review & Generate ──────────────────────────────────────────────────

@Composable
private fun ReviewStep(resume: ResumeEntity, vm: ResumeViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Template picker
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Select Template", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                ResumeTemplate.values().forEach { template ->
                    val selected = resume.template == template
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = { vm.setTemplate(template) }
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { vm.setTemplate(template) })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    template.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    when (template) {
                                        ResumeTemplate.MODERN_PROFESSIONAL -> "Blue accent, two-column layout"
                                        ResumeTemplate.MINIMAL_CLEAN -> "Clean typography, simple layout"
                                        ResumeTemplate.DEVELOPER_RESUME -> "Dark theme, code-style headings"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary card
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Summarize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Resume Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                ReviewRow("Name", resume.personalInfo.fullName)
                ReviewRow("Email", resume.personalInfo.email)
                ReviewRow("Education", resume.education.firstOrNull()?.degree ?: "—")
                ReviewRow("Skills", "${resume.skills.size} added")
                ReviewRow("Experience", "${resume.experience.size} positions")
                ReviewRow("Projects", "${resume.projects.size} projects")
            }
        }

        // AI info banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("AI will generate:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    listOf(
                        "✨ Professional summary",
                        "🎯 ATS-optimized bullet points",
                        "📈 Enhanced project descriptions",
                        "🏷️ Additional skill suggestions",
                        "🔑 Relevant ATS keywords"
                    ).forEach {
                        Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifEmpty { "—" }, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium)
    }
}
