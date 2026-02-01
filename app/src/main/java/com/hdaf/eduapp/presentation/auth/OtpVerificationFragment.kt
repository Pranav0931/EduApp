package com.hdaf.eduapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentOtpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OTP verification screen.
 * Verifies the OTP sent to user's phone.
 */
@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private val args: OtpVerificationFragmentArgs by navArgs()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    private var resendTimerJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeUiState()
        observeEvents()
        startResendTimer()
    }

    private fun setupViews() {
        binding.tvPhoneNumber.text = "+91 ${args.phone}"

        // OTP input handling
        binding.etOtp.addTextChangedListener { text ->
            viewModel.onOtpChanged(text.toString())
            binding.tilOtp.error = null
            
            // Auto-submit when OTP is complete
            if (text?.length == 6) {
                viewModel.verifyOtp(args.phone)
            }
        }

        binding.btnVerify.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.verifyOtp(args.phone)
        }

        binding.btnResend.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.requestOtp()
            startResendTimer()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AuthUiState) {
        binding.btnVerify.isEnabled = state.isOtpValid && !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        state.otpError?.let { error ->
            binding.tilOtp.error = error
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AuthUiEvent.NavigateToHome -> navigateToHome()
                        is AuthUiEvent.RegistrationComplete -> navigateToRegistration()
                        is AuthUiEvent.ShowError -> showError(event.message)
                        else -> { /* Handle other events */ }
                    }
                }
            }
        }
    }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        binding.btnResend.isEnabled = false

        resendTimerJob = viewLifecycleOwner.lifecycleScope.launch {
            for (seconds in 30 downTo 0) {
                binding.btnResend.text = if (seconds > 0) {
                    "पुनः भेजें ($seconds s)"
                } else {
                    "पुनः भेजें"
                }
                delay(1000)
            }
            binding.btnResend.isEnabled = true
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            OtpVerificationFragmentDirections.actionOtpVerificationToHome()
        )
    }

    private fun navigateToRegistration() {
        findNavController().navigate(
            OtpVerificationFragmentDirections.actionOtpVerificationToRegistration(args.phone)
        )
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        if (accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.announceForAccessibility(message)
        }
    }

    override fun onDestroyView() {
        resendTimerJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}
