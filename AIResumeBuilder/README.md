# 🤖 AI Resume Builder — Android App (100% Offline)

A production-ready Android application with a **fully built-in AI engine** that generates
professional, ATS-optimised resumes — **no API key, no internet, no subscription required**.

Built with **Kotlin · Jetpack Compose · MVVM · Room · Hilt**.

---

## ✨ Key Highlights

| | |
|--|--|
| 🔌 **Zero Setup** | No API key. No account. Just clone and run. |
| 📴 **Fully Offline** | AI runs entirely on-device. Internet not required. |
| 🧠 **Smart Domain Detection** | Auto-detects your career field from skills & experience |
| 📄 **3 PDF Templates** | Modern Professional · Minimal Clean · Developer Resume |
| 💾 **Local Storage** | All resumes saved to Room (SQLite) on your phone |
| 📤 **PDF Export & Share** | Export via Android PdfDocument, share to any app |
| ✅ **ATS Optimised** | Action verbs, quantified metrics, keyword injection |

---

## 🧠 How the Offline AI Works

The app includes `LocalAiEngine.kt` — a **Natural Language Generation engine** that:

1. **Detects your career domain** from your skills and job titles:
   - Android/Mobile · Backend · Frontend · Full Stack
   - Data Science/ML · DevOps/Cloud · Security
   - Data Analyst · QA · Engineering Manager · General Software

2. **Generates a professional summary** tailored to your domain, experience level, and education

3. **Writes ATS bullet points** for each job using:
   - Domain-specific action verbs (*Engineered, Architected, Spearheaded...*)
   - Quantified impact metrics (*"reduced load time by 35%"*)
   - Collaboration and leadership bullets

4. **Enhances project descriptions** with technical depth and impact

5. **Suggests missing skills** from a curated domain skill bank

6. **Produces 15 ATS keywords** to pass resume-scanning software

> All of this runs in milliseconds, entirely on your device.

---

## 🚀 Getting Started (3 Steps)

### 1. Open in Android Studio

```
File → Open → select the AIResumeBuilder folder → OK
```

Requires: **Android Studio Hedgehog 2023.1.1+** and **JDK 17** (bundled with AS)

### 2. Sync Gradle

Click **"Sync Now"** in the yellow bar that appears, or:
```
File → Sync Project with Gradle Files
```

### 3. Run

Select an emulator or physical device (Android 8.0+) and press **▶️ Run** (`Shift+F10`).

**That's it. No API key. No configuration. Just run.**

---

## 📱 App Flow

```
Dashboard
  ├── Create Resume  →  7-step form  →  Generate AI Content  →  Preview  →  Export PDF
  ├── My Resumes     →  Edit / View / Delete / AI Improve
  ├── Templates      →  Browse 3 templates
  └── Settings       →  About & AI engine info
```

---

## 📂 Project Structure

```
app/src/main/java/com/airesume/builder/
│
├── AIResumeApp.kt
├── MainActivity.kt
│
├── data/
│   ├── database/
│   │   ├── ResumeModels.kt     # Domain models + Room entity + TypeConverters
│   │   ├── ResumeDao.kt        # CRUD operations
│   │   └── ResumeDatabase.kt
│   └── repository/
│       └── ResumeRepository.kt
│
├── network/aiService/
│   ├── LocalAiEngine.kt        # ★ The offline AI — 600+ lines of NLG
│   ├── AiResumeService.kt      # Thin wrapper delegating to LocalAiEngine
│   └── AiModels.kt             # AiResumeResult data class
│
├── di/
│   └── AppModule.kt            # Hilt: Room + PDF utils (no network)
│
├── viewmodel/
│   └── ResumeViewModel.kt      # MVVM state — form, AI, PDF, CRUD
│
├── ui/
│   ├── navigation/Navigation.kt
│   ├── theme/
│   ├── components/Components.kt
│   └── screens/
│       ├── dashboard/
│       ├── form/               # 7-step resume form
│       ├── preview/            # Compose resume preview
│       ├── myresumes/
│       ├── templates/
│       ├── improve/            # Re-run AI with feedback
│       └── settings/
│
└── utils/pdfGenerator/
    ├── ResumePdfGenerator.kt   # PdfDocument → 3 templates
    └── PdfShareUtil.kt         # FileProvider share intent
```

---

## 📦 Build APK

```bash
# Debug APK (for testing)
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release APK (requires keystore)
./gradlew assembleRelease
```

### Create a Keystore (one-time, for release)

```bash
keytool -genkey -v -keystore ai_resume.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias ai_resume_key
```

Add to `app/build.gradle.kts` under `android { signingConfigs { ... } }`.

---

## 📱 Generate AAB for Google Play

```bash
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

Or: **Build → Generate Signed Bundle / APK → Android App Bundle**

---

## 🏪 Publish to Google Play

1. Create account at https://play.google.com/console ($25 one-time fee)
2. Create new app → fill store listing (title, description, screenshots)
3. Upload your `app-release.aab`
4. Complete content rating questionnaire
5. Set pricing (free recommended) and countries
6. Submit for review (~1–3 days)

**Suggested store listing:**
- **Title**: AI Resume Builder - No API Key
- **Short desc**: Create professional ATS-optimised resumes offline. No API key needed!
- **Category**: Productivity

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| DI | Hilt 2.51 |
| Database | Room (SQLite) |
| AI | Local NLG Engine (no API) |
| PDF | Android PdfDocument API |
| Navigation | Navigation Compose |
| Min SDK | API 26 (Android 8.0) |
| Internet | ❌ Not required |

---

## 🔧 Troubleshooting

**Build fails with KSP error**
→ `Build → Clean Project` then `Build → Rebuild Project`

**Hilt error: "cannot find symbol"**
→ Make sure `ksp` plugin is in `app/build.gradle.kts` and Gradle is synced

**PDF not opening after export**
→ Install a PDF viewer app on your device/emulator

**Room crash after schema change**
→ Uninstall the app and re-install (resets DB during development)

---

## 📄 License

MIT — free to use, modify, distribute, and publish on the Play Store.
