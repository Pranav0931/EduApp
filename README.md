# EduApp 📚🎓
### AI-Powered Inclusive Learning Platform for All Students

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-1.9.24-purple?style=for-the-badge&logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue?style=for-the-badge" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=for-the-badge" alt="Target SDK"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License"/>
</p>

<p align="center">
  <b>Making education accessible to everyone — Blind, Deaf, Low-Vision, Slow Learners & Regular Students</b>
</p>

---

## 🌟 About EduApp

EduApp is an **Android-based inclusive learning application** built with **accessibility as its core foundation**, not an afterthought. Developed after extensive research with **blind and deaf students at Anandwan, Warora**, this app addresses real challenges students face while accessing digital learning tools.

> **Our Mission:** Make quality education accessible to every student, regardless of their abilities.

---

## 🎨 Modern UI Design (v2.0)

### ✨ What's New
- **Modern Gradient UI** — Beautiful color gradients throughout the app
- **Large Accessible Cards** — Easy-to-tap mode selection cards
- **Colorful Subject Cards** — Each subject has unique gradient colors
- **Enhanced Audio Player** — Gradient background with large playback controls
- **TalkBack Voice Guidance** — Complete voice feedback system for blind users

### 📱 Screenshots

| Mode Selection | Home Dashboard | Audio Player |
|:---:|:---:|:---:|
| Choose Audio/Video mode | Start Learning, Quiz, Progress | Modern gradient player |

---

## ♿ Accessibility-First Architecture

### 🎙️ TalkBack Voice Guidance System (NEW!)
| Feature | Description |
|---------|-------------|
| **VoiceGuidanceManager** | Comprehensive voice announcements for all interactions |
| **Screen Announcements** | Automatic voice feedback when navigating screens |
| **Button Press Feedback** | Voice confirmation for every button tap |
| **Haptic Patterns** | Different vibration patterns for different actions |
| **Speed & Pitch Control** | Customize voice speed (0.5x-2.0x) and pitch |
| **Focus Announcements** | Voice reads focused element descriptions |

### 👁️ For Blind Students
| Feature | Description |
|---------|-------------|
| **OCR Engine** | ML Kit-powered camera-to-text — point at any text and hear it read aloud |
| **Text-to-Speech** | Natural voice synthesis with adjustable speed (0.5x - 2.0x) |
| **Voice Navigation** | Complete hands-free control with voice commands |
| **Haptic Feedback** | Tactile responses for navigation, success, and errors |
| **TalkBack Optimized** | Full compatibility with Android's screen reader |
| **Audio Descriptions** | Detailed audio context for all visual content |

### 🦻 For Deaf Students
| Feature | Description |
|---------|-------------|
| **Auto Subtitles** | Real-time caption generation for all audio content |
| **Visual Alerts** | Color-coded pop-up notifications instead of sounds |
| **Flash & Vibration** | Screen flash and haptic alerts for important events |
| **Speaker Identification** | Subtitles show who is speaking |
| **Sound Effect Labels** | Visual indicators for music, bells, and sound effects |

### 👓 For Low-Vision Users
| Feature | Description |
|---------|-------------|
| **High Contrast Mode** | Maximum contrast with bold black text on white |
| **Dynamic Font Scaling** | Text sizes from 1.0x to 1.5x system default |
| **Bold Text Mode** | Enhanced text weight for better readability |
| **Large Touch Targets** | Minimum 48dp-56dp touch areas |
| **Inverted Colors** | Optional dark-on-light inversion |

### 📚 For Slow Learners
| Feature | Description |
|---------|-------------|
| **Adaptive Pacing** | AI analyzes learning speed and adjusts content delivery |
| **Extended Quiz Time** | Configurable time multiplier (up to 2x) |
| **Encouragement System** | Positive reinforcement cards with emojis |
| **Simplified Navigation** | Reduced UI complexity for easier interaction |
| **Study Analytics** | Tracks progress and identifies struggling areas |
| **Smart Recommendations** | AI-generated personalized study suggestions |

---

## 🚀 Key Features

### 🤖 AI-Powered Features
- **Google Gemini Integration** — Intelligent tutoring and content explanations
- **ML Kit OCR** — Camera-based text recognition for print materials
- **Adaptive Learning Engine** — Personalized study recommendations
- **Voice Command Recognition** — Natural language navigation
- **AI Chat Assistant** — Floating AI chat accessible from any screen
- **AI Quiz Generation** — Generate quizzes from any chapter content

### 📖 Learning Content
- **6 Subjects:** English, Marathi, Mathematics, Science, Hindi, Social Science
- **Multi-format Content:** Text, Audio, Video with accessibility layers
- **Chapter-based Learning:** Structured curriculum progression
- **Interactive Quizzes:** Accessible quiz system with immediate feedback
- **Text Reader:** Dedicated reading experience with TTS and adjustable font sizes

### 👨‍👩‍👧‍👦 Multi-User Support
- **Student Profiles** — Individual accessibility preferences saved
- **Parent Dashboard** — Monitor child's learning progress
- **Offline Mode** — Core features work without internet

---

## 🏗️ Technical Architecture

### Clean Architecture + MVVM
```
app/
├── data/                    # Data Layer
│   ├── local/              
│   │   ├── dao/            # Room DAOs (StudyToolsDao, etc.)
│   │   ├── entity/         # Database Entities (Flashcard, Bookmark, etc.)
│   │   └── EduAppDatabase.kt
│   ├── remote/             # Network APIs
│   └── repository/         # Repository Implementations
├── domain/                  # Domain Layer
│   ├── model/              # Business Models (Flashcard, StudyTools, etc.)
│   ├── repository/         # Repository Interfaces
│   └── usecase/            # Use Cases
├── accessibility/           # Accessibility Engine
│   ├── OCREngine.kt        # ML Kit OCR
│   ├── TTSEngine.kt        # Text-to-Speech
│   ├── VoiceNavigationManager.kt
│   ├── DeafSupportManager.kt
│   └── AdaptiveStudyEngine.kt
├── presentation/            # Presentation Layer (MVVM)
│   ├── base/               # Base classes (BaseActivity, BaseFragment)
│   ├── book/               # Book list feature
│   ├── player/             # Audio/Video players
│   └── ...                 # Other features
├── core/                    # Core Utilities
│   ├── accessibility/      # ScreenReaderHelper, EduAccessibilityManager
│   ├── ui/                 # UiState, StateView, VisualFeedbackView
│   ├── di/                 # Hilt Dependency Injection
│   └── logging/            # CrashlyticsTree
└── utils/                   # Helper Classes
    ├── LocaleHelper.kt     # Language/Locale management
    ├── Constants.kt        # App constants
    └── PreferenceManager.kt
```

### 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 1.9.24 |
| **UI Framework** | Android Views + DataBinding |
| **Architecture** | Clean Architecture + MVVM |
| **Dependency Injection** | Hilt 2.51.1 |
| **Database** | Room 2.6.1 |
| **Networking** | Retrofit 2.11 + OkHttp 4.12 |
| **AI/ML** | Google Gemini AI, ML Kit |
| **Media** | ExoPlayer 1.2.1 |
| **Async** | Kotlin Coroutines + Flow |
| **Navigation** | Jetpack Navigation 2.8.5 |
| **Analytics** | Firebase Crashlytics |
| **Security** | AndroidX Security Crypto, Tink |
| **Image Loading** | Coil 2.6 |

---

## 📊 Impact & Validation

<p align="center">
  <img src="https://img.shields.io/badge/User%20Satisfaction-92%25-success?style=for-the-badge" alt="92% Satisfaction"/>
  <img src="https://img.shields.io/badge/Tested%20With-Real%20Students-blue?style=for-the-badge" alt="Real Testing"/>
</p>

- ✅ Field tested with blind and deaf students at Anandwan
- ✅ **92% of participants** would recommend the app
- ✅ Validated by teachers and accessibility experts
- ✅ Positive feedback on simplicity and usability

---

## 🆕 Recent Updates (v2.1)

### ✨ New Features
- **📚 Study Tools Suite** — Complete study companion with Flashcards, Bookmarks, Highlights, Study Notes, and Homework Reminders
- **🗂️ Flashcard System** — Spaced repetition learning with customizable decks, TTS support, and progress tracking
- **📖 Bookmark & Highlight** — Save important content with color-coded highlights and personal notes
- **📝 Study Notes** — Create rich text notes with voice note support and chapter/book associations
- **⏰ Homework Reminders** — Track assignments with due dates, priorities, and smart notifications
- **🌐 Hindi Localization** — Complete Hindi language support for all features (350+ strings)
- **🔄 Visual Feedback System** — Enhanced visual alerts and subtitles for deaf users
- **♿ Screen Reader Helper** — Optimized TalkBack announcements with Hindi + English support

### 🛠️ Technical Improvements
- **UiState Architecture** — Unified state management with Loading, Success, Error, and Empty states
- **StateView Component** — Reusable UI component for consistent state display across screens
- **VisualFeedbackView** — Animated visual alerts and subtitle overlays for deaf users
- **Navigation Animations** — Smooth enter/exit transitions for better UX
- **Room Database Extensions** — New DAOs and entities for study tools (Flashcards, Bookmarks, Highlights, Notes, Reminders)
- **LocaleHelper** — Runtime language switching with proper configuration handling
- **Premium Drawables** — Glassmorphism backgrounds, gradient headers, and shimmer loading placeholders

---

## 📌 Previous Updates (v2.0)

### ✨ Features
- **AI Chat Floating Button** — Access the AI assistant from any screen via the floating action button
- **Quiz System** — Complete quiz browsing with AI-generated quizzes from chapter content
- **Text Reader** — Dedicated reader with adjustable text sizes and read-aloud functionality
- **Profile Sub-screens** — Edit profile, view badges, and detailed learning statistics
- **Settings Sub-screens** — Dedicated accessibility settings, notification preferences, and about page
- **Improved Navigation** — All navigation paths properly connected and verified

### 🛠️ Technical Improvements
- Fixed navigation graph paths for media players
- Created all missing fragments for complete user flows
- Added haptic feedback throughout the app
- Improved accessibility mode integration across all screens
- Enhanced quiz adapter with AI quiz badges and difficulty chips

---

## �🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- Min Android 7.0 (API 24)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Pranav0931/EduApp.git
   cd EduApp
   ```

2. **Create `local.properties`** in the root directory:
   ```properties
   sdk.dir=YOUR_ANDROID_SDK_PATH
   
   # API Keys (get from respective platforms)
   GEMINI_API_KEY=your_gemini_api_key
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_key
   ```

3. **Add `google-services.json`**
   - Create a Firebase project
   - Download `google-services.json`
   - Place in `app/` directory

4. **Build & Run**
   ```bash
   ./gradlew assembleDevDebug
   ```
   Or open in Android Studio and click Run ▶️

---

## 📱 Voice Commands Reference

| Command | Action |
|---------|--------|
| "Go home" / "Main menu" | Navigate to home screen |
| "Go back" | Return to previous screen |
| "Read aloud" / "Speak" | Read current content |
| "Stop reading" | Stop TTS playback |
| "Next chapter" | Go to next content |
| "Previous chapter" | Go to previous content |
| "Start quiz" | Begin quiz mode |
| "Select option [1-4]" | Choose quiz answer |
| "Help" | Get available commands |
| "Slower" / "Faster" | Adjust speech rate |

---

## 🤝 Contributing

We welcome contributions! EduApp is open for collaboration in:

- 🔧 **Development** — New features, bug fixes, optimizations
- 🎨 **Design** — UI/UX improvements, accessibility enhancements
- 📝 **Content** — Educational materials, translations
- 🧪 **Testing** — Accessibility testing, device testing
- 📚 **Documentation** — Guides, tutorials, API docs

### How to Contribute
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

<p align="center">
  <b>Pranav Tapdiya</b><br>
  <a href="https://linkedin.com/in/pranav-tapdiya">
    <img src="https://img.shields.io/badge/LinkedIn-Connect-blue?style=for-the-badge&logo=linkedin" alt="LinkedIn"/>
  </a>
  <a href="https://github.com/Pranav0931">
    <img src="https://img.shields.io/badge/GitHub-Follow-black?style=for-the-badge&logo=github" alt="GitHub"/>
  </a>
</p>

---

## 🔗 Links

- **Demo Video:** [LinkedIn Post](https://www.linkedin.com/posts/pranav-tapdiya_edu-app-activity-7404168671969021952-CAc9)
- **Repository:** [GitHub](https://github.com/Pranav0931/EduApp)

---

<p align="center">
  <b>⭐ Star this repository if you believe in inclusive technology!</b><br><br>
  <i>"Technology should empower everyone, not exclude anyone."</i>
</p>
