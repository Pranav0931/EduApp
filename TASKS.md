# TASKS.md — EduApp Development Progress Tracker

> **Last updated:** 2026-02-12

---

## ✅ Completed Features

### Core Architecture
- [x] Clean Architecture setup (Presentation → Domain → Data)
- [x] Hilt dependency injection (6 modules: App, Database, Network, Repository, Accessibility, AI)
- [x] Room database with 25+ entities and 20+ DAOs
- [x] Retrofit API layer (AuthApi, ContentApi, GamificationApi, QuizApi)
- [x] Kotlin coroutines + Flow throughout data layer
- [x] Resource sealed class for operation results
- [x] ViewBinding enabled for all layouts
- [x] SafeArgs for navigation arguments
- [x] Build flavors (dev, prod, staging) with proper signing configs

### Onboarding & Auth Flow
- [x] Language selection screen (Hindi/English/Marathi) with TTS + haptic feedback
- [x] 3-page onboarding (Audio Mode, Video Mode, AI Assistant)
- [x] Navigation tutorial (5 pages: Welcome, Gestures, Deaf features, Blind features, Navigation)
- [x] Mode selection (Audio/Video/Both/Standard)
- [x] Phone + OTP authentication flow (PhoneInput → OtpVerification → Registration)
- [x] Splash screen with auth state routing

### Home & Navigation
- [x] Home dashboard (progress, recent chapters, recommended books, streak, XP)
- [x] Jetpack Navigation graph with 25+ fragment destinations
- [x] Bottom navigation (Home, Books, Quizzes, Profile, Leaderboard)
- [x] Quick action cards on home (Start Learning, Daily Quiz, My Progress) with click handlers + haptic feedback
- [x] Network status monitoring with offline mode indicator

### Content & Learning
- [x] Book list display by class (grid layout)
- [x] Chapter list with progress indicators
- [x] Reader fragment for text-based chapters
- [x] Audio player with TTS-based chapter reading, speed control, per-chapter progress isolation
- [x] Video player with ExoPlayer (Media3) integration via VideoPlayerManager
- [x] ISL (Indian Sign Language) overlay layout in video player
- [x] Subtitle/caption overlay system
- [x] Content download system (DownloadedContentEntity, DownloadQueueEntity, ContentDownloadWorker)

### Quizzes
- [x] Quiz list screen with TalkBack support
- [x] Active quiz with timer, 4-option MCQ, progress tracking
- [x] AI-generated quizzes via Gemini (chapter-based and adaptive)
- [x] Quiz result screen with score, stats, motivational feedback
- [x] Quiz attempt persistence and offline support
- [x] XP awards for quiz completion (with difficulty multipliers)
- [x] Quiz resume support (QuizProgressEntity)

### AI Integration
- [x] Gemini AI service (Kotlin) — quiz gen, concept explanation, Q&A, summarization, translation, intent parsing
- [x] Legacy EduAI service (Java) — REST-based Hinglish conversational tutor
- [x] EduAI Chat bottom sheet with voice input + text input + quick actions
- [x] Intent parser for Hinglish voice commands (regex-based)
- [x] Navigation handler for AI-parsed intents
- [x] OCR engine (ML Kit) for camera text scanning with save-as-note

### Gamification
- [x] XP system with 10 sources (quiz, chapter, book, login, streak, perfect score, etc.)
- [x] Level calculation system
- [x] Badge system with categories (Streak, Learning, Quiz, Achievement, Social)
- [x] Daily streak tracking with celebration on 7-day milestones
- [x] Daily challenge system
- [x] Leaderboard (weekly/monthly/all-time with filters)
- [x] Profile with stats display (XP, level, streak, badges, rank)

### Accessibility
- [x] EduAccessibilityManager — central TTS + haptic + state monitoring singleton
- [x] TTSManager (Java singleton) for legacy Activities
- [x] TTSManager (Kotlin @Singleton) with Flow integration for modern ViewModels
- [x] Blind mode: TalkBack optimization, voice control, audio descriptions, blind gesture navigation
- [x] Deaf mode: Visual flash alerts, caption manager, sign language manager
- [x] Braille-like haptic vibration patterns
- [x] Voice recognition manager for speech-to-text commands
- [x] Adaptive study engine for slow learners
- [x] High contrast mode support
- [x] Font scaling support (0.8–2.0)
- [x] Bilingual UI strings (English + Hindi) for all screens
- [x] Minimum 48dp touch targets on all interactive elements
- [x] Content descriptions on all UI elements
- [x] Screen reader announcements on screen transitions

### Data & Sync
- [x] SyncWorker (6-hour periodic) — syncs content, quiz attempts, progress
- [x] ContentDownloadWorker for on-demand offline downloads
- [x] isSynced flags on entities for sync state tracking
- [x] Supabase PostgREST integration for books/chapters (legacy Java)
- [x] SSL certificate pinning in OkHttpClient
- [x] EncryptedSharedPreferences for secure token storage
- [x] Crashlytics integration via Timber CrashlyticsTree

### Settings
- [x] Settings hub screen
- [x] Accessibility settings (voice speed, pitch, TalkBack, blind/deaf feature toggles)
- [x] Notification settings screen
- [x] About screen
- [x] Language change with locale switching

### Study Tools
- [x] Flashcard study screen with flip animation, spaced repetition rating (Again/Hard/Good/Easy), TTS
- [x] FlashcardStudyViewModel with card loading, review rating, deck completion events
- [x] Study Planner screen (today summary, study goals, homework reminders, sessions)
- [x] StudyPlannerViewModel with DAO-backed data loading
- [x] Navigation graph destinations for flashcard study and study planner
- [x] Room database entities for flashcards, bookmarks, highlights, homework reminders, study notes
- [x] Room database DAOs for all study tools (6 additional DAOs registered)
- [x] Hilt DI providers for all study tool DAOs

---

## 🔄 In Progress / Partially Complete

### ~~MainActivity Registration~~ ✅ DONE
- [x] **`MainActivity` registered in AndroidManifest.xml** — Added with proper theme and `windowSoftInputMode="adjustResize"`.

### ~~Database Migration Strategy~~ ✅ DONE
- [x] **Proper Room migration added (v6→v7)** — Migration creates 6 new tables (flashcards, flashcard_decks, bookmarks, highlights, homework_reminders, study_notes) with indices. `fallbackToDestructiveMigration()` retained only as fallback for pre-release versions.

### Flashcard Study
- [x] FlashcardEntity + FlashcardDeckEntity + DAOs with spaced repetition fields
- [x] `fragment_flashcard_study.xml` layout exists
- [x] **FlashcardStudyFragment Kotlin implementation** — Created with flip animation, rating buttons, TTS, accessibility
- [x] **FlashcardStudyViewModel** — Card loading, spaced repetition rating, deck completion
- [ ] **Flashcard creation UI** — No screen to create/edit flashcards (future task)

### Study Planner
- [x] StudySessionEntity + StudyPlanEntity + StudyGoalEntity + DailyStudySummaryEntity + DAOs
- [x] **Study planner UI screen** — `StudyPlannerFragment` with today's summary, goals, homework, sessions
- [x] **StudyPlannerViewModel** — DAO-backed state management
- [ ] **Homework reminder creation UI** — HomeworkReminderEntity exists but no creation form screen
- [ ] **Study goal creation UI** — Can view goals but no creation form

### Backend API Connection
- [ ] **Real backend deployment** — AuthApi, GamificationApi, QuizApi endpoints defined but server deployment status unknown
- [ ] **Supabase tables provisioned** — SupabaseClient configured but may not have all tables populated
- [x] Local Room fallback works for offline-first usage

---

## 🐛 Known Issues

### ~~Critical~~ (Resolved)
1. ~~**`MainActivity` not in AndroidManifest**~~ ✅ **FIXED** — Registered in AndroidManifest.xml.
2. ~~**`fallbackToDestructiveMigration()`**~~ ✅ **FIXED** — Proper MIGRATION_6_7 added creating 6 new tables. Destructive fallback retained only for pre-release versions.
3. ~~**Duplicate TTSManager**~~ ✅ **MITIGATED** — Legacy Java TTSManager marked `@Deprecated` with migration guidance. Both versions coexist safely; new code should use Kotlin `@Singleton` version via Hilt.

### Moderate
4. **Duplicate functionality across Java/Kotlin** — Gamification, analytics, AI service, quiz engine all have parallel implementations in both languages.
5. **Legacy Supabase client uses synchronous `Call<>`** — Not using suspend functions; runs blocking calls that could hang the UI thread if misused.
6. ~~**Multiple `language_hindi` / `language_english` definitions**~~ ✅ **Verified: No duplicates exist** — Each string appears once in default values; `values-hi/` has proper localized overrides.
7. **VideoPlayerFragment** — ExoPlayer integration is basic. Custom controls overlay exists but play/pause/seek wiring could be more robust.
8. ~~**Missing ProGuard rules for Gemini AI SDK**~~ ✅ **FIXED** — Enhanced rules for `generativeai.type`, `generativeai.internal`, and Kotlinx serialization.

### ~~Minor~~ (Resolved)
9. **Unused imports and variables** — Multiple warnings in ProfileFragment, QuizViewModel, AccessibilityViewHelper. (Non-breaking, cosmetic only.)
10. ~~**Deprecated `scaledDensity` usage**~~ ✅ **FIXED** — Replaced all 4 occurrences with `pxToSp()` helper using `TypedValue.applyDimension`.
11. **`formatted="false"` warnings** on string resources with multiple substitutions. (Non-breaking, cosmetic only.)

---

## 📋 Next Development Tasks

### High Priority
- [x] ~~Add `MainActivity` to AndroidManifest with proper launch configuration~~ ✅
- [x] ~~Implement proper Room database migrations (version 6 → 7)~~ ✅
- [x] ~~Consolidate duplicate TTSManager~~ ✅ (deprecated Java version, Kotlin version is canonical)
- [ ] Wire up video player ExoPlayer controls (seek bar sync, chapter progress saving)
- [ ] Test and fix onboarding → tutorial → mode selection → auth → home flow end-to-end

### Medium Priority
- [x] ~~Create FlashcardStudyFragment with swipe cards UI~~ ✅
- [ ] Create FlashcardCreateFragment for adding/editing flashcards
- [x] ~~Build Study Planner screens (summary, goal view, homework view)~~ ✅
- [ ] Build Homework Reminder creation screen
- [ ] Build Study Goal creation screen
- [ ] Add proper error states and retry mechanisms across all network operations
- [ ] Implement pull-to-refresh on book list, chapter list, quiz list
- [ ] Add chapter content caching for true offline reading

### Lower Priority
- [ ] Add parent dashboard deep link from settings
- [ ] Add share quiz results functionality
- [ ] Add notification channels for homework reminders, daily challenges, streak reminders
- [ ] Implement biometric lock for parent zone

---

## 🔮 Future Improvements

### Legacy Migration
- [ ] Migrate `SplashScreenActivity` → `SplashFragment` as app entry point
- [ ] Migrate `ClassSelectionActivity` → BookListFragment (already exists)
- [ ] Migrate `AudioPlayerActivity` → AudioPlayerFragment (already exists)
- [ ] Migrate `VideoPlayerActivity` → VideoPlayerFragment (already exists)
- [ ] Migrate `QuizActivity` → QuizActiveFragment (already exists)
- [ ] Migrate `GamificationActivity` → ProfileFragment + BadgesFragment (already exist)
- [ ] Migrate `SettingsActivity` → SettingsFragment (already exists)
- [ ] Remove all legacy Java Activities after full migration
- [ ] Remove legacy `EduAIService.java` after `GeminiAIServiceImpl.kt` handles all use cases
- [ ] Remove legacy `GamificationManager.java` after Kotlin gamification is complete

### Scalability
- [ ] Add pagination (Paging 3 library already in dependencies) for book/chapter/quiz lists
- [ ] Implement proper Room database migrations with Migration objects
- [ ] Add WorkManager constraints tuning for battery optimization
- [ ] Add image caching strategy (Coil already in dependencies)
- [ ] Consider adding offline queue for AI chat messages

### Performance Optimization
- [ ] Profile database query performance with 25+ tables
- [ ] Add database indices where missing (check query patterns)
- [ ] Optimize RecyclerView with DiffUtil (verify all adapters use it)
- [ ] Monitor memory with MemoryMonitor (already exists) — add alerts
- [ ] Lazy-load heavy fragments (video player, quiz active)
- [ ] Reduce ExoPlayer memory footprint on low-end devices
- [ ] Add R8 keep rules for reflection-heavy libraries (Gemini, Retrofit)

---

## ♿ Accessibility Improvement Tasks

- [ ] Audit all screens with TalkBack enabled — verify every element is reachable and described
- [ ] Add focus order (`accessibilityTraversalBefore/After`) on complex layouts
- [ ] Test voice control commands across all screens (Hindi and English)
- [ ] Add audio descriptions for all image-heavy content (book covers, badges)
- [ ] Improve ISL video overlay — add resize handle, auto-show for deaf mode users
- [ ] Add vibration pattern library for braille feedback — expand beyond basic CLICK/SUCCESS/ERROR
- [ ] Test with Switch Access for motor-impaired users
- [ ] Add live captions for AI chat TTS output
- [ ] Verify color contrast ratios meet WCAG 2.1 AA on all screens
- [ ] Add accessibility testing to CI pipeline (Espresso accessibility checks)

---

## 🎨 UI/UX Improvement Tasks

- [ ] Add skeleton loading states for all list screens (book list already has `item_book_skeleton`)
- [ ] Add empty state illustrations for empty lists (no books, no quizzes, no badges)
- [ ] Add pull-to-refresh animation on all data-driven screens
- [ ] Improve quiz timer UI — add color transitions as time runs out
- [ ] Add confetti/animation on quiz completion with high score
- [ ] Add streak fire animation on home screen
- [ ] Add dark mode support (Material 3 DayNight theme is set but not fully tested)
- [ ] Add tablet layout support with responsive grid layouts
- [ ] Improve onboarding with animated illustrations (currently static icons)
- [ ] Add loading shimmer effect on home dashboard cards
- [ ] Standardize card elevation and corner radius across all screens
