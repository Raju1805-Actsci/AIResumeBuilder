package com.airesume.builder.network.aiService

/**
 * Unified result returned by the local AI engine.
 * Identical interface to what an API call would return —
 * the rest of the app doesn't need to know it's offline.
 */
data class AiResumeResult(
    val professionalSummary: String,
    val experienceBulletPoints: Map<String, List<String>>,   // company -> bullet list
    val enhancedProjectDescriptions: Map<String, String>,    // title   -> enhanced desc
    val suggestedSkills: List<String>,
    val atsKeywords: List<String>
)
