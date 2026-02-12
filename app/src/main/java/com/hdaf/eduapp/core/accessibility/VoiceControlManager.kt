package com.hdaf.eduapp.core.accessibility

import android.content.Context
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Voice Control System for Blind Users.
 * 
 * Enables hands-free navigation and interaction:
 * - Voice commands for navigation
 * - Voice-based quiz answering
 * - Voice search
 * - Voice notes/bookmarks
 * - Continuous listening mode for accessibility
 */
@Singleton
class VoiceControlManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _lastCommand = MutableStateFlow<VoiceCommand?>(null)
    val lastCommand: StateFlow<VoiceCommand?> = _lastCommand.asStateFlow()
    
    private val _preferredLanguage = MutableStateFlow(VoiceLanguage.HINDI)
    val preferredLanguage: StateFlow<VoiceLanguage> = _preferredLanguage.asStateFlow()
    
    private var commandListener: ((VoiceCommand) -> Unit)? = null
    
    // ========== Configuration ==========
    
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
    }
    
    fun setLanguage(language: VoiceLanguage) {
        _preferredLanguage.value = language
    }
    
    fun setCommandListener(listener: (VoiceCommand) -> Unit) {
        commandListener = listener
    }
    
    // ========== Speech Recognition ==========
    
    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Process recognized text and extract command.
     */
    fun processRecognizedText(text: String): VoiceCommand? {
        if (text.isBlank()) return null
        
        val normalizedText = text.trim().lowercase()
        
        // Try to match known commands
        val command = matchCommand(normalizedText)
        
        if (command != null) {
            _lastCommand.value = command
            commandListener?.invoke(command)
        }
        
        return command
    }
    
    private fun matchCommand(text: String): VoiceCommand? {
        // Navigation commands
        navigationCommands.forEach { (patterns, command) ->
            if (patterns.any { text.contains(it) }) {
                return command
            }
        }
        
        // Playback commands
        playbackCommands.forEach { (patterns, command) ->
            if (patterns.any { text.contains(it) }) {
                return command
            }
        }
        
        // Quiz commands
        quizCommands.forEach { (patterns, command) ->
            if (patterns.any { text.contains(it) }) {
                return command
            }
        }
        
        // Study commands
        studyCommands.forEach { (patterns, command) ->
            if (patterns.any { text.contains(it) }) {
                return command
            }
        }
        
        // System commands
        systemCommands.forEach { (patterns, command) ->
            if (patterns.any { text.contains(it) }) {
                return command
            }
        }
        
        // No match - return as free text
        return VoiceCommand.FreeText(text)
    }
    
    // ========== Command Definitions ==========
    
    private val navigationCommands = listOf(
        // Hindi + English patterns
        listOf("आगे जाएं", "अगला", "next", "go next", "forward") to 
            VoiceCommand.Navigate(NavigationTarget.NEXT),
        
        listOf("पीछे जाएं", "पिछला", "previous", "go back", "back") to 
            VoiceCommand.Navigate(NavigationTarget.PREVIOUS),
        
        listOf("होम", "घर", "home", "go home", "main menu") to 
            VoiceCommand.Navigate(NavigationTarget.HOME),
        
        listOf("विषय", "subject", "subjects", "विषय चुनें") to 
            VoiceCommand.Navigate(NavigationTarget.SUBJECTS),
        
        listOf("प्रोफाइल", "profile", "my profile", "मेरा प्रोफाइल") to 
            VoiceCommand.Navigate(NavigationTarget.PROFILE),
        
        listOf("सेटिंग्स", "settings", "सेटिंग") to 
            VoiceCommand.Navigate(NavigationTarget.SETTINGS),
        
        listOf("क्विज़", "quiz", "test", "परीक्षा") to 
            VoiceCommand.Navigate(NavigationTarget.QUIZ),
        
        listOf("अध्याय", "chapter", "chapters", "पाठ") to 
            VoiceCommand.Navigate(NavigationTarget.CHAPTERS)
    )
    
    private val playbackCommands = listOf(
        listOf("चलाएं", "play", "start", "शुरू करें") to 
            VoiceCommand.Playback(PlaybackAction.PLAY),
        
        listOf("रोकें", "pause", "stop", "रुकें") to 
            VoiceCommand.Playback(PlaybackAction.PAUSE),
        
        listOf("आगे बढ़ाएं", "forward", "skip", "10 सेकंड आगे") to 
            VoiceCommand.Playback(PlaybackAction.FORWARD_10),
        
        listOf("पीछे करें", "rewind", "10 सेकंड पीछे") to 
            VoiceCommand.Playback(PlaybackAction.REWIND_10),
        
        listOf("तेज़", "faster", "speed up", "गति बढ़ाएं") to 
            VoiceCommand.Playback(PlaybackAction.SPEED_UP),
        
        listOf("धीमा", "slower", "slow down", "गति कम करें") to 
            VoiceCommand.Playback(PlaybackAction.SLOW_DOWN),
        
        listOf("दोहराएं", "repeat", "फिर से") to 
            VoiceCommand.Playback(PlaybackAction.REPEAT),
        
        listOf("अगला अध्याय", "next chapter") to 
            VoiceCommand.Playback(PlaybackAction.NEXT_CHAPTER),
        
        listOf("पिछला अध्याय", "previous chapter") to 
            VoiceCommand.Playback(PlaybackAction.PREVIOUS_CHAPTER)
    )
    
    private val quizCommands = listOf(
        listOf("विकल्प ए", "option a", "a", "पहला विकल्प", "first option") to 
            VoiceCommand.QuizAnswer(1),
        
        listOf("विकल्प बी", "option b", "b", "दूसरा विकल्प", "second option") to 
            VoiceCommand.QuizAnswer(2),
        
        listOf("विकल्प सी", "option c", "c", "तीसरा विकल्प", "third option") to 
            VoiceCommand.QuizAnswer(3),
        
        listOf("विकल्प डी", "option d", "d", "चौथा विकल्प", "fourth option") to 
            VoiceCommand.QuizAnswer(4),
        
        listOf("प्रश्न दोहराएं", "repeat question", "फिर से सुनाएं") to 
            VoiceCommand.QuizAction(QuizVoiceAction.REPEAT_QUESTION),
        
        listOf("विकल्प सुनाएं", "read options", "options") to 
            VoiceCommand.QuizAction(QuizVoiceAction.READ_OPTIONS),
        
        listOf("छोड़ें", "skip", "अगला प्रश्न") to 
            VoiceCommand.QuizAction(QuizVoiceAction.SKIP),
        
        listOf("सबमिट", "submit", "जमा करें") to 
            VoiceCommand.QuizAction(QuizVoiceAction.SUBMIT),
        
        listOf("क्विज़ समाप्त", "end quiz", "finish") to 
            VoiceCommand.QuizAction(QuizVoiceAction.END_QUIZ)
    )
    
    private val studyCommands = listOf(
        listOf("बुकमार्क", "bookmark", "save", "याद रखें") to 
            VoiceCommand.Study(StudyAction.BOOKMARK),
        
        listOf("नोट", "note", "add note", "नोट जोड़ें") to 
            VoiceCommand.Study(StudyAction.ADD_NOTE),
        
        listOf("हाइलाइट", "highlight", "mark", "चिह्नित करें") to 
            VoiceCommand.Study(StudyAction.HIGHLIGHT),
        
        listOf("डाउनलोड", "download", "offline", "save offline") to 
            VoiceCommand.Study(StudyAction.DOWNLOAD),
        
        listOf("शेयर", "share", "साझा करें") to 
            VoiceCommand.Study(StudyAction.SHARE)
    )
    
    private val systemCommands = listOf(
        listOf("मदद", "help", "सहायता") to 
            VoiceCommand.System(SystemAction.HELP),
        
        listOf("बंद करें", "exit", "close", "वापस जाएं") to 
            VoiceCommand.System(SystemAction.EXIT),
        
        listOf("खोजें", "search", "find", "ढूंढें") to 
            VoiceCommand.System(SystemAction.SEARCH),
        
        listOf("आवाज़ बंद", "mute", "शांत") to 
            VoiceCommand.System(SystemAction.MUTE),
        
        listOf("आवाज़ चालू", "unmute", "आवाज़") to 
            VoiceCommand.System(SystemAction.UNMUTE),
        
        listOf("कमांड सुनाएं", "what can i say", "available commands") to 
            VoiceCommand.System(SystemAction.LIST_COMMANDS)
    )
    
    // ========== Help Text Generation ==========
    
    /**
     * Get list of available voice commands for current screen.
     */
    fun getAvailableCommands(screenType: VoiceScreenType): List<CommandHelp> {
        val baseCommands = listOf(
            CommandHelp("होम / Home", "Go to home screen"),
            CommandHelp("मदद / Help", "Get help with commands"),
            CommandHelp("बंद करें / Close", "Close current screen")
        )
        
        return when (screenType) {
            VoiceScreenType.HOME -> baseCommands + listOf(
                CommandHelp("विषय / Subjects", "View all subjects"),
                CommandHelp("क्विज़ / Quiz", "Take a quiz"),
                CommandHelp("प्रोफाइल / Profile", "View profile")
            )
            
            VoiceScreenType.AUDIO_PLAYER -> baseCommands + listOf(
                CommandHelp("चलाएं / Play", "Start playback"),
                CommandHelp("रोकें / Pause", "Pause playback"),
                CommandHelp("आगे बढ़ाएं / Forward", "Skip 10 seconds"),
                CommandHelp("पीछे करें / Rewind", "Go back 10 seconds"),
                CommandHelp("तेज़ / Faster", "Increase speed"),
                CommandHelp("धीमा / Slower", "Decrease speed"),
                CommandHelp("बुकमार्क / Bookmark", "Save position")
            )
            
            VoiceScreenType.QUIZ -> baseCommands + listOf(
                CommandHelp("विकल्प ए/बी/सी/डी", "Select option A/B/C/D"),
                CommandHelp("प्रश्न दोहराएं / Repeat", "Hear question again"),
                CommandHelp("विकल्प सुनाएं", "Hear all options"),
                CommandHelp("छोड़ें / Skip", "Skip this question"),
                CommandHelp("सबमिट / Submit", "Submit answer")
            )
            
            VoiceScreenType.READING -> baseCommands + listOf(
                CommandHelp("आगे जाएं / Next", "Next paragraph"),
                CommandHelp("पीछे जाएं / Previous", "Previous paragraph"),
                CommandHelp("दोहराएं / Repeat", "Read again"),
                CommandHelp("बुकमार्क / Bookmark", "Save bookmark"),
                CommandHelp("नोट / Note", "Add a note")
            )
            
            VoiceScreenType.CHAPTER_LIST -> baseCommands + listOf(
                CommandHelp("अध्याय [नंबर]", "Open chapter by number"),
                CommandHelp("अगला / Next", "Next chapter"),
                CommandHelp("पिछला / Previous", "Previous chapter")
            )
        }
    }
    
    /**
     * Get voice commands help announcement.
     */
    fun getCommandsAnnouncement(
        screenType: VoiceScreenType,
        isHindi: Boolean
    ): String {
        val commands = getAvailableCommands(screenType)
        
        return buildString {
            append(if (isHindi) "उपलब्ध वॉइस कमांड्स: " else "Available voice commands: ")
            commands.take(5).forEachIndexed { index, cmd ->
                append(if (isHindi) cmd.hindiCommand else cmd.englishDescription)
                if (index < 4) append(". ")
            }
        }
    }
}

/**
 * Voice command types.
 */
sealed class VoiceCommand {
    data class Navigate(val target: NavigationTarget) : VoiceCommand()
    data class Playback(val action: PlaybackAction) : VoiceCommand()
    data class QuizAnswer(val optionNumber: Int) : VoiceCommand()
    data class QuizAction(val action: QuizVoiceAction) : VoiceCommand()
    data class Study(val action: StudyAction) : VoiceCommand()
    data class System(val action: SystemAction) : VoiceCommand()
    data class FreeText(val text: String) : VoiceCommand()
}

/**
 * Navigation targets.
 */
enum class NavigationTarget {
    HOME, NEXT, PREVIOUS, SUBJECTS, CHAPTERS, QUIZ, PROFILE, SETTINGS
}

/**
 * Playback actions.
 */
enum class PlaybackAction {
    PLAY, PAUSE, FORWARD_10, REWIND_10, SPEED_UP, SLOW_DOWN, 
    REPEAT, NEXT_CHAPTER, PREVIOUS_CHAPTER
}

/**
 * Quiz voice actions.
 */
enum class QuizVoiceAction {
    REPEAT_QUESTION, READ_OPTIONS, SKIP, SUBMIT, END_QUIZ
}

/**
 * Study actions.
 */
enum class StudyAction {
    BOOKMARK, ADD_NOTE, HIGHLIGHT, DOWNLOAD, SHARE
}

/**
 * System actions.
 */
enum class SystemAction {
    HELP, EXIT, SEARCH, MUTE, UNMUTE, LIST_COMMANDS
}

/**
 * Voice screen types.
 */
enum class VoiceScreenType {
    HOME, AUDIO_PLAYER, QUIZ, READING, CHAPTER_LIST
}

/**
 * Voice language options.
 */
enum class VoiceLanguage(val code: String, val displayName: String) {
    HINDI("hi-IN", "हिंदी"),
    ENGLISH("en-IN", "English (India)"),
    HINGLISH("hi-EN", "Hinglish")
}

/**
 * Command help information.
 */
data class CommandHelp(
    val hindiCommand: String,
    val englishDescription: String
)
