package com.hdaf.eduapp.domain.usecase.auth

import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.UserSession
import com.hdaf.eduapp.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user login with phone and OTP.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(params: Params): Resource<UserSession> {
        return authRepository.login(params.phone, params.otp)
    }

    data class Params(
        val phone: String,
        val otp: String
    )
}

/**
 * Use case for user registration.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(params: Params): Resource<UserSession> {
        return authRepository.register(
            name = params.name,
            phone = params.phone,
            classLevel = params.classLevel,
            medium = params.medium,
            accessibilityMode = params.accessibilityMode
        )
    }

    data class Params(
        val name: String,
        val phone: String,
        val classLevel: Int,
        val medium: String,
        val accessibilityMode: String
    )
}

/**
 * Use case for requesting OTP.
 */
class RequestOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Resource<Boolean> {
        return authRepository.requestOtp(phoneNumber)
    }
}

/**
 * Use case for logout.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return authRepository.logout()
    }
}

/**
 * Use case to check if user is logged in.
 */
class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.isLoggedIn()
}

/**
 * Use case to get current session.
 */
class GetCurrentSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getCurrentSession()
}
