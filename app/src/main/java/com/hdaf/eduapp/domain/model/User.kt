package com.hdaf.eduapp.domain.model

/**
 * Domain model for User.
 * This is the core business model used throughout the app.
 */
data class User(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val classLevel: Int,
    val medium: String, // "hindi", "english", "marathi"
    val role: UserRole = UserRole.STUDENT,
    val accessibilityMode: AccessibilityMode = AccessibilityMode.VISUAL,
    val avatarUrl: String? = null,
    val parentId: String? = null,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    STUDENT,
    PARENT,
    TEACHER,
    ADMIN;
    
    companion object {
        fun fromString(value: String): UserRole {
            return when (value.lowercase()) {
                "student" -> STUDENT
                "parent" -> PARENT
                "teacher" -> TEACHER
                "admin" -> ADMIN
                else -> STUDENT
            }
        }
    }
}

enum class AccessibilityMode {
    VISUAL,      // Default mode with full visuals
    BLIND,       // TTS-focused mode for blind users
    DEAF,        // Sign language and visual mode for deaf users
    LOW_VISION,  // High contrast, large text mode
    COGNITIVE;   // Simplified navigation mode
    
    companion object {
        fun fromString(value: String): AccessibilityMode {
            return when (value.lowercase()) {
                "visual" -> VISUAL
                "blind" -> BLIND
                "deaf" -> DEAF
                "low_vision", "lowvision" -> LOW_VISION
                "cognitive" -> COGNITIVE
                else -> VISUAL
            }
        }
    }
}

data class UserSession(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt
    
    val isExpiringSoon: Boolean
        get() = System.currentTimeMillis() >= (expiresAt - EXPIRY_THRESHOLD)
    
    companion object {
        private const val EXPIRY_THRESHOLD = 5 * 60 * 1000L // 5 minutes
    }
}
