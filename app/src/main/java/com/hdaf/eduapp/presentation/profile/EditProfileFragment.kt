package com.hdaf.eduapp.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Edit profile fragment for updating user information.
 */
@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDropdowns()
        setupSaveButton()
        observeUiState()

        viewModel.loadProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDropdowns() {
        // Class dropdown
        val classes = (1..10).map { getString(R.string.std_format, it.toString()) }
        val classAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, classes)
        binding.dropdownClass.setAdapter(classAdapter)

        // Medium dropdown
        val mediums = listOf("Hindi", "English", "Marathi", "Gujarati")
        val mediumAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mediums)
        binding.dropdownMedium.setAdapter(mediumAdapter)
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilName.error = "Name is required"
                return@setOnClickListener
            }
            binding.tilName.error = null
            
            // For now, just show success and navigate back
            // TODO: Implement profile update in ProfileViewModel
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, 
                "Profile saved", 
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    state.user?.let { profile ->
                        binding.etName.setText(profile.name)
                        binding.dropdownClass.setText(getString(R.string.std_format, profile.classLevel.toString()), false)
                        binding.dropdownMedium.setText(profile.medium, false)
                    }

                    state.error?.let { error ->
                        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
