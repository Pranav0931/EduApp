package com.hdaf.eduapp.data.mapper

import com.hdaf.eduapp.data.remote.dto.AuthResponse
import com.hdaf.eduapp.data.remote.dto.UserDto
import com.hdaf.eduapp.domain.model.AccessibilityMode
import com.hdaf.eduapp.domain.model.User
import com.hdaf.eduapp.domain.model.UserRole
import com.hdaf.eduapp.domain.model.UserSession

/**
 * Mappers for User and Auth conversions between layers.
 */

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        classLevel = classLevel ?: 1,
        medium = "hindi", // Default since not in DTO
        role = UserRole.fromString(role),
        accessibilityMode = AccessibilityMode.VISUAL, // Default since not in DTO
        avatarUrl = avatarUrl,
        parentId = parentId,
        isEmailVerified = email != null,
        isPhoneVerified = phone != null,
        createdAt = createdAt?.let { parseTimestamp(it) } ?: System.currentTimeMillis()
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        phone = phone,
        classLevel = classLevel,
        role = role.name.lowercase(),
        avatarUrl = avatarUrl,
        parentId = parentId,
        isActive = true,
        createdAt = null,
        updatedAt = null,
        childrenIds = null
    )
}

fun AuthResponse.toSession(): UserSession {
    return UserSession(
        user = user.toDomain(),
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = System.currentTimeMillis() + (expiresIn * 1000)
    )
}

private fun parseTimestamp(timestamp: String): Long {
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            .parse(timestamp)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
