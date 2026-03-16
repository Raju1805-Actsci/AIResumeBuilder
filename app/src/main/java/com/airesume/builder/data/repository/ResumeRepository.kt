package com.airesume.builder.data.repository

import com.airesume.builder.data.database.ResumeDao
import com.airesume.builder.data.database.ResumeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeRepository @Inject constructor(
    private val resumeDao: ResumeDao
) {
    fun getAllResumes(): Flow<List<ResumeEntity>> = resumeDao.getAllResumes()

    suspend fun getResumeById(id: Long): ResumeEntity? = resumeDao.getResumeById(id)

    fun getResumeByIdFlow(id: Long): Flow<ResumeEntity?> = resumeDao.getResumeByIdFlow(id)

    suspend fun saveResume(resume: ResumeEntity): Long = resumeDao.insertResume(resume)

    suspend fun updateResume(resume: ResumeEntity) {
        resumeDao.updateResume(resume.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteResume(id: Long) = resumeDao.deleteResumeById(id)

    suspend fun getResumeCount(): Int = resumeDao.getResumeCount()
}
