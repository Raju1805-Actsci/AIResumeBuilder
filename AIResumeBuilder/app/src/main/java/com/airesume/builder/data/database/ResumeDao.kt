package com.airesume.builder.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {

    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    fun getAllResumes(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: Long): ResumeEntity?

    @Query("SELECT * FROM resumes WHERE id = :id")
    fun getResumeByIdFlow(id: Long): Flow<ResumeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Update
    suspend fun updateResume(resume: ResumeEntity)

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    @Query("DELETE FROM resumes WHERE id = :id")
    suspend fun deleteResumeById(id: Long)

    @Query("SELECT COUNT(*) FROM resumes")
    suspend fun getResumeCount(): Int

    @Query("UPDATE resumes SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())
}
