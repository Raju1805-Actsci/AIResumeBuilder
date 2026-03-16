package com.airesume.builder.network.aiService

import com.airesume.builder.data.database.ResumeEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resume service that delegates to the fully-offline LocalAiEngine.
 * No internet connection or API key required.
 */
@Singleton
class AiResumeService @Inject constructor(
    private val localAiEngine: LocalAiEngine
) {
    suspend fun generateResumeContent(resume: ResumeEntity): Result<AiResumeResult> =
        localAiEngine.generateResumeContent(resume)

    suspend fun improveResume(resume: ResumeEntity, feedback: String): Result<AiResumeResult> =
        localAiEngine.improveResume(resume, feedback)
}
