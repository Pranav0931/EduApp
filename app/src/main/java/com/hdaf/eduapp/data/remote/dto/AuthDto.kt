package com.hdaf.eduapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs for Authentication API.
 */

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("role") val role: String = "student", // student, parent, admin
    @SerializedName("class_level") val classLevel: Int? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class OtpRequest(
    @SerializedName("phone") val phone: String
)

data class OtpVerifyRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("otp") val otp: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("token_type") val tokenType: String = "Bearer",
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("name") val name: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("role") val role: String,
    @SerializedName("class_level") val classLevel: Int?,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("parent_id") val parentId: String? = null, // For child accounts
    @SerializedName("children_ids") val childrenIds: List<String>? = null // For parent accounts
)
