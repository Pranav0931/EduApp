package com.hdaf.eduapp.presentation.auth

import com.hdaf.eduapp.domain.model.AccessibilityMode
import com.hdaf.eduapp.presentation.base.UiEvent
import com.hdaf.eduapp.presentation.base.UiState

/**
 * UI State for Authentication screens.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val currentStep: AuthStep = AuthStep.PHONE_INPUT,
    
    // Phone Input
    val phoneNumber: String = "",
    val phoneError: String? = null,
    val isPhoneValid: Boolean = false,
    
    // OTP Verification
    val otp: String = "",
    val otpError: String? = null,
    val isOtpValid: Boolean = false,
    val otpResendEnabled: Boolean = false,
    val otpResendCountdown: Int = 60,
    
    // Registration
    val name: String = "",
    val nameError: String? = null,
    val selectedClass: Int = 1,
    val selectedMedium: String = "hindi",
    val selectedAccessibilityMode: AccessibilityMode = AccessibilityMode.VISUAL,
    
    // General
    val isNewUser: Boolean = false,
    val generalError: String? = null
) : UiState

enum class AuthStep {
    PHONE_INPUT,
    OTP_VERIFICATION,
    REGISTRATION,
    ACCESSIBILITY_SETUP
}

/**
 * Events emitted by AuthViewModel.
 */
sealed class AuthUiEvent : UiEvent {
    data object NavigateToHome : AuthUiEvent()
    data object NavigateToOnboarding : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
    data class ShowError(val message: String) : AuthUiEvent()
    data object OtpSent : AuthUiEvent()
    data object RegistrationComplete : AuthUiEvent()
    data class SpeakMessage(val message: String) : AuthUiEvent()
}

// Type alias for compatibility
typealias AuthEvent = AuthUiEvent

// Extension val for easier access in fragments
val AuthUiEvent.OtpSent: AuthUiEvent get() = AuthUiEvent.OtpSent
val AuthUiEvent.LoginSuccess: AuthUiEvent get() = AuthUiEvent.NavigateToHome
val AuthUiEvent.RegistrationRequired: AuthUiEvent get() = AuthUiEvent.RegistrationComplete

/**
 * Actions from Auth UI.
 */
sealed class AuthAction {
    // Phone Input
    data class UpdatePhone(val phone: String) : AuthAction()
    data object RequestOtp : AuthAction()
    
    // OTP Verification
    data class UpdateOtp(val otp: String) : AuthAction()
    data object VerifyOtp : AuthAction()
    data object ResendOtp : AuthAction()
    
    // Registration
    data class UpdateName(val name: String) : AuthAction()
    data class SelectClass(val classLevel: Int) : AuthAction()
    data class SelectMedium(val medium: String) : AuthAction()
    data class SelectAccessibilityMode(val mode: AccessibilityMode) : AuthAction()
    data object CompleteRegistration : AuthAction()
    
    // Navigation
    data object GoBack : AuthAction()
    data object DismissError : AuthAction()
}
