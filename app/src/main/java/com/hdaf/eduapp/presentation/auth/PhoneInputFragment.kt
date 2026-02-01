package com.hdaf.eduapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentPhoneInputBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Phone input screen for authentication.
 * Collects user's phone number and sends OTP.
 */
@AndroidEntryPoint
class PhoneInputFragment : Fragment() {

    private var _binding: FragmentPhoneInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeUiState()
        observeEvents()
    }

    private fun setupViews() {
        binding.etPhone.addTextChangedListener { text ->
            viewModel.onPhoneChanged(text.toString())
            binding.tilPhone.error = null
        }

        binding.etPhone.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.requestOtp()
                true
            } else false
        }

        binding.btnContinue.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.requestOtp()
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
        binding.btnContinue.isEnabled = state.isPhoneValid && !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        state.phoneError?.let { error ->
            binding.tilPhone.error = error
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AuthUiEvent.OtpSent -> navigateToOtpVerification(viewModel.uiState.value.phoneNumber)
                        is AuthUiEvent.ShowError -> showError(event.message)
                        else -> { /* Handle other events */ }
                    }
                }
            }
        }
    }

    private fun navigateToOtpVerification(phone: String) {
        findNavController().navigate(
            PhoneInputFragmentDirections.actionPhoneInputToOtpVerification(phone)
        )
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        if (accessibilityManager.isAccessibilityEnabled()) {
            accessibilityManager.announceForAccessibility(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
