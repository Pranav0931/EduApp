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
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.core.accessibility.HapticType
import com.hdaf.eduapp.databinding.FragmentRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Registration screen for new users.
 * Collects name, class, and medium preferences.
 */
@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private val args: RegistrationFragmentArgs by navArgs()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeUiState()
        observeEvents()
    }

    private fun setupViews() {
        // Name input
        binding.etName.addTextChangedListener { text ->
            viewModel.onAction(AuthAction.UpdateName(text.toString()))
            binding.tilName.error = null
        }

        // Class selection chips
        setupClassChips()

        // Medium selection chips
        binding.chipGroupMedium.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = binding.chipGroupMedium.findViewById<Chip>(checkedIds.first())
                val medium = when (chip?.id) {
                    R.id.chip_hindi -> "hindi"
                    R.id.chip_english -> "english"
                    R.id.chip_marathi -> "marathi"
                    else -> "hindi"
                }
                viewModel.onAction(AuthAction.SelectMedium(medium))
            }
        }

        // Register button
        binding.btnRegister.setOnClickListener {
            accessibilityManager.provideHapticFeedback(HapticType.CLICK)
            viewModel.onAction(AuthAction.CompleteRegistration)
        }

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupClassChips() {
        binding.chipGroupClass.removeAllViews()
        for (i in 1..10) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = getString(R.string.class_button_description, i.toString())
                tag = i
                isCheckable = true
                isCheckedIconVisible = true
            }
            chip.setOnClickListener {
                viewModel.onAction(AuthAction.SelectClass(i))
            }
            binding.chipGroupClass.addView(chip)
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
        binding.btnRegister.isEnabled = state.name.isNotBlank() && !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        state.nameError?.let { error ->
            binding.tilName.error = error
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AuthUiEvent.NavigateToHome -> navigateToHome()
                        is AuthUiEvent.RegistrationComplete -> navigateToHome()
                        is AuthUiEvent.ShowError -> showError(event.message)
                        else -> { /* Handle other events */ }
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            RegistrationFragmentDirections.actionRegistrationToHome()
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
