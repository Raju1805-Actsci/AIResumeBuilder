package com.airesume.builder.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ResumeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ResumeConverters::class)
abstract class ResumeDatabase : RoomDatabase() {
    abstract fun resumeDao(): ResumeDao

    companion object {
        const val DATABASE_NAME = "ai_resume_db"
    }
}
