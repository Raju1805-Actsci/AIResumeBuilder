package com.airesume.builder.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ─── Domain Models ────────────────────────────────────────────────────────────

data class PersonalInfo(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val linkedin: String = "",
    val portfolio: String = "",
    val location: String = ""
)

data class Education(
    val id: Long = System.currentTimeMillis(),
    val degree: String = "",
    val college: String = "",
    val year: String = "",
    val gpa: String = ""
)

data class Experience(
    val id: Long = System.currentTimeMillis(),
    val company: String = "",
    val role: String = "",
    val duration: String = "",
    val description: String = "",
    val aiBulletPoints: List<String> = emptyList()
)

data class Project(
    val id: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val technologies: String = "",
    val githubUrl: String = "",
    val aiDescription: String = ""
)

data class Certification(
    val id: Long = System.currentTimeMillis(),
    val name: String = "",
    val organization: String = "",
    val year: String = ""
)

data class AiGeneratedContent(
    val professionalSummary: String = "",
    val suggestedSkills: List<String> = emptyList(),
    val atsKeywords: List<String> = emptyList(),
    val generatedAt: Long = 0L
)

// ─── Template Enum ────────────────────────────────────────────────────────────

enum class ResumeTemplate {
    MODERN_PROFESSIONAL,
    MINIMAL_CLEAN,
    DEVELOPER_RESUME
}

// ─── Room Entity ──────────────────────────────────────────────────────────────

@Entity(tableName = "resumes")
@TypeConverters(ResumeConverters::class)
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "My Resume",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val education: List<Education> = emptyList(),
    val skills: List<String> = emptyList(),
    val experience: List<Experience> = emptyList(),
    val projects: List<Project> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val aiContent: AiGeneratedContent = AiGeneratedContent(),
    val template: ResumeTemplate = ResumeTemplate.MODERN_PROFESSIONAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isComplete: Boolean = false
)

// ─── Type Converters ──────────────────────────────────────────────────────────

class ResumeConverters {
    private val gson = Gson()

    @TypeConverter fun personalInfoToJson(v: PersonalInfo): String = gson.toJson(v)
    @TypeConverter fun jsonToPersonalInfo(v: String): PersonalInfo =
        gson.fromJson(v, PersonalInfo::class.java) ?: PersonalInfo()

    @TypeConverter fun educationListToJson(v: List<Education>): String = gson.toJson(v)
    @TypeConverter fun jsonToEducationList(v: String): List<Education> =
        gson.fromJson(v, object : TypeToken<List<Education>>() {}.type) ?: emptyList()

    @TypeConverter fun experienceListToJson(v: List<Experience>): String = gson.toJson(v)
    @TypeConverter fun jsonToExperienceList(v: String): List<Experience> =
        gson.fromJson(v, object : TypeToken<List<Experience>>() {}.type) ?: emptyList()

    @TypeConverter fun projectListToJson(v: List<Project>): String = gson.toJson(v)
    @TypeConverter fun jsonToProjectList(v: String): List<Project> =
        gson.fromJson(v, object : TypeToken<List<Project>>() {}.type) ?: emptyList()

    @TypeConverter fun certListToJson(v: List<Certification>): String = gson.toJson(v)
    @TypeConverter fun jsonToCertList(v: String): List<Certification> =
        gson.fromJson(v, object : TypeToken<List<Certification>>() {}.type) ?: emptyList()

    @TypeConverter fun stringListToJson(v: List<String>): String = gson.toJson(v)
    @TypeConverter fun jsonToStringList(v: String): List<String> =
        gson.fromJson(v, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter fun aiContentToJson(v: AiGeneratedContent): String = gson.toJson(v)
    @TypeConverter fun jsonToAiContent(v: String): AiGeneratedContent =
        gson.fromJson(v, AiGeneratedContent::class.java) ?: AiGeneratedContent()

    @TypeConverter fun templateToString(v: ResumeTemplate): String = v.name
    @TypeConverter fun stringToTemplate(v: String): ResumeTemplate =
        runCatching { ResumeTemplate.valueOf(v) }.getOrDefault(ResumeTemplate.MODERN_PROFESSIONAL)
}
