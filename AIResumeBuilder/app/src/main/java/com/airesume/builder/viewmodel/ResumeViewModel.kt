package com.airesume.builder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airesume.builder.data.database.*
import com.airesume.builder.data.repository.ResumeRepository
import com.airesume.builder.network.aiService.AiResumeService
import com.airesume.builder.utils.pdfGenerator.ResumePdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

data class ResumeUiState(
    val resume: ResumeEntity = ResumeEntity(),
    val currentStep: FormStep = FormStep.PERSONAL,
    val isLoading: Boolean = false,
    val isGeneratingAi: Boolean = false,
    val aiProgress: String = "",
    val error: String? = null,
    val isSaved: Boolean = false,
    val pdfFile: File? = null,
    val pdfSuccess: Boolean = false
)

enum class FormStep(val index: Int, val title: String) {
    PERSONAL(0, "Personal Info"),
    EDUCATION(1, "Education"),
    SKILLS(2, "Skills"),
    EXPERIENCE(3, "Experience"),
    PROJECTS(4, "Projects"),
    CERTIFICATIONS(5, "Certifications"),
    REVIEW(6, "Review & Generate")
}

data class DashboardUiState(
    val resumes: List<ResumeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val repository: ResumeRepository,
    private val aiService: AiResumeService,
    private val pdfGenerator: ResumePdfGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeUiState())
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    init {
        loadAllResumes()
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    private fun loadAllResumes() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isLoading = true) }
            repository.getAllResumes()
                .catch { e -> _dashboardState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _dashboardState.update { it.copy(resumes = list, isLoading = false, error = null) }
                }
        }
    }

    // ─── Form — Load / Init ───────────────────────────────────────────────────

    fun loadResume(resumeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val resume = repository.getResumeById(resumeId)
            if (resume != null) {
                _uiState.update { it.copy(resume = resume, isLoading = false, isSaved = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Resume not found") }
            }
        }
    }

    fun resetForm() {
        _uiState.value = ResumeUiState()
    }

    // ─── Step Navigation ──────────────────────────────────────────────────────

    fun nextStep() {
        val steps = FormStep.values()
        val idx = _uiState.value.currentStep.index
        if (idx < steps.lastIndex) {
            _uiState.update { it.copy(currentStep = steps[idx + 1]) }
        }
    }

    fun prevStep() {
        val steps = FormStep.values()
        val idx = _uiState.value.currentStep.index
        if (idx > 0) {
            _uiState.update { it.copy(currentStep = steps[idx - 1]) }
        }
    }

    fun goToStep(step: FormStep) = _uiState.update { it.copy(currentStep = step) }

    // ─── Field Updates ────────────────────────────────────────────────────────

    fun updatePersonalInfo(info: PersonalInfo) =
        _uiState.update { it.copy(resume = it.resume.copy(personalInfo = info)) }

    fun updateTitle(title: String) =
        _uiState.update { it.copy(resume = it.resume.copy(title = title)) }

    fun addEducation(edu: Education) =
        _uiState.update { it.copy(resume = it.resume.copy(education = it.resume.education + edu)) }

    fun updateEducation(edu: Education) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(
                education = s.resume.education.map { if (it.id == edu.id) edu else it }
            ))
        }

    fun removeEducation(id: Long) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(education = s.resume.education.filter { it.id != id }))
        }

    fun updateSkills(skills: List<String>) =
        _uiState.update { it.copy(resume = it.resume.copy(skills = skills)) }

    fun addExperience(exp: Experience) =
        _uiState.update { it.copy(resume = it.resume.copy(experience = it.resume.experience + exp)) }

    fun updateExperience(exp: Experience) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(
                experience = s.resume.experience.map { if (it.id == exp.id) exp else it }
            ))
        }

    fun removeExperience(id: Long) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(experience = s.resume.experience.filter { it.id != id }))
        }

    fun addProject(proj: Project) =
        _uiState.update { it.copy(resume = it.resume.copy(projects = it.resume.projects + proj)) }

    fun updateProject(proj: Project) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(
                projects = s.resume.projects.map { if (it.id == proj.id) proj else it }
            ))
        }

    fun removeProject(id: Long) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(projects = s.resume.projects.filter { it.id != id }))
        }

    fun addCertification(cert: Certification) =
        _uiState.update { it.copy(resume = it.resume.copy(certifications = it.resume.certifications + cert)) }

    fun updateCertification(cert: Certification) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(
                certifications = s.resume.certifications.map { if (it.id == cert.id) cert else it }
            ))
        }

    fun removeCertification(id: Long) =
        _uiState.update { s ->
            s.copy(resume = s.resume.copy(certifications = s.resume.certifications.filter { it.id != id }))
        }

    fun setTemplate(template: ResumeTemplate) =
        _uiState.update { it.copy(resume = it.resume.copy(template = template)) }

    // ─── Save ─────────────────────────────────────────────────────────────────

    fun saveResume() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val current = _uiState.value.resume
                val id = if (current.id == 0L) {
                    repository.saveResume(current)
                } else {
                    repository.updateResume(current)
                    current.id
                }
                val saved = repository.getResumeById(id)!!
                _uiState.update { it.copy(resume = saved, isLoading = false, isSaved = true, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Save failed: ${e.message}") }
            }
        }
    }

    // ─── AI Generation ────────────────────────────────────────────────────────

    fun generateAiContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, aiProgress = "Analyzing your profile…", error = null) }

            // Auto-save first so we have an ID
            val current = _uiState.value.resume
            val savedId = if (current.id == 0L) {
                repository.saveResume(current)
            } else {
                repository.updateResume(current); current.id
            }
            val saved = repository.getResumeById(savedId)!!
            _uiState.update { it.copy(resume = saved, aiProgress = "Sending to AI…") }

            aiService.generateResumeContent(saved)
                .onSuccess { result ->
                    _uiState.update { it.copy(aiProgress = "Processing AI response…") }

                    // Merge AI bullet points into experience entries
                    val updatedExperience = saved.experience.map { exp ->
                        val bullets = result.experienceBulletPoints[exp.company]
                            ?: result.experienceBulletPoints.values.firstOrNull()
                            ?: emptyList()
                        exp.copy(aiBulletPoints = bullets)
                    }

                    // Merge enhanced project descriptions
                    val updatedProjects = saved.projects.map { proj ->
                        val enhanced = result.enhancedProjectDescriptions[proj.title] ?: proj.description
                        proj.copy(aiDescription = enhanced)
                    }

                    val updatedResume = saved.copy(
                        experience = updatedExperience,
                        projects = updatedProjects,
                        skills = (saved.skills + result.suggestedSkills).distinct().take(20),
                        aiContent = AiGeneratedContent(
                            professionalSummary = result.professionalSummary,
                            suggestedSkills = result.suggestedSkills,
                            atsKeywords = result.atsKeywords,
                            generatedAt = System.currentTimeMillis()
                        ),
                        isComplete = true
                    )
                    repository.updateResume(updatedResume)
                    _uiState.update {
                        it.copy(
                            resume = updatedResume,
                            isGeneratingAi = false,
                            aiProgress = "",
                            isSaved = true,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAi = false,
                            aiProgress = "",
                            error = "AI generation failed: ${e.message}"
                        )
                    }
                }
        }
    }

    fun improveResume(resumeId: Long, feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, aiProgress = "Improving your resume…", error = null) }
            val resume = repository.getResumeById(resumeId) ?: run {
                _uiState.update { it.copy(isGeneratingAi = false, error = "Resume not found") }
                return@launch
            }
            aiService.improveResume(resume, feedback)
                .onSuccess { result ->
                    val updatedResume = resume.copy(
                        aiContent = resume.aiContent.copy(
                            professionalSummary = result.professionalSummary.ifEmpty { resume.aiContent.professionalSummary },
                            suggestedSkills = result.suggestedSkills,
                            atsKeywords = result.atsKeywords,
                            generatedAt = System.currentTimeMillis()
                        ),
                        skills = (resume.skills + result.suggestedSkills).distinct().take(20)
                    )
                    repository.updateResume(updatedResume)
                    _uiState.update {
                        it.copy(resume = updatedResume, isGeneratingAi = false, aiProgress = "", isSaved = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isGeneratingAi = false, error = "Improvement failed: ${e.message}") }
                }
        }
    }

    // ─── PDF ──────────────────────────────────────────────────────────────────

    fun generatePdf(resumeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, pdfSuccess = false, error = null) }
            val resume = repository.getResumeById(resumeId) ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Resume not found") }
                return@launch
            }
            pdfGenerator.generatePdf(resume)
                .onSuccess { file ->
                    _uiState.update { it.copy(isLoading = false, pdfFile = file, pdfSuccess = true, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "PDF failed: ${e.message}") }
                }
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    fun deleteResume(id: Long) {
        viewModelScope.launch {
            repository.deleteResume(id)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearPdfState() = _uiState.update { it.copy(pdfSuccess = false, pdfFile = null) }
}
