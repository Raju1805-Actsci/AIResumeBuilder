package com.airesume.builder.utils.pdfGenerator

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.airesume.builder.data.database.*
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumePdfGenerator @Inject constructor(
    private val context: Context
) {

    companion object {
        const val PAGE_WIDTH = 595   // A4 at 72dpi
        const val PAGE_HEIGHT = 842
        const val MARGIN = 40f
        const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    fun generatePdf(resume: ResumeEntity): Result<File> = runCatching {
        val file = createOutputFile(resume.personalInfo.fullName)
        val pdfDocument = PdfDocument()
        val pages = mutableListOf<PdfDocument.Page>()

        when (resume.template) {
            ResumeTemplate.MODERN_PROFESSIONAL -> drawModernProfessional(pdfDocument, resume, pages)
            ResumeTemplate.MINIMAL_CLEAN -> drawMinimalClean(pdfDocument, resume, pages)
            ResumeTemplate.DEVELOPER_RESUME -> drawDeveloperResume(pdfDocument, resume, pages)
        }

        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pages.forEach { pdfDocument.finishPage(it) }
        pdfDocument.close()
        file
    }

    // ─── Template: Modern Professional ───────────────────────────────────────

    private fun drawModernProfessional(
        doc: PdfDocument,
        resume: ResumeEntity,
        pages: MutableList<PdfDocument.Page>
    ) {
        val renderer = PageRenderer(doc, pages)

        // Color palette
        val accentColor = Color.parseColor("#1A237E") // Deep navy
        val lightAccent = Color.parseColor("#E8EAF6")
        val textDark = Color.parseColor("#212121")
        val textMedium = Color.parseColor("#616161")

        // Header background
        renderer.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, accentColor)

        // Name
        renderer.drawText(
            resume.personalInfo.fullName.ifEmpty { "Your Name" },
            MARGIN, 45f,
            paint(28f, Color.WHITE, Typeface.BOLD)
        )

        // Contact info in header
        val contact = buildContactLine(resume.personalInfo)
        renderer.drawText(contact, MARGIN, 70f, paint(9f, Color.WHITE))

        val p2 = buildContactLine2(resume.personalInfo)
        if (p2.isNotEmpty()) renderer.drawText(p2, MARGIN, 85f, paint(9f, Color.WHITE))

        var y = 135f

        // Professional Summary
        if (resume.aiContent.professionalSummary.isNotEmpty()) {
            y = renderer.drawSection("PROFESSIONAL SUMMARY", y, accentColor, lightAccent, textDark)
            y = renderer.drawWrappedText(resume.aiContent.professionalSummary, y, paint(10f, textDark))
            y += 8f
        }

        // Skills
        if (resume.skills.isNotEmpty()) {
            val allSkills = (resume.skills + resume.aiContent.suggestedSkills).distinct()
            y = renderer.drawSection("SKILLS", y, accentColor, lightAccent, textDark)
            y = renderer.drawSkillTags(allSkills, y, accentColor, lightAccent)
            y += 8f
        }

        // Experience
        if (resume.experience.isNotEmpty()) {
            y = renderer.drawSection("EXPERIENCE", y, accentColor, lightAccent, textDark)
            for (exp in resume.experience) {
                renderer.checkNewPage(y, 80f)
                // Role & Company
                renderer.drawText(exp.role, MARGIN, y, paint(11f, textDark, Typeface.BOLD))
                y += 15f
                renderer.drawText(
                    "${exp.company}  |  ${exp.duration}",
                    MARGIN, y, paint(9f, accentColor)
                )
                y += 14f
                // AI bullet points or raw description
                val bullets = resume.experience
                    .find { it.company == exp.company }
                    ?.aiBulletPoints?.takeIf { it.isNotEmpty() }
                    ?: listOf(exp.description)
                for (bullet in bullets) {
                    y = renderer.drawWrappedText(bullet, y, paint(9.5f, textMedium), MARGIN + 8f)
                }
                y += 6f
            }
        }

        // Projects
        if (resume.projects.isNotEmpty()) {
            y = renderer.drawSection("PROJECTS", y, accentColor, lightAccent, textDark)
            for (proj in resume.projects) {
                renderer.checkNewPage(y, 60f)
                renderer.drawText(proj.title, MARGIN, y, paint(10.5f, textDark, Typeface.BOLD))
                if (proj.technologies.isNotEmpty()) {
                    renderer.drawText(
                        proj.technologies, PAGE_WIDTH - MARGIN - 150f, y,
                        paint(8.5f, accentColor)
                    )
                }
                y += 14f
                val desc = proj.aiDescription.ifEmpty { proj.description }
                y = renderer.drawWrappedText(desc, y, paint(9.5f, textMedium))
                y += 6f
            }
        }

        // Education
        if (resume.education.isNotEmpty()) {
            y = renderer.drawSection("EDUCATION", y, accentColor, lightAccent, textDark)
            for (edu in resume.education) {
                renderer.drawText(edu.degree, MARGIN, y, paint(10.5f, textDark, Typeface.BOLD))
                y += 14f
                renderer.drawText(
                    "${edu.college}  |  ${edu.year}${if (edu.gpa.isNotEmpty()) "  |  GPA: ${edu.gpa}" else ""}",
                    MARGIN, y, paint(9f, textMedium)
                )
                y += 14f
            }
        }

        // Certifications
        if (resume.certifications.isNotEmpty()) {
            y = renderer.drawSection("CERTIFICATIONS", y, accentColor, lightAccent, textDark)
            for (cert in resume.certifications) {
                renderer.drawText(
                    "• ${cert.name}  —  ${cert.organization}${if (cert.year.isNotEmpty()) " (${cert.year})" else ""}",
                    MARGIN, y, paint(9.5f, textDark)
                )
                y += 14f
            }
        }

        renderer.finalize()
    }

    // ─── Template: Minimal Clean ──────────────────────────────────────────────

    private fun drawMinimalClean(
        doc: PdfDocument,
        resume: ResumeEntity,
        pages: MutableList<PdfDocument.Page>
    ) {
        val renderer = PageRenderer(doc, pages)
        val accentColor = Color.parseColor("#37474F")
        val textDark = Color.parseColor("#212121")
        val textMedium = Color.parseColor("#757575")
        val dividerColor = Color.parseColor("#BDBDBD")

        var y = MARGIN + 10f

        // Name — large elegant
        renderer.drawText(
            resume.personalInfo.fullName.ifEmpty { "Your Name" },
            MARGIN, y, paint(30f, textDark, Typeface.DEFAULT)
        )
        y += 35f

        // Thin divider
        renderer.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, dividerColor, 0.5f)
        y += 10f

        // Contact
        val contact = buildContactLine(resume.personalInfo)
        renderer.drawText(contact, MARGIN, y, paint(8.5f, textMedium))
        y += 20f

        if (resume.aiContent.professionalSummary.isNotEmpty()) {
            y = renderer.drawWrappedText(resume.aiContent.professionalSummary, y, paint(10f, textDark))
            y += 16f
        }

        fun minimalSection(title: String): Float {
            renderer.drawText(title, MARGIN, y, paint(9f, accentColor, Typeface.BOLD))
            val ty = y + 12f
            renderer.drawLine(MARGIN, ty, PAGE_WIDTH - MARGIN, ty, dividerColor, 0.5f)
            return ty + 10f
        }

        if (resume.skills.isNotEmpty()) {
            var sy = minimalSection("SKILLS")
            val all = (resume.skills + resume.aiContent.suggestedSkills).distinct()
            renderer.drawText(all.joinToString("  ·  "), MARGIN, sy, paint(9.5f, textDark))
            sy += 20f
            y = sy
        }

        if (resume.experience.isNotEmpty()) {
            y = minimalSection("EXPERIENCE")
            for (exp in resume.experience) {
                renderer.drawText(exp.role, MARGIN, y, paint(10.5f, textDark, Typeface.BOLD))
                renderer.drawText(exp.duration, PAGE_WIDTH - MARGIN - 100f, y, paint(9f, textMedium))
                y += 14f
                renderer.drawText(exp.company, MARGIN, y, paint(9f, accentColor))
                y += 14f
                val bullets = exp.aiBulletPoints.takeIf { it.isNotEmpty() } ?: listOf(exp.description)
                for (b in bullets) {
                    y = renderer.drawWrappedText(b, y, paint(9f, textMedium))
                }
                y += 8f
            }
        }

        if (resume.projects.isNotEmpty()) {
            y = minimalSection("PROJECTS")
            for (proj in resume.projects) {
                renderer.drawText(proj.title, MARGIN, y, paint(10.5f, textDark, Typeface.BOLD))
                y += 14f
                val desc = proj.aiDescription.ifEmpty { proj.description }
                y = renderer.drawWrappedText(desc, y, paint(9f, textMedium))
                y += 8f
            }
        }

        if (resume.education.isNotEmpty()) {
            y = minimalSection("EDUCATION")
            for (edu in resume.education) {
                renderer.drawText(edu.degree, MARGIN, y, paint(10.5f, textDark, Typeface.BOLD))
                y += 14f
                renderer.drawText("${edu.college}  •  ${edu.year}", MARGIN, y, paint(9f, textMedium))
                y += 14f
            }
        }

        if (resume.certifications.isNotEmpty()) {
            y = minimalSection("CERTIFICATIONS")
            for (cert in resume.certifications) {
                renderer.drawText("${cert.name}  —  ${cert.organization}", MARGIN, y, paint(9.5f, textDark))
                y += 14f
            }
        }

        renderer.finalize()
    }

    // ─── Template: Developer Resume ───────────────────────────────────────────

    private fun drawDeveloperResume(
        doc: PdfDocument,
        resume: ResumeEntity,
        pages: MutableList<PdfDocument.Page>
    ) {
        val renderer = PageRenderer(doc, pages)
        val darkBg = Color.parseColor("#0D1117")
        val codeGreen = Color.parseColor("#238636")
        val codePurple = Color.parseColor("#A371F7")
        val codeBlue = Color.parseColor("#79C0FF")
        val textLight = Color.parseColor("#C9D1D9")
        val textMuted = Color.parseColor("#8B949E")
        val sectionBg = Color.parseColor("#161B22")

        // Full dark header
        renderer.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, darkBg)

        renderer.drawText(
            resume.personalInfo.fullName.ifEmpty { "Developer" },
            MARGIN, 42f, paint(24f, Color.WHITE, Typeface.BOLD)
        )
        renderer.drawText(
            buildContactLine(resume.personalInfo),
            MARGIN, 64f, paint(8.5f, codeBlue)
        )
        renderer.drawText(
            buildContactLine2(resume.personalInfo),
            MARGIN, 78f, paint(8.5f, textMuted)
        )

        var y = 125f

        if (resume.aiContent.professionalSummary.isNotEmpty()) {
            renderer.drawRect(MARGIN, y - 5f, CONTENT_WIDTH, 40f, sectionBg, 3f)
            y = renderer.drawWrappedText(
                resume.aiContent.professionalSummary, y,
                paint(9.5f, textLight), MARGIN + 8f
            )
            y += 12f
        }

        fun devSection(title: String): Float {
            renderer.drawText("// $title", MARGIN, y, paint(10f, codePurple, Typeface.BOLD))
            return y + 16f
        }

        if (resume.skills.isNotEmpty()) {
            var sy = devSection("TECH STACK")
            val all = (resume.skills + resume.aiContent.suggestedSkills).distinct()
            sy = renderer.drawSkillTags(all, sy, codeGreen, sectionBg, Color.WHITE)
            y = sy + 8f
        }

        if (resume.experience.isNotEmpty()) {
            y = devSection("WORK EXPERIENCE")
            for (exp in resume.experience) {
                renderer.drawRect(MARGIN, y - 4f, CONTENT_WIDTH, 22f, sectionBg, 2f)
                renderer.drawText(exp.role, MARGIN + 8f, y + 5f, paint(10.5f, codeBlue, Typeface.BOLD))
                renderer.drawText(
                    exp.company, MARGIN + 8f, y + 18f,
                    paint(9f, codeGreen)
                )
                renderer.drawText(
                    exp.duration, PAGE_WIDTH - MARGIN - 100f, y + 5f,
                    paint(8.5f, textMuted)
                )
                y += 32f
                val bullets = exp.aiBulletPoints.takeIf { it.isNotEmpty() } ?: listOf(exp.description)
                for (b in bullets) {
                    y = renderer.drawWrappedText(b, y, paint(9f, textLight), MARGIN + 12f)
                }
                y += 8f
            }
        }

        if (resume.projects.isNotEmpty()) {
            y = devSection("PROJECTS")
            for (proj in resume.projects) {
                renderer.drawText(proj.title, MARGIN, y, paint(10.5f, codeBlue, Typeface.BOLD))
                if (proj.technologies.isNotEmpty()) {
                    renderer.drawText(
                        "[ ${proj.technologies} ]",
                        PAGE_WIDTH - MARGIN - 180f, y,
                        paint(8f, codeGreen)
                    )
                }
                y += 14f
                val desc = proj.aiDescription.ifEmpty { proj.description }
                y = renderer.drawWrappedText(desc, y, paint(9f, textLight))
                if (proj.githubUrl.isNotEmpty()) {
                    renderer.drawText("↗ ${proj.githubUrl}", MARGIN, y, paint(8f, codeBlue))
                    y += 12f
                }
                y += 6f
            }
        }

        if (resume.education.isNotEmpty()) {
            y = devSection("EDUCATION")
            for (edu in resume.education) {
                renderer.drawText(edu.degree, MARGIN, y, paint(10f, Color.WHITE, Typeface.BOLD))
                y += 14f
                renderer.drawText("${edu.college}  •  ${edu.year}", MARGIN, y, paint(9f, textMuted))
                y += 14f
            }
        }

        if (resume.certifications.isNotEmpty()) {
            y = devSection("CERTIFICATIONS")
            for (cert in resume.certifications) {
                renderer.drawText("✓  ${cert.name}  —  ${cert.organization}", MARGIN, y, paint(9.5f, codeGreen))
                y += 14f
            }
        }

        renderer.finalize()
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildContactLine(p: PersonalInfo): String =
        listOfNotNull(
            p.email.takeIf { it.isNotEmpty() },
            p.phone.takeIf { it.isNotEmpty() },
            p.location.takeIf { it.isNotEmpty() }
        ).joinToString("  |  ")

    private fun buildContactLine2(p: PersonalInfo): String =
        listOfNotNull(
            p.linkedin.takeIf { it.isNotEmpty() },
            p.portfolio.takeIf { it.isNotEmpty() }
        ).joinToString("  |  ")

    private fun paint(
        textSizeSp: Float,
        color: Int = Color.BLACK,
        typeface: Typeface = Typeface.DEFAULT
    ) = Paint().apply {
        this.textSize = textSizeSp * 1.33f // sp → px approx
        this.color = color
        this.typeface = typeface
        isAntiAlias = true
    }

    private fun createOutputFile(name: String): File {
        val safeFilename = name.replace(Regex("[^A-Za-z0-9]"), "_").take(30)
            .ifEmpty { "Resume" }
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AIResume")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${safeFilename}_Resume.pdf")
    }

    // ─── Inner class: PageRenderer ────────────────────────────────────────────

    inner class PageRenderer(
        private val doc: PdfDocument,
        private val pages: MutableList<PdfDocument.Page>
    ) {
        private var currentPage: PdfDocument.Page = newPage()
        private var canvas: Canvas = currentPage.canvas
        private var currentY: Float = 0f

        private fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pages.size + 1).create()
            val page = doc.startPage(pageInfo)
            pages.add(page)
            return page
        }

        fun checkNewPage(y: Float, needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) {
                doc.finishPage(currentPage)
                currentPage = newPage()
                canvas = currentPage.canvas
            }
        }

        fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Int, radius: Float = 0f) {
            val paint = Paint().apply { this.color = color; isAntiAlias = true }
            if (radius > 0) {
                canvas.drawRoundRect(RectF(x, y, x + width, y + height), radius, radius, paint)
            } else {
                canvas.drawRect(x, y, x + width, y + height, paint)
            }
        }

        fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, strokeWidth: Float = 1f) {
            val paint = Paint().apply {
                this.color = color
                this.strokeWidth = strokeWidth
                isAntiAlias = true
            }
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        fun drawText(text: String, x: Float, y: Float, paint: Paint) {
            canvas.drawText(text, x, y, paint)
        }

        fun drawWrappedText(
            text: String,
            startY: Float,
            paint: Paint,
            leftMargin: Float = MARGIN,
            maxWidth: Float = CONTENT_WIDTH - 10f
        ): Float {
            var y = startY
            val words = text.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                    checkNewPage(y, 16f)
                    canvas.drawText(currentLine, leftMargin, y, paint)
                    y += paint.textSize * 1.4f
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                checkNewPage(y, 16f)
                canvas.drawText(currentLine, leftMargin, y, paint)
                y += paint.textSize * 1.4f
            }
            return y
        }

        fun drawSection(
            title: String,
            y: Float,
            accentColor: Int,
            bgColor: Int,
            textColor: Int
        ): Float {
            checkNewPage(y, 25f)
            drawRect(MARGIN, y - 12f, CONTENT_WIDTH, 20f, bgColor)
            drawRect(MARGIN, y - 12f, 3f, 20f, accentColor)
            val paint = Paint().apply {
                textSize = 10.5f * 1.33f
                color = accentColor
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText(title, MARGIN + 8f, y, paint)
            return y + 14f
        }

        fun drawSkillTags(
            skills: List<String>,
            startY: Float,
            tagColor: Int,
            bgColor: Int,
            textColor: Int = Color.WHITE
        ): Float {
            var x = MARGIN
            var y = startY
            val tagPaint = Paint().apply {
                textSize = 8.5f * 1.33f
                color = textColor
                isAntiAlias = true
            }
            for (skill in skills.take(20)) {
                val width = tagPaint.measureText(skill) + 16f
                if (x + width > PAGE_WIDTH - MARGIN) {
                    x = MARGIN
                    y += 22f
                }
                drawRect(x, y - 12f, width, 16f, bgColor, 4f)
                canvas.drawText(skill, x + 8f, y, tagPaint)
                x += width + 6f
            }
            return y + 18f
        }

        fun finalize() {
            doc.finishPage(currentPage)
        }
    }
}
