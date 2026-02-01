package com.hdaf.eduapp.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hdaf.eduapp.R
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings screen for app preferences and accessibility options.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        observeUiState()

        viewModel.loadSettings()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Accessibility settings
            switchTalkback.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setTalkbackEnabled(isChecked)
            }

            switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setHighContrastEnabled(isChecked)
            }

            switchLargeText.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setLargeTextEnabled(isChecked)
            }

            switchHapticFeedback.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setHapticFeedbackEnabled(isChecked)
            }

            // Content settings
            layoutContentMode.setOnClickListener {
                showContentModeDialog()
            }

            layoutLanguage.setOnClickListener {
                showLanguageDialog()
            }

            layoutDownloadQuality.setOnClickListener {
                showDownloadQualityDialog()
            }

            switchAutoPlay.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAutoPlayEnabled(isChecked)
            }

            switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setOfflineModeEnabled(isChecked)
            }

            // Notifications
            switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setNotificationsEnabled(isChecked)
            }

            switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setDailyReminderEnabled(isChecked)
            }

            // Account
            btnChangeClass.setOnClickListener {
                showChangeClassDialog()
            }

            btnClearCache.setOnClickListener {
                showClearCacheDialog()
            }

            btnDeleteAccount.setOnClickListener {
                showDeleteAccountDialog()
            }

            btnLogout.setOnClickListener {
                showLogoutDialog()
            }

            // About
            layoutAbout.setOnClickListener {
                findNavController().navigate(R.id.aboutFragment)
            }

            layoutHelp.setOnClickListener {
                // TODO: Implement help screen
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }

            layoutPrivacy.setOnClickListener {
                // TODO: Implement privacy policy screen
                Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
            }
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

    private fun updateUi(state: SettingsUiState) {
        binding.apply {
            // Accessibility
            switchTalkback.isChecked = state.talkbackEnabled
            switchHighContrast.isChecked = state.highContrastEnabled
            switchLargeText.isChecked = state.largeTextEnabled
            switchHapticFeedback.isChecked = state.hapticFeedbackEnabled

            // Content
            tvContentModeValue.text = state.contentMode
            tvLanguageValue.text = state.language
            tvDownloadQualityValue.text = state.downloadQuality
            switchAutoPlay.isChecked = state.autoPlayEnabled
            switchOfflineMode.isChecked = state.offlineModeEnabled

            // Notifications
            switchNotifications.isChecked = state.notificationsEnabled
            switchDailyReminder.isChecked = state.dailyReminderEnabled
            switchDailyReminder.isEnabled = state.notificationsEnabled

            // App info
            tvAppVersion.text = state.appVersion
        }
    }

    private fun showContentModeDialog() {
        val modes = arrayOf(
            getString(R.string.mode_audio),
            getString(R.string.mode_video),
            getString(R.string.mode_both)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.content_mode)
            .setItems(modes) { _, which ->
                viewModel.setContentMode(modes[which])
            }
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("हिंदी", "English", "मराठी")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language)
            .setItems(languages) { _, which ->
                viewModel.setLanguage(languages[which])
            }
            .show()
    }

    private fun showDownloadQualityDialog() {
        val qualities = arrayOf(
            getString(R.string.quality_low),
            getString(R.string.quality_medium),
            getString(R.string.quality_high)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.download_quality)
            .setItems(qualities) { _, which ->
                viewModel.setDownloadQuality(qualities[which])
            }
            .show()
    }

    private fun showChangeClassDialog() {
        val classes = (1..10).map { "कक्षा $it" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.change_class)
            .setItems(classes) { _, which ->
                viewModel.changeClass(which + 1)
            }
            .show()
    }

    private fun showClearCacheDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_cache)
            .setMessage(R.string.clear_cache_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearCache()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout) { _, _ ->
                viewModel.logout()
                findNavController().navigate(R.id.phoneInputFragment)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
