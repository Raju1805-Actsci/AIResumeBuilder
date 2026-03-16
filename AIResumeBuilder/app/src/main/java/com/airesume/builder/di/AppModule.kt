package com.airesume.builder.di

import android.content.Context
import androidx.room.Room
import com.airesume.builder.data.database.ResumeDao
import com.airesume.builder.data.database.ResumeDatabase
import com.airesume.builder.utils.pdfGenerator.PdfShareUtil
import com.airesume.builder.utils.pdfGenerator.ResumePdfGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ResumeDatabase =
        Room.databaseBuilder(
            context,
            ResumeDatabase::class.java,
            ResumeDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideResumeDao(database: ResumeDatabase): ResumeDao = database.resumeDao()

    @Provides
    @Singleton
    fun provideResumePdfGenerator(@ApplicationContext context: Context): ResumePdfGenerator =
        ResumePdfGenerator(context)

    @Provides
    @Singleton
    fun providePdfShareUtil(@ApplicationContext context: Context): PdfShareUtil =
        PdfShareUtil(context)
}
