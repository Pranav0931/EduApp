# PROJECT_CONTEXT.md — EduApp Developer Reference

> **Purpose:** Quick-reference document for AI assistants and developers working on this codebase.
> **Last updated:** 2026-02-12

---

## 1. Project Overview

**EduApp** is an Android education app targeting Indian students in classes 1–10, with a strong accessibility-first design for **blind**, **deaf**, **low-vision**, and **slow-learner** students.

| Field | Value |
|-------|-------|
| Package | `com.hdaf.eduapp` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Language | Kotlin (modern) + Java (legacy) |
| Locales | English (`en`), Hindi (`hi`), Marathi (`mr`) |
| Build System | Gradle KTS, AGP, Kotlin 1.9+ |

**Target users:** Blind students (TalkBack + TTS), Deaf students (ISL videos + captions + visual alerts), Low-vision students (font scaling + high contrast), Slow learners (adaptive pacing), and standard students.

---

## 2. Architecture

### Pattern: Clean Architecture + MVVM + UDF

```
Presentation (Fragments + ViewModels)
        ↓ observes StateFlows / dispatches Actions
Domain (Use Cases + Repository Interfaces + Models)
        ↓ called by use cases
Data (Repository Impls + Room DB + Retrofit APIs + AI Service)
```

### Dual Codebase (Legacy + Modern)

The project has **two parallel UI stacks**:

| Stack | Entry Point | Navigation | Language |
|-------|------------|------------|----------|
| **Legacy** | `SplashScreenActivity` (LAUNCHER) | Activity-to-Activity Intents | Java |
| **Modern** | `MainActivity` (not in Manifest) | Single-Activity + Jetpack Navigation | Kotlin |

**Legacy Java Activities** (12 total, all declared in AndroidManifest):
`SplashScreenActivity`, `ModeSelectionActivity`, `ClassSelectionActivity`, `BookListActivity`, `ChapterListActivity`, `AudioPlayerActivity`, `VideoPlayerActivity`, `QuizActivity`, `QuizListActivity`, `QuizResultActivity`, `GamificationActivity`, `SettingsActivity`

**Modern Kotlin Fragments** (25+, in `presentation/` package):
`SplashFragment`, `LanguageSelectionFragment`, `OnboardingFragment`, `NavigationTutorialFragment`, `ModeSelectionFragment`, `PhoneInputFragment`, `OtpVerificationFragment`, `RegistrationFragment`, `HomeFragment`, `BookListFragment`, `ChapterListFragment`, `ReaderFragment`, `AudioPlayerFragment`, `VideoPlayerFragment`, `QuizListFragment`, `QuizActiveFragment`, `QuizResultFragment`, `ProfileFragment`, `EditProfileFragment`, `BadgesFragment`, `StatsFragment`, `LeaderboardFragment`, `SettingsFragment`, `AccessibilitySettingsFragment`, `NotificationSettingsFragment`, `AboutFragment`

> ⚠️ **Important:** The Java Activities are currently the active entry point. The Kotlin navigation graph exists but `MainActivity` is NOT declared in AndroidManifest. Both stacks share the same data layer.

---

## 3. Major Modules & Packages

### `core/` — Infrastructure (Kotlin)
- `core/accessibility/` — 12 manager classes (EduAccessibilityManager, TTSManager, BlindGestureManager, BrailleHapticManager, CaptionManager, DeafAlertManager, SignLanguageManager, AudioDescriptionManager, ScreenReaderHelper, VoiceControlManager, ChapterAudioManager, AccessibilitySettingsManager)
- `core/di/` — 6 Hilt modules (AppModule, DatabaseModule, NetworkModule, RepositoryModule, AccessibilityModule, AIModule)
- `core/common/` — `Resource<T>` sealed class, `FlowExtensions`, `UseCase` base
- `core/network/` — AuthInterceptor, NetworkMonitor, RateLimiter
- `core/security/` — EncryptedSharedPreferences wrapper
- `core/worker/` — SyncWorker (6-hour periodic), ContentDownloadWorker
- `core/ui/` — UiState, StateView, VisualFeedbackView
- `core/logging/` — CrashlyticsTree (Timber)
- `core/memory/` — MemoryMonitor

### `data/` — Data Layer (Kotlin)
- `data/local/` — Room database (`eduapp_database`), 25+ entities, 12+ DAO files (20+ DAO interfaces), TypeConverters
- `data/remote/` — 4 Retrofit API interfaces (AuthApi, ContentApi, GamificationApi, QuizApi), 5 DTO files
- `data/repository/` — 9 repository implementations
- `data/mapper/` — 6 entity↔domain mapper files
- `data/ai/` — GeminiAIServiceImpl

### `domain/` — Business Logic (Kotlin)
- `domain/model/` — 15 domain model files (User, Book, Chapter, Quiz, Badge, AccessibilityProfile, Flashcard, StudyPlanner, StudyTools, DownloadManager, etc.)
- `domain/repository/` — 10+ repository interfaces across 7 files
- `domain/usecase/` — ~35 use cases in 6 subdirectories (auth, content, gamification, progress, quiz, user)
- `domain/ai/` — EduAIService interface (9 methods)

### `presentation/` — UI Layer (Kotlin)
- Feature-based packages: `auth/`, `home/`, `book/`, `chapter/`, `player/`, `quiz/`, `profile/`, `leaderboard/`, `settings/`, `onboarding/`, `language/`, `tutorial/`, `mode/`, `reader/`, `splash/`
- `base/` — BaseFragment, BaseActivity, AccessibleBaseFragment, BaseViewModel
- `MainActivity.kt` + `MainViewModel.kt`

### `accessibility/` — Legacy Accessibility (Mixed Java/Kotlin)
- `TTSManager.java` — Java TTS singleton with getInstance()
- `TTSEngine.kt` — Kotlin TTS engine
- `VoiceGuidanceManager.java` — Voice-guided navigation
- `VoiceRecognitionManager.java` — Speech-to-text
- `VoiceNavigationManager.kt` — Kotlin voice navigation
- `DeafSupportManager.kt` — Deaf user features
- `AdaptiveStudyEngine.kt` — Slow learner pacing
- `OCREngine.kt` — ML Kit text recognition

### `activities/` — Legacy Activities (Java)
12 Activity files with full layouts, used as the active app entry point.

### `adapters/` — Legacy RecyclerView Adapters (Java)
`BookAdapter`, `ChapterAdapter`, `QuizOptionAdapter`, `ChatMessageAdapter`

### `ai/` — Legacy AI System (Java)
- `EduAIService.java` — Direct Gemini REST API client with Hinglish personality prompt
- `IntentModel.java` — Parsed intent model with IntentType enum
- `IntentParser.java` — Regex-based Hinglish NLP
- `NavigationHandler.java` — Intent→Activity navigation

### `gamification/` — Legacy Gamification (Java)
`GamificationManager`, `Achievement`, `BadgeRegistry`, `DailyChallenge`, `LevelCalculator`, `UserProgress`, `GamificationStorage`, `XPRewardResult`

### `analytics/` — Legacy Analytics (Java)
`AnalyticsTracker`, `AnalyticsEvent`, `AnalyticsStorage`, `LearningSession`, `SubjectPerformance`

### `supabase/` — Legacy Supabase Client (Java)
`SupabaseClient`, `SupabaseApi`, `SupabaseRepository` — Retrofit-based PostgREST client

### `media/` — Media Playback (Kotlin)
`VideoPlayerManager` — ExoPlayer (Media3) singleton wrapper with StateFlow

### `ui/` — Shared UI Components
- `EduAIChatBottomSheet.java` — AI chat with voice input, quick actions, TTS responses
- `ui/accessibility/` — AccessibilityViewHelper, AccessibilityViewModel, SubtitleOverlayManager, VisualAlertManager

### `utils/` — Utilities (Java)
`Constants.java`, `PreferenceManager.java`, `LocaleHelper.kt`

---

## 4. Accessibility Systems

### Accessibility Modes (from `AccessibilityModeType` enum)
| Mode | Features Enabled |
|------|------------------|
| `BLIND` / `VISUAL` | TalkBack optimization, TTS for all content, haptic feedback, voice control, braille-like vibration patterns, audio descriptions, blind gesture navigation |
| `DEAF` | Indian Sign Language (ISL) video overlay, real-time captions, visual flash alerts, subtitle overlay, haptic alerts |
| `LOW_VISION` | Font scaling (0.8–2.0), high contrast mode, color adjustments |
| `COGNITIVE` / `SLOW_LEARNER` | Adaptive pacing engine, simplified content, extended timers |
| `STANDARD` | All features available, nothing auto-enabled |

### Key Accessibility Classes
| Class | Role |
|-------|------|
| `EduAccessibilityManager` (@Singleton) | Central hub: TTS (Hindi default), haptic feedback (CLICK/SUCCESS/ERROR/WARNING/NAVIGATION), accessibility state monitoring |
| `TTSManager.java` | Java singleton used by legacy Activities and some Fragments |
| `TTSManager.kt` | Kotlin @Singleton with Flow integration, used by modern ViewModels |
| `BlindGestureManager` | Custom gesture recognition for blind navigation |
| `BrailleHapticManager` | Vibration patterns simulating braille feedback |
| `DeafAlertManager` | Visual flash alerts replacing sound |
| `SignLanguageManager` | ISL video overlay management |
| `CaptionManager` | Real-time caption rendering |
| `AudioDescriptionManager` | Describes visual content for blind users |
| `VoiceControlManager` | Voice command processing |
| `AdaptiveStudyEngine` | Pacing adaptation for slow learners |
| `OCREngine` | ML Kit text recognition from camera images |

### Accessibility in UI
- All interactive elements have `contentDescription` attributes
- Minimum touch targets: 48dp
- Bilingual strings (English + Hindi) throughout
- TalkBack announcements on every screen transition
- Haptic feedback on every user interaction
- Voice commands supported (Hindi + English)

---

## 5. Navigation Flow

### Navigation Graph (`nav_graph.xml`)

```
app launch
    ↓
SplashFragment
    ├── (no language selected) → LanguageSelectionFragment → OnboardingFragment
    ├── (not onboarded) → OnboardingFragment → NavigationTutorialFragment → ModeSelectionFragment
    ├── (not logged in) → Auth Nested Graph:
    │       PhoneInputFragment → OtpVerificationFragment → RegistrationFragment → HomeFragment
    └── (logged in) → HomeFragment

HomeFragment (main dashboard)
    ├── → BookListFragment → ChapterListFragment → ReaderFragment
    │                                            → AudioPlayerFragment
    │                                            → VideoPlayerFragment
    ├── → QuizListFragment → QuizActiveFragment → QuizResultFragment
    ├── → ProfileFragment → EditProfileFragment / BadgesFragment / StatsFragment
    ├── → LeaderboardFragment
    └── → SettingsFragment → AccessibilitySettingsFragment
                           → NotificationSettingsFragment
                           → AboutFragment
```

**Bottom Navigation** (in MainActivity): Home, Books, Quizzes, Profile, Leaderboard
**SafeArgs** used for type-safe argument passing between fragments.
**Custom animations** on all navigation actions (enter/exit/popEnter/popExit).

---

## 6. AI Integrations

### Gemini AI (Google Generative AI)
| Feature | Model | Purpose |
|---------|-------|---------|
| Quiz Generation | `gemini-1.5-flash` (strict, temp=0.3) | Generate quizzes from chapter content as structured JSON |
| Adaptive Quiz | `gemini-1.5-flash` (strict) | Generate quizzes targeting weak topics |
| Concept Explanation | `gemini-1.5-flash` (creative, temp=0.7) | Explain concepts in simple Hindi |
| Question Answering | `gemini-1.5-flash` (creative) | Answer student questions |
| Chapter Summary | `gemini-1.5-flash` (creative) | Summarize chapters |
| Practice Problems | `gemini-1.5-flash` (creative) | Generate practice exercises |
| Content Translation | `gemini-1.5-flash` (creative) | Translate to Hindi/Marathi/English |
| Voice Intent Parsing | `gemini-1.5-flash` (strict) | Parse Hinglish voice commands into navigation intents |
| EduAI Chat | `gemini-1.5-flash` (via REST) | Conversational Hinglish tutor with personality |

**Two implementations exist:**
1. `GeminiAIServiceImpl.kt` — Modern Kotlin, uses Google Generative AI SDK, Hilt-injected
2. `EduAIService.java` — Legacy Java, uses OkHttp REST calls to `generativelanguage.googleapis.com`

### ML Kit
- **Text Recognition** via `OCREngine.kt` — Camera → bitmap → extracted text → TTS/save as note

---

## 7. Data Layer

### Room Database
- **Name:** `eduapp_database`
- **Tables:** 25+ (books, chapters, quizzes, quiz_questions, quiz_attempts, user_progress, badges, accessibility_profiles, ai_chat_messages, study_analytics, chapter_audio_progress, quiz_progress, notes, text_highlights, downloaded_content, download_queue, study_sessions, study_plans, study_goals, daily_study_summaries, flashcards, flashcard_decks, bookmarks, highlights)
- **Migration:** `fallbackToDestructiveMigration()` — schema changes wipe all local data
- **Type Converters:** JSON serialization for lists (StringListConverter, IntListConverter, etc.)

### Retrofit APIs
| API | Base URL | Auth |
|-----|----------|------|
| `AuthApi` | Supabase REST | API key header |
| `ContentApi` | Supabase PostgREST | API key + Bearer token |
| `GamificationApi` | Backend server | Bearer token |
| `QuizApi` | Backend server | Bearer token |

### Supabase (Legacy)
`SupabaseClient.java` wraps Retrofit for PostgREST access to books/chapters tables. Falls back to local mock data when unconfigured.

### Offline Support
- Room database as local cache for all entities
- `DownloadedContentEntity` / `DownloadQueueEntity` for offline content
- `SyncWorker` runs every 6 hours to sync content, quiz attempts, and progress
- `ContentDownloadWorker` for on-demand book/chapter downloads
- `isSynced` flags on entities track sync status

---

## 8. Study Tools & Learning Modules

| Feature | Implementation | Status |
|---------|---------------|--------|
| Audio Player | `AudioPlayerFragment` + `AudioPlayerViewModel` + `ChapterAudioManager` | TTS-based chapter reading with speed control |
| Video Player | `VideoPlayerFragment` + `VideoPlayerManager` (ExoPlayer) | Media3 playback with ISL overlay support |
| Reader | `ReaderFragment` + `ReaderViewModel` | Text-based chapter reading |
| Quizzes | `QuizActiveFragment` + `QuizViewModel` | Timer, 4 options, AI generation, XP awards |
| AI Chat | `EduAIChatBottomSheet` | Voice + text input, Hinglish tutor personality |
| Flashcards | `FlashcardEntity` + `FlashcardDeckEntity` + DAOs | Spaced repetition system (entities exist, UI partial) |
| Notes | `NoteEntity` + `NotesDao` | Save notes from OCR scans and manual entry |
| Bookmarks | `BookmarkEntity` + `BookmarkDao` | Chapter bookmarking with color coding |
| Highlights | `HighlightEntity` + `HighlightDao` + `TextHighlightEntity` | Text highlighting in reader |
| Study Planner | `StudySession` + `StudyPlan` + `StudyGoal` entities + DAOs | Weekly goals, scheduling (entities exist, UI partial) |
| OCR Scanner | `OCREngine` (ML Kit) | Camera → text → TTS/save as note |
| Gamification | XP, levels, badges, streaks, daily challenges, leaderboard | Dual implementation (Java legacy + Kotlin modern) |

---

## 9. UI Design Direction

- **Theme:** Material 3 (`Theme.Material3.DayNight.NoActionBar`)
- **Brand Colors:** Indigo primary (`#6366F1`), Purple secondary (`#8B5CF6`), Pink accent (`#EC4899`)
- **Cards:** 20dp corner radius, elevation-based depth
- **Buttons:** 52dp min height, 24dp corner radius
- **Touch Targets:** 48dp minimum for accessibility
- **Typography:** Material 3 Display/Headline/Title/Body/Label scale
- **Accessibility Colors:** High-contrast set (black bg, white text, yellow accent `#FFD60A`)
- **Per-class Colors:** 9 unique card colors for standards 1–9
- **Subject Colors:** Unique per subject (English=blue, Math=red, Science=green, etc.)

---

## 10. Coding & Architecture Rules

### DO:
1. **Follow Clean Architecture** — Domain layer must not depend on Data or Presentation
2. **Use Hilt** for all dependency injection — `@AndroidEntryPoint` on Fragments/Activities, `@HiltViewModel` on ViewModels
3. **Use Kotlin coroutines + Flow** for async operations — `StateFlow` for UI state, `Flow` for database observations
4. **Use SafeArgs** for navigation arguments between fragments
5. **Add accessibility support** to every new screen — `contentDescription`, TalkBack announcements, haptic feedback, minimum 48dp touch targets
6. **Add Hindi translations** for all user-facing strings in `values-hi/strings.xml`
7. **Use `Resource<T>` sealed class** for operation results — `Success`, `Error`, `Loading`
8. **Use ViewBinding** for all fragment/activity view access — no `findViewById` in new code
9. **Follow UDF pattern** in ViewModels — sealed class Actions dispatched from UI, StateFlow observed by UI
10. **Keep PreferenceManager.java** as the singleton for SharedPreferences in Java code, `SharedPreferences` injection for Kotlin

### DON'T:
1. **Never break accessibility** — Every change must maintain TalkBack, TTS, haptic, and caption support
2. **Never remove the legacy Java Activities** without adding `MainActivity` to AndroidManifest first
3. **Never use `findViewByld` in Kotlin fragments** — use ViewBinding
4. **Never make network calls on main thread** — always `withContext(Dispatchers.IO)` or use Room's Flow
5. **Never hardcode strings** — all user-facing text in `strings.xml` with Hindi translation
6. **Never store sensitive data in plain SharedPreferences** — use `SecurePreferences` for tokens/keys
7. **Don't mix legacy and modern patterns** in new code — all new code should use Kotlin Clean Architecture

### Build Flavors:
- **`dev`** — For development, no minification, `.dev` suffix
- **`prod`** — For release, minified + shrunk, ProGuard rules applied
- **`staging`** — Debuggable but with minification for testing

---

## 11. Development Priorities

1. **Register `MainActivity` in AndroidManifest** — Critical for enabling the Kotlin navigation stack
2. **Migrate legacy Java Activities to Kotlin Fragments** — Gradual; keep both running until complete
3. **Replace `fallbackToDestructiveMigration()`** with proper Room migrations before production
4. **Complete partial UI features** — Flashcard study screen, Study planner screens, Homework reminders
5. **Connect API endpoints** to real backend — Currently many features use local Room data only
6. **Add proper error handling and retry** across all network operations
7. **Optimize ExoPlayer usage** in VideoPlayerFragment — Currently basic integration
8. **Performance profiling** — 25+ tables, large entity graph, potential memory concerns

---

## 12. Important Constraints

- **Accessibility is non-negotiable** — The app's core value proposition is accessibility for disabled students. Never ship a feature without TalkBack, TTS, and haptic support.
- **Hindi-first content** — All AI prompts are in Hindi. All UI strings must have Hindi translations.
- **Offline capability** — Students may have limited connectivity. All core features must work offline with periodic sync.
- **Low-end device support** — Min SDK 24 targets older devices. Watch memory usage and animation performance.
- **Dual codebase coexistence** — Legacy Java and modern Kotlin must coexist until migration is complete. Both share the same Room database, PreferenceManager, and accessibility managers.
