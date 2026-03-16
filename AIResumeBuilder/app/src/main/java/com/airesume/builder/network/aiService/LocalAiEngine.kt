package com.airesume.builder.network.aiService

import com.airesume.builder.data.database.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Fully offline AI engine — no API key required.
 *
 * Uses curated phrase banks, smart keyword extraction, and
 * template-based NLG (Natural Language Generation) to produce
 * professional, ATS-optimised resume content entirely on-device.
 */
@Singleton
class LocalAiEngine @Inject constructor() {

    // ─── Public API ───────────────────────────────────────────────────────────

    suspend fun generateResumeContent(resume: ResumeEntity): Result<AiResumeResult> =
        runCatching {
            val roleKeywords  = extractRoleKeywords(resume)
            val domainProfile = detectDomain(resume)

            AiResumeResult(
                professionalSummary        = buildSummary(resume, domainProfile),
                experienceBulletPoints     = buildExperienceBullets(resume, domainProfile),
                enhancedProjectDescriptions = buildProjectDescriptions(resume, domainProfile),
                suggestedSkills            = suggestSkills(resume, domainProfile),
                atsKeywords                = buildAtsKeywords(resume, domainProfile)
            )
        }

    suspend fun improveResume(resume: ResumeEntity, feedback: String): Result<AiResumeResult> =
        runCatching {
            val domainProfile = detectDomain(resume, feedback)
            AiResumeResult(
                professionalSummary        = buildSummary(resume, domainProfile, feedback),
                experienceBulletPoints     = buildExperienceBullets(resume, domainProfile),
                enhancedProjectDescriptions = buildProjectDescriptions(resume, domainProfile),
                suggestedSkills            = suggestSkills(resume, domainProfile),
                atsKeywords                = buildAtsKeywords(resume, domainProfile)
            )
        }

    // ─── Domain Detection ─────────────────────────────────────────────────────

    private fun detectDomain(resume: ResumeEntity, extra: String = ""): Domain {
        val allText = buildString {
            append(resume.personalInfo.fullName)
            resume.skills.forEach { append(" $it") }
            resume.experience.forEach { append(" ${it.role} ${it.description}") }
            resume.education.forEach { append(" ${it.degree}") }
            if (extra.isNotEmpty()) append(" $extra")
        }.lowercase()

        return when {
            allText.containsAny("android","kotlin","ios","swift","mobile","flutter","react native") -> Domain.MOBILE_DEV
            allText.containsAny("machine learning","ml","ai","data science","tensorflow","pytorch","nlp","deep learning") -> Domain.DATA_SCIENCE
            allText.containsAny("devops","kubernetes","docker","aws","cloud","terraform","ci/cd","sre","infrastructure") -> Domain.DEVOPS
            allText.containsAny("frontend","react","vue","angular","css","html","ui","ux","design") -> Domain.FRONTEND
            allText.containsAny("backend","api","java","spring","node","python","django","flask","microservice") -> Domain.BACKEND
            allText.containsAny("fullstack","full stack","full-stack") -> Domain.FULLSTACK
            allText.containsAny("security","penetration","cybersecurity","ethical hacking","soc","siem") -> Domain.SECURITY
            allText.containsAny("manager","management","agile","scrum","product","stakeholder","roadmap") -> Domain.MANAGEMENT
            allText.containsAny("data analyst","sql","tableau","power bi","excel","visualization","reporting") -> Domain.DATA_ANALYST
            allText.containsAny("qa","testing","selenium","automation","quality","test plan") -> Domain.QA
            else -> Domain.GENERAL_SOFTWARE
        }
    }

    private fun extractRoleKeywords(resume: ResumeEntity): List<String> {
        return resume.experience.map { it.role.lowercase() }
            .flatMap { it.split(" ", ",", "/") }
            .filter { it.length > 3 }
            .distinct()
    }

    // ─── Summary Builder ──────────────────────────────────────────────────────

    private fun buildSummary(resume: ResumeEntity, domain: Domain, feedback: String = ""): String {
        val name        = resume.personalInfo.fullName.split(" ").firstOrNull() ?: "Professional"
        val edu         = resume.education.firstOrNull()
        val yearsExp    = estimateYearsExperience(resume)
        val topSkills   = resume.skills.take(3).joinToString(", ")
        val role        = resume.experience.firstOrNull()?.role ?: domain.defaultRole
        val adjective   = PROFESSIONAL_ADJECTIVES.random()
        val strength1   = domain.coreStrengths.random()
        val strength2   = domain.coreStrengths.filter { it != strength1 }.random()
        val impact      = IMPACT_PHRASES.random()

        val experiencePhrase = when {
            yearsExp >= 5  -> "$yearsExp+ years of experience"
            yearsExp >= 2  -> "over $yearsExp years of experience"
            yearsExp == 1  -> "1 year of hands-on experience"
            else           -> "a solid foundation"
        }

        val eduPhrase = edu?.let {
            if (it.degree.isNotEmpty() && it.college.isNotEmpty())
                "holding a ${it.degree} from ${it.college}"
            else if (it.degree.isNotEmpty()) "with a ${it.degree}"
            else ""
        } ?: ""

        val skillsPhrase = if (topSkills.isNotEmpty())
            "Proficient in $topSkills" else "Skilled across multiple technologies"

        val targetPhrase = if (feedback.containsAny("senior","lead","principal"))
            "Seeking a senior-level role to drive technical strategy and mentor teams."
        else if (feedback.containsAny("manager","management"))
            "Looking to leverage technical expertise in a leadership capacity."
        else
            "$impact and committed to delivering high-quality, scalable solutions."

        return "$adjective $role with $experiencePhrase${if (eduPhrase.isNotEmpty()) ", $eduPhrase" else ""}. " +
               "$skillsPhrase, with demonstrated expertise in $strength1 and $strength2. " +
               targetPhrase
    }

    private fun estimateYearsExperience(resume: ResumeEntity): Int {
        if (resume.experience.isEmpty()) return 0
        var total = 0
        for (exp in resume.experience) {
            val dur = exp.duration.lowercase()
            // Try to parse "2022 - 2024" or "2 years" patterns
            val yearPattern = Regex("(\\d{4})\\s*[-–—]\\s*(\\d{4}|present|current)")
            val match = yearPattern.find(dur)
            if (match != null) {
                val start = match.groupValues[1].toIntOrNull() ?: continue
                val end   = if (match.groupValues[2].containsAny("present","current"))
                    2025 else match.groupValues[2].toIntOrNull() ?: 2025
                total += (end - start).coerceAtLeast(0)
            } else {
                val numMatch = Regex("(\\d+)\\s*yr|year").find(dur)
                total += numMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
            }
        }
        return total.coerceAtMost(30)
    }

    // ─── Experience Bullets ───────────────────────────────────────────────────

    private fun buildExperienceBullets(
        resume: ResumeEntity,
        domain: Domain
    ): Map<String, List<String>> {
        return resume.experience.associate { exp ->
            exp.company to generateBulletsForExperience(exp, domain, resume.skills)
        }
    }

    private fun generateBulletsForExperience(
        exp: Experience,
        domain: Domain,
        skills: List<String>
    ): List<String> {
        val desc       = exp.description.lowercase()
        val role       = exp.role.lowercase()
        val skillsUsed = skills.take(4).joinToString(", ").ifEmpty { domain.defaultTechStack }
        val bullets    = mutableListOf<String>()

        // Bullet 1: Primary tech/development bullet
        val primaryVerb = domain.actionVerbs.random()
        val primaryAchievement = buildPrimaryBullet(exp, domain, skillsUsed, primaryVerb)
        bullets.add("• $primaryAchievement")

        // Bullet 2: Performance / impact bullet
        val metric = PERFORMANCE_METRICS.random()
        val perfVerb = listOf("Optimized", "Enhanced", "Improved", "Streamlined", "Boosted").random()
        val perfArea = domain.performanceAreas.random()
        bullets.add("• $perfVerb $perfArea, resulting in $metric")

        // Bullet 3: Collaboration / process bullet
        val collabVerb = listOf("Collaborated", "Partnered", "Worked closely", "Coordinated", "Engaged").random()
        val team = listOf(
            "cross-functional teams", "product managers and designers",
            "a team of ${(3..8).random()} engineers", "stakeholders and business analysts",
            "QA engineers and DevOps teams"
        ).random()
        val collabOutcome = COLLABORATION_OUTCOMES.random()
        bullets.add("• $collabVerb with $team to $collabOutcome")

        // Bullet 4: Leadership or ownership bullet (if senior role detected)
        if (role.containsAny("senior","lead","principal","manager","head","architect")) {
            val leaderVerb = listOf("Led","Mentored","Guided","Spearheaded","Championed","Architected").random()
            val leaderAction = domain.leadershipActions.random()
            bullets.add("• $leaderVerb $leaderAction")
        } else {
            // Growth / learning bullet for junior/mid
            val growthVerb = listOf("Contributed","Implemented","Developed","Delivered","Built").random()
            val growthArea = domain.growthAreas.random()
            bullets.add("• $growthVerb $growthArea, demonstrating ${SOFT_SKILLS.random()}")
        }

        return bullets
    }

    private fun buildPrimaryBullet(exp: Experience, domain: Domain, skills: String, verb: String): String {
        val desc = exp.description.trim()
        return if (desc.length > 20) {
            // Enhance the user's own description
            "$verb ${enhanceDescription(desc, domain, skills)}"
        } else {
            // Fully synthesize
            val action = domain.primaryActions.random()
            "$verb $action using $skills, ${IMPACT_PHRASES.random()}"
        }
    }

    private fun enhanceDescription(raw: String, domain: Domain, skills: String): String {
        // Remove filler words, capitalise first letter, add metric if missing
        val cleaned = raw
            .replace(Regex("^(i |we |the team |our team )", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(was responsible for|was in charge of|helped to|assisted with)\\b", RegexOption.IGNORE_CASE), "")
            .trim()
            .replaceFirstChar { it.uppercase() }

        // Add a quantified metric if none present
        val hasMetric = cleaned.contains(Regex("\\d+%|\\d+x|\\d+ (users|requests|hours|days|ms|seconds)"))
        return if (!hasMetric) {
            "$cleaned, ${IMPACT_PHRASES.random()}"
        } else {
            cleaned
        }
    }

    // ─── Project Descriptions ─────────────────────────────────────────────────

    private fun buildProjectDescriptions(
        resume: ResumeEntity,
        domain: Domain
    ): Map<String, String> {
        return resume.projects.associate { proj ->
            proj.title to enhanceProject(proj, domain)
        }
    }

    private fun enhanceProject(proj: Project, domain: Domain): String {
        val tech = proj.technologies.ifEmpty { domain.defaultTechStack }
        val desc = proj.description.trim()

        return if (desc.length > 20) {
            val cleaned = desc
                .replace(Regex("^(this|i |we )", RegexOption.IGNORE_CASE), "")
                .trim()
                .replaceFirstChar { it.uppercase() }
            val hasMetric = cleaned.contains(Regex("\\d+|users|requests|features"))
            val suffix = if (!hasMetric) ", serving ${(100..10000).random()} users" else ""
            "Designed and developed ${cleaned}$suffix. Built with $tech, featuring ${domain.projectFeatures.random()}."
        } else {
            val action   = listOf("Designed and built","Engineered","Developed","Created","Architected").random()
            val feature1 = domain.projectFeatures.random()
            val feature2 = domain.projectFeatures.filter { it != feature1 }.random()
            "$action a ${proj.title.ifEmpty{"project"}} using $tech. " +
            "Implemented $feature1 and $feature2. " +
            "${IMPACT_PHRASES.random().replaceFirstChar { it.uppercase() }}."
        }
    }

    // ─── Skill Suggestions ────────────────────────────────────────────────────

    private fun suggestSkills(resume: ResumeEntity, domain: Domain): List<String> {
        val existing = resume.skills.map { it.lowercase() }.toSet()
        return domain.skillSuggestions
            .filter { it.lowercase() !in existing }
            .shuffled()
            .take(8)
    }

    // ─── ATS Keywords ─────────────────────────────────────────────────────────

    private fun buildAtsKeywords(resume: ResumeEntity, domain: Domain): List<String> {
        val base = domain.atsKeywords.shuffled().take(10)
        val fromSkills = resume.skills.take(5)
        return (base + fromSkills).distinct().take(15)
    }

    // ─── Domain Profiles ─────────────────────────────────────────────────────

    private enum class Domain(
        val defaultRole: String,
        val defaultTechStack: String,
        val coreStrengths: List<String>,
        val actionVerbs: List<String>,
        val performanceAreas: List<String>,
        val leadershipActions: List<String>,
        val growthAreas: List<String>,
        val primaryActions: List<String>,
        val projectFeatures: List<String>,
        val skillSuggestions: List<String>,
        val atsKeywords: List<String>
    ) {
        MOBILE_DEV(
            defaultRole = "Mobile Developer",
            defaultTechStack = "Kotlin, Jetpack Compose, Android SDK",
            coreStrengths = listOf(
                "Android application development","mobile UI/UX implementation",
                "performance optimisation","offline-first architecture","REST API integration"
            ),
            actionVerbs = listOf("Engineered","Architected","Developed","Built","Implemented","Shipped"),
            performanceAreas = listOf(
                "app startup time by 35%","memory usage by 40%","battery consumption by 25%",
                "crash-free rate to 99.8%","frame render time to under 16ms"
            ),
            leadershipActions = listOf(
                "a team of mobile engineers through a complete app rewrite",
                "the adoption of MVVM architecture across the mobile team",
                "code review processes that reduced bug rate by 30%",
                "mobile CI/CD pipeline reducing release cycle from 2 weeks to 3 days"
            ),
            growthAreas = listOf(
                "offline-first sync with Room and WorkManager",
                "custom Compose animations and transitions",
                "push notification infrastructure using FCM",
                "deep-link navigation and dynamic feature modules"
            ),
            primaryActions = listOf(
                "native Android applications with Jetpack Compose",
                "RESTful API integrations with Retrofit and Coroutines",
                "offline caching layer with Room Database",
                "custom UI components with Material Design 3"
            ),
            projectFeatures = listOf(
                "real-time data sync","offline caching","biometric authentication",
                "push notifications","deep link navigation","dark mode support",
                "adaptive layouts for tablets","in-app purchases"
            ),
            skillSuggestions = listOf(
                "Jetpack Compose","Kotlin Coroutines","Hilt","Room","Retrofit",
                "MVVM","LiveData","StateFlow","WorkManager","Firebase",
                "Material Design","Navigation Component","ProGuard","Gradle"
            ),
            atsKeywords = listOf(
                "Android","Kotlin","Jetpack Compose","Mobile Development","MVVM",
                "REST API","Room Database","Coroutines","Material Design","Play Store",
                "APK","AAB","SDK","Gradle","Unit Testing"
            )
        ),

        BACKEND(
            defaultRole = "Backend Developer",
            defaultTechStack = "Java/Kotlin, Spring Boot, PostgreSQL",
            coreStrengths = listOf(
                "scalable API design","database optimisation",
                "microservices architecture","system reliability","security best practices"
            ),
            actionVerbs = listOf("Architected","Engineered","Designed","Developed","Deployed","Optimised"),
            performanceAreas = listOf(
                "API response time by 60%","database query performance by 45%",
                "system throughput to 10,000 req/s","service availability to 99.95%",
                "infrastructure costs by 30%"
            ),
            leadershipActions = listOf(
                "the design and implementation of a microservices migration",
                "backend architecture decisions for a platform serving 1M+ users",
                "a team of 4 backend engineers to deliver critical features on schedule",
                "adoption of event-driven architecture using Apache Kafka"
            ),
            growthAreas = listOf(
                "RESTful API endpoints serving 500K+ daily requests",
                "database schema design and optimisation strategies",
                "JWT-based authentication and authorisation systems",
                "containerised deployment with Docker and Kubernetes"
            ),
            primaryActions = listOf(
                "RESTful APIs consumed by web and mobile clients",
                "microservices with Spring Boot and Docker",
                "relational database schemas and optimised queries",
                "background job processing systems"
            ),
            projectFeatures = listOf(
                "JWT authentication","rate limiting","caching with Redis",
                "async task queues","webhook handling","API versioning",
                "database migrations","circuit breakers","OpenAPI documentation"
            ),
            skillSuggestions = listOf(
                "Spring Boot","PostgreSQL","Redis","Docker","Kubernetes",
                "Apache Kafka","REST APIs","Microservices","JUnit","Maven",
                "CI/CD","OAuth2","gRPC","Elasticsearch","RabbitMQ"
            ),
            atsKeywords = listOf(
                "Backend Development","REST API","Microservices","Spring Boot",
                "SQL","NoSQL","Docker","Kubernetes","CI/CD","System Design",
                "Scalability","Database Optimisation","Java","Python","Node.js"
            )
        ),

        FRONTEND(
            defaultRole = "Frontend Developer",
            defaultTechStack = "React, TypeScript, CSS",
            coreStrengths = listOf(
                "responsive UI development","state management","web performance optimisation",
                "accessibility (a11y)","cross-browser compatibility"
            ),
            actionVerbs = listOf("Designed","Built","Implemented","Crafted","Engineered","Developed"),
            performanceAreas = listOf(
                "page load speed by 50% via code splitting and lazy loading",
                "Lighthouse score from 65 to 97","bundle size by 40% with tree shaking",
                "Core Web Vitals to green status","Time-to-Interactive by 2.3 seconds"
            ),
            leadershipActions = listOf(
                "the frontend architecture migration from Class to Functional components",
                "a design system adopted by 4 product teams",
                "frontend performance culture with weekly Lighthouse review sessions",
                "onboarding of junior frontend developers"
            ),
            growthAreas = listOf(
                "reusable component libraries with Storybook documentation",
                "client-side state management with Redux Toolkit",
                "server-side rendering with Next.js",
                "unit and integration tests with Jest and Testing Library"
            ),
            primaryActions = listOf(
                "responsive web interfaces with React and TypeScript",
                "pixel-perfect UI components from Figma designs",
                "client-side routing and state management",
                "accessible components following WCAG 2.1 guidelines"
            ),
            projectFeatures = listOf(
                "responsive design","dark mode","skeleton loaders",
                "infinite scroll","real-time updates via WebSocket",
                "progressive web app (PWA)","i18n/l10n support","a11y compliance"
            ),
            skillSuggestions = listOf(
                "React","TypeScript","Next.js","Tailwind CSS","Redux Toolkit",
                "Jest","Storybook","Webpack","Vite","GraphQL",
                "CSS Modules","Figma","Web Accessibility","Performance Optimisation"
            ),
            atsKeywords = listOf(
                "Frontend Development","React","TypeScript","JavaScript","CSS",
                "HTML","Responsive Design","State Management","Web Performance",
                "Component Library","UI/UX","Cross-browser Compatibility","REST APIs"
            )
        ),

        FULLSTACK(
            defaultRole = "Full Stack Developer",
            defaultTechStack = "React, Node.js, PostgreSQL",
            coreStrengths = listOf(
                "end-to-end feature delivery","full stack architecture",
                "API design and integration","database modelling","deployment automation"
            ),
            actionVerbs = listOf("Built","Engineered","Delivered","Developed","Architected","Shipped"),
            performanceAreas = listOf(
                "end-to-end feature delivery time by 40%",
                "API latency by 55% with optimised queries and caching",
                "user engagement by 25% through UX improvements",
                "deployment frequency from monthly to daily"
            ),
            leadershipActions = listOf(
                "full-stack architecture for a SaaS platform with 50,000 users",
                "the selection and adoption of the team's frontend framework",
                "end-to-end delivery of 3 major product features per quarter",
                "technical debt reduction initiative saving 20% development time"
            ),
            growthAreas = listOf(
                "complete CRUD applications with React and Node.js",
                "user authentication flows with JWT and OAuth",
                "database-backed REST APIs with automated testing",
                "deployment pipelines with GitHub Actions"
            ),
            primaryActions = listOf(
                "full-stack web applications from concept to production",
                "RESTful APIs consumed by React frontends",
                "database schemas and ORM integrations",
                "end-to-end features across the entire stack"
            ),
            projectFeatures = listOf(
                "user authentication","real-time notifications","file uploads",
                "payment integration","email automation","admin dashboards",
                "role-based access control","audit logging"
            ),
            skillSuggestions = listOf(
                "React","Node.js","TypeScript","PostgreSQL","MongoDB","Redis",
                "Docker","REST APIs","GraphQL","Next.js","Prisma","Jest",
                "GitHub Actions","AWS","Vercel"
            ),
            atsKeywords = listOf(
                "Full Stack Development","React","Node.js","REST API","SQL",
                "JavaScript","TypeScript","Git","Agile","CI/CD",
                "Database Design","Cloud Deployment","End-to-End","SaaS"
            )
        ),

        DATA_SCIENCE(
            defaultRole = "Data Scientist",
            defaultTechStack = "Python, TensorFlow, Pandas, SQL",
            coreStrengths = listOf(
                "machine learning model development","statistical analysis",
                "data pipeline engineering","A/B testing","predictive modelling"
            ),
            actionVerbs = listOf("Developed","Trained","Deployed","Engineered","Analysed","Built"),
            performanceAreas = listOf(
                "model accuracy from 78% to 94% using ensemble methods",
                "prediction latency by 70% through model quantisation",
                "data pipeline efficiency by 3x with vectorised operations",
                "false positive rate by 40% through feature engineering"
            ),
            leadershipActions = listOf(
                "an end-to-end MLOps platform serving 15 production models",
                "a data science team of 5 in building a recommendation engine",
                "the transition from batch to real-time inference architecture",
                "company-wide ML model governance framework"
            ),
            growthAreas = listOf(
                "classification models with scikit-learn achieving 92% accuracy",
                "ETL pipelines processing 5GB+ of data daily",
                "exploratory data analysis and visualisation dashboards",
                "feature engineering improving model performance by 18%"
            ),
            primaryActions = listOf(
                "predictive ML models deployed to production",
                "data pipelines ingesting and transforming large datasets",
                "statistical analyses driving product decisions",
                "A/B test frameworks measuring feature impact"
            ),
            projectFeatures = listOf(
                "automated feature engineering","model monitoring","data versioning",
                "experiment tracking","real-time inference","batch prediction",
                "interactive dashboards","anomaly detection"
            ),
            skillSuggestions = listOf(
                "Python","TensorFlow","PyTorch","Pandas","NumPy","scikit-learn",
                "SQL","Spark","MLflow","Jupyter","Matplotlib","Seaborn","AWS SageMaker","Airflow"
            ),
            atsKeywords = listOf(
                "Machine Learning","Data Science","Python","Deep Learning","NLP",
                "Statistical Analysis","A/B Testing","Feature Engineering",
                "Model Deployment","MLOps","Big Data","Predictive Modelling","SQL"
            )
        ),

        DEVOPS(
            defaultRole = "DevOps Engineer",
            defaultTechStack = "Kubernetes, Terraform, AWS, Docker",
            coreStrengths = listOf(
                "CI/CD pipeline automation","cloud infrastructure management",
                "system reliability engineering","infrastructure as code","container orchestration"
            ),
            actionVerbs = listOf("Automated","Architected","Designed","Implemented","Deployed","Orchestrated"),
            performanceAreas = listOf(
                "deployment frequency from weekly to multiple times daily",
                "MTTR (mean time to recovery) from 4 hours to 15 minutes",
                "infrastructure costs by 35% through right-sizing and spot instances",
                "system uptime to 99.99% SLA"
            ),
            leadershipActions = listOf(
                "the migration of 50+ microservices to Kubernetes",
                "a platform engineering team delivering internal developer tools",
                "GitOps adoption across 8 engineering teams",
                "disaster recovery strategy with RPO < 1 hour and RTO < 30 minutes"
            ),
            growthAreas = listOf(
                "CI/CD pipelines reducing release time from days to minutes",
                "Terraform modules managing 200+ cloud resources",
                "monitoring stack with Prometheus, Grafana, and alerting",
                "container security scanning in the build pipeline"
            ),
            primaryActions = listOf(
                "CI/CD pipelines automating build, test, and deploy workflows",
                "cloud infrastructure using Terraform and Ansible",
                "Kubernetes clusters managing 100+ microservices",
                "monitoring and alerting systems for production workloads"
            ),
            projectFeatures = listOf(
                "zero-downtime deployments","auto-scaling policies",
                "log aggregation","distributed tracing","secret management",
                "infrastructure drift detection","blue/green deployments","chaos engineering"
            ),
            skillSuggestions = listOf(
                "Kubernetes","Docker","Terraform","AWS","Azure","GCP",
                "Jenkins","GitHub Actions","ArgoCD","Prometheus","Grafana",
                "Ansible","Helm","Linux","Bash Scripting"
            ),
            atsKeywords = listOf(
                "DevOps","CI/CD","Kubernetes","Docker","Infrastructure as Code",
                "Terraform","AWS","Cloud","Automation","SRE","Monitoring",
                "Linux","GitOps","Pipeline","Reliability"
            )
        ),

        SECURITY(
            defaultRole = "Security Engineer",
            defaultTechStack = "SIEM tools, Python, Kali Linux",
            coreStrengths = listOf(
                "vulnerability assessment","incident response",
                "penetration testing","security architecture","compliance frameworks"
            ),
            actionVerbs = listOf("Identified","Remediated","Implemented","Hardened","Assessed","Investigated"),
            performanceAreas = listOf(
                "vulnerability detection rate by 60% with automated scanning",
                "incident response time from 4 hours to 45 minutes",
                "attack surface by 70% through network segmentation",
                "security awareness training completion to 98%"
            ),
            leadershipActions = listOf(
                "company-wide SOC 2 Type II certification process",
                "a red team exercise identifying 12 critical vulnerabilities",
                "zero-trust network architecture implementation",
                "security champion programme across 6 engineering teams"
            ),
            growthAreas = listOf(
                "penetration testing of web applications and APIs",
                "SIEM rule development reducing false positives by 40%",
                "security automation scripts for log analysis",
                "threat modelling for new product features"
            ),
            primaryActions = listOf(
                "security controls across cloud and on-premise infrastructure",
                "vulnerability management programme and remediation tracking",
                "incident detection and response playbooks",
                "security code reviews for critical application changes"
            ),
            projectFeatures = listOf(
                "threat detection","automated alerting","RBAC implementation",
                "encryption at rest and in transit","compliance reporting",
                "security dashboards","pen test findings tracking"
            ),
            skillSuggestions = listOf(
                "Penetration Testing","SIEM","Splunk","Nessus","Burp Suite",
                "Python","Incident Response","Threat Modelling","OWASP","ISO 27001",
                "SOC 2","Zero Trust","Network Security","Cloud Security"
            ),
            atsKeywords = listOf(
                "Cybersecurity","Penetration Testing","Vulnerability Assessment",
                "Incident Response","SIEM","Compliance","Risk Management",
                "Security Architecture","OWASP","Cloud Security","SOC","CISSP"
            )
        ),

        MANAGEMENT(
            defaultRole = "Engineering Manager",
            defaultTechStack = "Agile, Jira, Confluence",
            coreStrengths = listOf(
                "team leadership and development","agile delivery",
                "cross-functional collaboration","roadmap planning","stakeholder management"
            ),
            actionVerbs = listOf("Led","Managed","Drove","Championed","Scaled","Delivered"),
            performanceAreas = listOf(
                "team velocity by 40% through process improvements",
                "employee satisfaction scores by 25% year-on-year",
                "time-to-hire from 8 weeks to 3 weeks",
                "on-time delivery rate to 95% across 12 sprints"
            ),
            leadershipActions = listOf(
                "a team of 8 engineers delivering a platform used by 500K users",
                "engineering hiring process resulting in 6 successful hires in 3 months",
                "quarterly OKR planning aligning engineering and business goals",
                "adoption of Shape Up methodology improving predictability by 50%"
            ),
            growthAreas = listOf(
                "sprint planning and retrospectives improving team processes",
                "1:1 coaching and career development plans for direct reports",
                "technical specifications for cross-team initiatives",
                "stakeholder communication and expectation management"
            ),
            primaryActions = listOf(
                "cross-functional engineering teams of 5–10 engineers",
                "agile delivery frameworks improving team output",
                "technical hiring and onboarding processes",
                "product roadmaps aligned with business objectives"
            ),
            projectFeatures = listOf(
                "OKR frameworks","sprint planning","technical roadmaps",
                "hiring pipelines","performance reviews","career ladders",
                "incident post-mortems","team health metrics"
            ),
            skillSuggestions = listOf(
                "Agile","Scrum","Kanban","Jira","Confluence","OKRs",
                "Technical Leadership","Hiring","Mentoring","Stakeholder Management",
                "Roadmapping","Risk Management","Engineering Metrics"
            ),
            atsKeywords = listOf(
                "Engineering Management","Agile","Team Leadership","Scrum",
                "Product Delivery","Stakeholder Management","OKRs","Hiring",
                "Technical Strategy","Cross-functional","Mentoring","Roadmap"
            )
        ),

        DATA_ANALYST(
            defaultRole = "Data Analyst",
            defaultTechStack = "SQL, Python, Tableau, Excel",
            coreStrengths = listOf(
                "data analysis and visualisation","SQL query optimisation",
                "business intelligence reporting","stakeholder communication","statistical modelling"
            ),
            actionVerbs = listOf("Analysed","Developed","Created","Designed","Built","Generated"),
            performanceAreas = listOf(
                "reporting time from 3 days to 2 hours with automated dashboards",
                "data accuracy by 30% through improved validation processes",
                "decision-making speed by providing real-time KPI tracking",
                "data pipeline efficiency by 50% with optimised SQL queries"
            ),
            leadershipActions = listOf(
                "a self-service analytics platform used by 200+ stakeholders",
                "data quality framework reducing reporting errors by 75%",
                "weekly business review cadence with C-suite dashboards",
                "training programme upskilling 15 non-technical team members in data tools"
            ),
            growthAreas = listOf(
                "SQL dashboards tracking KPIs for executive review",
                "cohort analysis identifying retention trends",
                "automated reporting reducing manual effort by 8 hours/week",
                "data cleaning pipelines ensuring 99% data accuracy"
            ),
            primaryActions = listOf(
                "business performance dashboards and reports",
                "ad-hoc analyses informing product and marketing decisions",
                "data models in SQL supporting business intelligence tools",
                "A/B test analyses measuring experiment impact"
            ),
            projectFeatures = listOf(
                "interactive dashboards","automated alerts","cohort analysis",
                "funnel visualisations","trend analysis","anomaly detection",
                "self-service reporting","data dictionaries"
            ),
            skillSuggestions = listOf(
                "SQL","Python","Tableau","Power BI","Excel","Google Analytics",
                "Looker","dbt","BigQuery","Pandas","Statistics","A/B Testing",
                "Data Visualisation","Business Intelligence"
            ),
            atsKeywords = listOf(
                "Data Analysis","SQL","Business Intelligence","Data Visualisation",
                "Tableau","Python","Reporting","KPI","Analytics","Excel",
                "Stakeholder Communication","A/B Testing","Data-driven"
            )
        ),

        QA(
            defaultRole = "QA Engineer",
            defaultTechStack = "Selenium, Appium, JUnit, Python",
            coreStrengths = listOf(
                "test automation","manual testing","bug reporting",
                "performance testing","quality advocacy"
            ),
            actionVerbs = listOf("Designed","Automated","Implemented","Executed","Developed","Maintained"),
            performanceAreas = listOf(
                "test coverage from 45% to 85% with automated regression suite",
                "release cycle from 3 weeks to 1 week through automation",
                "critical bug escape rate by 90%",
                "manual testing effort by 60% via automation"
            ),
            leadershipActions = listOf(
                "QA automation strategy across 3 product squads",
                "shift-left testing culture reducing post-release defects by 40%",
                "test framework adopted by the entire engineering organisation",
                "performance testing programme identifying bottlenecks pre-launch"
            ),
            growthAreas = listOf(
                "automated regression suite covering 500+ test cases",
                "API testing framework with Postman and REST-Assured",
                "performance test scripts using JMeter",
                "bug reports with detailed reproduction steps and logs"
            ),
            primaryActions = listOf(
                "automated test suites for web and mobile applications",
                "test plans and test cases for new features",
                "regression testing frameworks integrated into CI/CD",
                "performance and load tests for critical user flows"
            ),
            projectFeatures = listOf(
                "automated regression tests","API test suites","mobile test automation",
                "performance benchmarks","test data management","CI/CD integration",
                "test reporting dashboards","exploratory testing charters"
            ),
            skillSuggestions = listOf(
                "Selenium","Appium","JUnit","TestNG","Cypress","Playwright",
                "Postman","JMeter","Python","Java","BDD","Cucumber",
                "Test Planning","CI/CD","JIRA"
            ),
            atsKeywords = listOf(
                "QA Engineering","Test Automation","Selenium","Manual Testing",
                "Regression Testing","API Testing","Performance Testing",
                "CI/CD","SDLC","Bug Reporting","Test Planning","Quality Assurance"
            )
        ),

        GENERAL_SOFTWARE(
            defaultRole = "Software Engineer",
            defaultTechStack = "Python, JavaScript, SQL, Git",
            coreStrengths = listOf(
                "software development and delivery","problem-solving",
                "code quality and testing","team collaboration","continuous learning"
            ),
            actionVerbs = listOf("Developed","Built","Implemented","Engineered","Delivered","Created"),
            performanceAreas = listOf(
                "code review turnaround time by 50%",
                "test coverage to 85% with unit and integration tests",
                "build time by 30% through dependency optimisation",
                "bug resolution time by 40%"
            ),
            leadershipActions = listOf(
                "adoption of new development standards improving code quality",
                "onboarding documentation reducing ramp-up time for new joiners",
                "technical investigations and proof-of-concept implementations",
                "cross-team initiatives delivering shared platform improvements"
            ),
            growthAreas = listOf(
                "features end-to-end from design to deployment",
                "unit and integration tests ensuring software reliability",
                "technical documentation for complex systems",
                "code refactoring improving maintainability"
            ),
            primaryActions = listOf(
                "software features following best practices and design patterns",
                "clean, maintainable code with comprehensive test coverage",
                "technical solutions to complex business problems",
                "integrations with third-party APIs and services"
            ),
            projectFeatures = listOf(
                "user authentication","data persistence","REST API integration",
                "automated tests","error handling and logging",
                "configuration management","deployment automation"
            ),
            skillSuggestions = listOf(
                "Git","Docker","SQL","REST APIs","Unit Testing","CI/CD",
                "Linux","Bash","Agile","JIRA","Code Review","Design Patterns",
                "Data Structures","Algorithms"
            ),
            atsKeywords = listOf(
                "Software Engineering","Object-Oriented Programming","Agile",
                "Git","REST API","Testing","CI/CD","Problem Solving",
                "Code Review","Documentation","SDLC","Collaboration"
            )
        )
    }

    // ─── Phrase Banks ─────────────────────────────────────────────────────────

    private val PROFESSIONAL_ADJECTIVES = listOf(
        "Results-driven","Highly motivated","Detail-oriented","Innovative",
        "Passionate","Dedicated","Experienced","Skilled","Versatile","Dynamic"
    )

    private val IMPACT_PHRASES = listOf(
        "improving overall system reliability","reducing technical debt significantly",
        "increasing team productivity","driving measurable business value",
        "enhancing user experience","contributing to a 20% increase in performance",
        "supporting the growth of the platform","enabling faster feature delivery",
        "reducing operational overhead","improving code maintainability"
    )

    private val PERFORMANCE_METRICS = listOf(
        "a 35% improvement in performance",
        "a 40% reduction in processing time",
        "improved scalability supporting 2x more users",
        "a 25% decrease in error rate",
        "saving the team 5+ hours per week",
        "a 50% reduction in manual effort",
        "improved system reliability to 99.9% uptime",
        "a 30% increase in throughput"
    )

    private val COLLABORATION_OUTCOMES = listOf(
        "deliver product features on time and within scope",
        "resolve critical production incidents within SLA",
        "define technical requirements and architecture decisions",
        "improve cross-team communication and alignment",
        "ship high-quality releases on a weekly cadence",
        "reduce dependencies and unblock parallel workstreams",
        "ensure alignment between technical and business objectives"
    )

    private val SOFT_SKILLS = listOf(
        "strong attention to detail","effective communication skills",
        "a growth mindset","ownership and accountability",
        "adaptability in a fast-paced environment","strong analytical thinking"
    )

    // ─── Helper Extensions ────────────────────────────────────────────────────

    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it, ignoreCase = true) }
}
