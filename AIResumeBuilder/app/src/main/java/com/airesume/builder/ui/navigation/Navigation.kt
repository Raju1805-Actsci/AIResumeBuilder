package com.airesume.builder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airesume.builder.ui.screens.dashboard.DashboardScreen
import com.airesume.builder.ui.screens.form.ResumeFormScreen
import com.airesume.builder.ui.screens.preview.ResumePreviewScreen
import com.airesume.builder.ui.screens.myresumes.MyResumesScreen
import com.airesume.builder.ui.screens.settings.SettingsScreen
import com.airesume.builder.ui.screens.templates.TemplatesScreen
import com.airesume.builder.ui.screens.improve.ImproveResumeScreen

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object MyResumes    : Screen("my_resumes")
    object Templates    : Screen("templates")
    object Settings     : Screen("settings")
    object ImproveResume: Screen("improve_resume/{resumeId}") {
        fun createRoute(resumeId: Long) = "improve_resume/$resumeId"
    }
    object ResumeForm   : Screen("resume_form?resumeId={resumeId}") {
        fun createRoute(resumeId: Long? = null) =
            if (resumeId != null) "resume_form?resumeId=$resumeId" else "resume_form"
    }
    object ResumePreview: Screen("resume_preview/{resumeId}") {
        fun createRoute(resumeId: Long) = "resume_preview/$resumeId"
    }
}

@Composable
fun AIResumeNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onCreateResume   = { navController.navigate(Screen.ResumeForm.createRoute()) },
                onMyResumes      = { navController.navigate(Screen.MyResumes.route) },
                onTemplates      = { navController.navigate(Screen.Templates.route) },
                onImproveResume  = { navController.navigate(Screen.MyResumes.route) },
                onSettings       = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            route = Screen.ResumeForm.route,
            arguments = listOf(navArgument("resumeId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { back ->
            val resumeId = back.arguments?.getLong("resumeId").takeIf { it != -1L }
            ResumeFormScreen(
                resumeId = resumeId,
                onNavigateBack    = { navController.popBackStack() },
                onPreviewResume   = { id -> navController.navigate(Screen.ResumePreview.createRoute(id)) }
            )
        }
        composable(
            route = Screen.ResumePreview.route,
            arguments = listOf(navArgument("resumeId") { type = NavType.LongType })
        ) { back ->
            val resumeId = back.arguments!!.getLong("resumeId")
            ResumePreviewScreen(
                resumeId      = resumeId,
                onNavigateBack = { navController.popBackStack() },
                onImprove      = { navController.navigate(Screen.ImproveResume.createRoute(resumeId)) }
            )
        }
        composable(Screen.MyResumes.route) {
            MyResumesScreen(
                onNavigateBack  = { navController.popBackStack() },
                onOpenResume    = { id -> navController.navigate(Screen.ResumePreview.createRoute(id)) },
                onEditResume    = { id -> navController.navigate(Screen.ResumeForm.createRoute(id)) },
                onImproveResume = { id -> navController.navigate(Screen.ImproveResume.createRoute(id)) }
            )
        }
        composable(Screen.Templates.route) {
            TemplatesScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ImproveResume.route,
            arguments = listOf(navArgument("resumeId") { type = NavType.LongType })
        ) { back ->
            val resumeId = back.arguments!!.getLong("resumeId")
            ImproveResumeScreen(
                resumeId      = resumeId,
                onNavigateBack = { navController.popBackStack() },
                onDone         = { id -> navController.navigate(Screen.ResumePreview.createRoute(id)) { popUpTo(Screen.ImproveResume.route) { inclusive = true } } }
            )
        }
    }
}
