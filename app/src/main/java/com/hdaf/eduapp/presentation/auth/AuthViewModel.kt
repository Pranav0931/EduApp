package com.hdaf.eduapp.presentation.auth

import androidx.lifecycle.viewModelScope
import com.hdaf.eduapp.core.common.Resource
import com.hdaf.eduapp.domain.model.AccessibilityMode
import com.hdaf.eduapp.domain.usecase.auth.IsLoggedInUseCase
import com.hdaf.eduapp.domain.usecase.auth.LoginUseCase
import com.hdaf.eduapp.domain.usecase.auth.RegisterUseCase
import com.hdaf.eduapp.domain.usecase.auth.RequestOtpUseCase
import com.hdaf.eduapp.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Authentication flow.
 * Handles phone OTP login and registration.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : BaseViewModel<AuthUiState, AuthUiEvent>(AuthUiState()) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var otpCountdownJob: Job? = null

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val isLoggedIn = isLoggedInUseCase().first()
            if (isLoggedIn) {
                sendEvent(AuthUiEvent.NavigateToHome)
            }
        }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            // Phone Input
            is AuthAction.UpdatePhone -> updatePhone(action.phone)
            is AuthAction.RequestOtp -> requestOtpInternal()
            
            // OTP Verification
            is AuthAction.UpdateOtp -> updateOtp(action.otp)
            is AuthAction.VerifyOtp -> verifyOtpInternal()
            is AuthAction.ResendOtp -> resendOtp()
            
            // Registration
            is AuthAction.UpdateName -> updateName(action.name)
            is AuthAction.SelectClass -> selectClass(action.classLevel)
            is AuthAction.SelectMedium -> selectMedium(action.medium)
            is AuthAction.SelectAccessibilityMode -> selectAccessibilityMode(action.mode)
            is AuthAction.CompleteRegistration -> completeRegistration()
            
            // Navigation
            is AuthAction.GoBack -> goBack()
            is AuthAction.DismissError -> dismissError()
        }
    }

    // ==================== Phone Input ====================

    private fun updatePhone(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() }.take(10)
        val isValid = cleanPhone.length == 10 && cleanPhone.first() in '6'..'9'
        
        _uiState.update { 
            it.copy(
                phoneNumber = cleanPhone,
                isPhoneValid = isValid,
                phoneError = if (cleanPhone.isNotEmpty() && !isValid) "कृपया वैध 10 अंकों का मोबाइल नंबर दर्ज करें" else null
            )
        }
    }

    private fun requestOtpInternal() {
        val phone = _uiState.value.phoneNumber
        if (!_uiState.value.isPhoneValid) {
            _uiState.update { it.copy(phoneError = "कृपया वैध मोबाइल नंबर दर्ज करें") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneError = null) }
            
            when (val result = requestOtpUseCase(phone)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentStep = AuthStep.OTP_VERIFICATION,
                            otpResendEnabled = false,
                            otpResendCountdown = 60
                        )
                    }
                    startOtpCountdown()
                    sendEvent(AuthUiEvent.OtpSent)
                    sendEvent(AuthUiEvent.SpeakMessage("OTP भेजा गया है। कृपया OTP दर्ज करें।"))
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            phoneError = result.message ?: "OTP भेजने में विफल"
                        )
                    }
                    sendEvent(AuthUiEvent.ShowError(result.message ?: "OTP भेजने में विफल"))
                }
                is Resource.Loading -> {
                    // Already set loading
                }
            }
        }
    }

    // ==================== OTP Verification ====================

    private fun updateOtp(otp: String) {
        val cleanOtp = otp.filter { it.isDigit() }.take(6)
        _uiState.update { 
            it.copy(
                otp = cleanOtp,
                isOtpValid = cleanOtp.length == 6,
                otpError = null
            )
        }
        
        // Auto-verify when 6 digits entered
        if (cleanOtp.length == 6) {
            verifyOtpInternal()
        }
    }

    private fun verifyOtpInternal() {
        val phone = _uiState.value.phoneNumber
        val otp = _uiState.value.otp
        
        if (!_uiState.value.isOtpValid) {
            _uiState.update { it.copy(otpError = "कृपया 6 अंकों का OTP दर्ज करें") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }
            
            val loginParams = LoginUseCase.Params(phone = phone, otp = otp)
            when (val result = loginUseCase(loginParams)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(AuthUiEvent.NavigateToHome)
                    sendEvent(AuthUiEvent.SpeakMessage("लॉगिन सफल। स्वागत है!"))
                }
                is Resource.Error -> {
                    // Check if user needs to register
                    if (result.message?.contains("not found", ignoreCase = true) == true ||
                        result.message?.contains("register", ignoreCase = true) == true) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isNewUser = true,
                                currentStep = AuthStep.REGISTRATION
                            )
                        }
                        sendEvent(AuthUiEvent.SpeakMessage("नया खाता बनाने के लिए जानकारी दर्ज करें।"))
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                otpError = "गलत OTP। कृपया पुनः प्रयास करें।"
                            )
                        }
                        sendEvent(AuthUiEvent.ShowError("गलत OTP"))
                    }
                }
                is Resource.Loading -> { }
            }
        }
    }

    private fun resendOtp() {
        if (!_uiState.value.otpResendEnabled) return
        
        _uiState.update { it.copy(otp = "", otpError = null) }
        requestOtpInternal()
    }

    private fun startOtpCountdown() {
        otpCountdownJob?.cancel()
        otpCountdownJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _uiState.update { 
                    it.copy(
                        otpResendCountdown = i,
                        otpResendEnabled = i == 0
                    )
                }
                delay(1000)
            }
        }
    }

    // ==================== Registration ====================

    private fun updateName(name: String) {
        val isValid = name.trim().length >= 2
        _uiState.update { 
            it.copy(
                name = name,
                nameError = if (name.isNotEmpty() && !isValid) "नाम कम से कम 2 अक्षरों का होना चाहिए" else null
            )
        }
    }

    private fun selectClass(classLevel: Int) {
        _uiState.update { it.copy(selectedClass = classLevel) }
        sendEvent(AuthUiEvent.SpeakMessage("कक्षा $classLevel चुनी गई"))
    }

    private fun selectMedium(medium: String) {
        _uiState.update { it.copy(selectedMedium = medium) }
        val mediumName = when(medium) {
            "hindi" -> "हिंदी"
            "english" -> "अंग्रेजी"
            "marathi" -> "मराठी"
            else -> medium
        }
        sendEvent(AuthUiEvent.SpeakMessage("$mediumName माध्यम चुना गया"))
    }

    private fun selectAccessibilityMode(mode: AccessibilityMode) {
        _uiState.update { 
            it.copy(
                selectedAccessibilityMode = mode,
                currentStep = AuthStep.ACCESSIBILITY_SETUP
            )
        }
        
        val modeName = when(mode) {
            AccessibilityMode.BLIND -> "दृष्टिहीन मोड"
            AccessibilityMode.DEAF -> "बधिर मोड"
            AccessibilityMode.LOW_VISION -> "कम दृष्टि मोड"
            AccessibilityMode.COGNITIVE -> "सरल मोड"
            AccessibilityMode.VISUAL -> "सामान्य मोड"
        }
        sendEvent(AuthUiEvent.SpeakMessage("$modeName चुना गया"))
    }

    private fun completeRegistration() {
        val state = _uiState.value
        
        if (state.name.trim().length < 2) {
            _uiState.update { it.copy(nameError = "कृपया अपना नाम दर्ज करें") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            
            val params = RegisterUseCase.Params(
                name = state.name.trim(),
                phone = state.phoneNumber,
                classLevel = state.selectedClass,
                medium = state.selectedMedium,
                accessibilityMode = state.selectedAccessibilityMode.name
            )
            
            when (val result = registerUseCase(params)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(AuthUiEvent.RegistrationComplete)
                    sendEvent(AuthUiEvent.SpeakMessage("खाता बन गया। स्वागत है, ${state.name}!"))
                    sendEvent(AuthUiEvent.NavigateToOnboarding)
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            generalError = result.message ?: "पंजीकरण विफल"
                        )
                    }
                    sendEvent(AuthUiEvent.ShowError(result.message ?: "पंजीकरण विफल"))
                }
                is Resource.Loading -> { }
            }
        }
    }

    // ==================== Navigation ====================

    private fun goBack() {
        val currentStep = _uiState.value.currentStep
        when (currentStep) {
            AuthStep.OTP_VERIFICATION -> {
                otpCountdownJob?.cancel()
                _uiState.update { 
                    it.copy(
                        currentStep = AuthStep.PHONE_INPUT,
                        otp = "",
                        otpError = null
                    )
                }
            }
            AuthStep.REGISTRATION -> {
                _uiState.update { 
                    it.copy(
                        currentStep = AuthStep.OTP_VERIFICATION,
                        name = "",
                        nameError = null
                    )
                }
            }
            AuthStep.ACCESSIBILITY_SETUP -> {
                _uiState.update { it.copy(currentStep = AuthStep.REGISTRATION) }
            }
            AuthStep.PHONE_INPUT -> {
                // Can't go back from first step
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(generalError = null, phoneError = null, otpError = null, nameError = null) }
    }

    // ==================== Convenience Methods for Fragments ====================

    /**
     * Called when phone number text changes.
     */
    fun onPhoneChanged(phone: String) {
        onAction(AuthAction.UpdatePhone(phone))
    }

    /**
     * Request OTP - public method for fragments.
     */
    fun requestOtp() {
        val phone = _uiState.value.phoneNumber
        if (!_uiState.value.isPhoneValid) {
            _uiState.update { it.copy(phoneError = "कृपया वैध मोबाइल नंबर दर्ज करें") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneError = null) }
            
            when (val result = requestOtpUseCase(phone)) {
                is Resource.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentStep = AuthStep.OTP_VERIFICATION,
                            otpResendEnabled = false,
                            otpResendCountdown = 60
                        )
                    }
                    startOtpCountdown()
                    sendEvent(AuthUiEvent.OtpSent)
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            phoneError = result.message ?: "OTP भेजने में विफल"
                        )
                    }
                    sendEvent(AuthUiEvent.ShowError(result.message ?: "OTP भेजने में विफल"))
                }
                is Resource.Loading -> { }
            }
        }
    }

    /**
     * Called when OTP text changes.
     */
    fun onOtpChanged(otp: String) {
        onAction(AuthAction.UpdateOtp(otp))
    }

    /**
     * Verify OTP - public method for fragments.
     */
    fun verifyOtp(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
        verifyOtpInternal()
    }

    override fun onCleared() {
        super.onCleared()
        otpCountdownJob?.cancel()
    }
}
