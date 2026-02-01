package com.hdaf.eduapp.presentation.home

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hdaf.eduapp.R
import com.hdaf.eduapp.accessibility.OCREngine
import com.hdaf.eduapp.core.accessibility.EduAccessibilityManager
import com.hdaf.eduapp.databinding.FragmentHomeBinding
import com.hdaf.eduapp.domain.model.AccessibilityModeType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Home Fragment - Main dashboard of the app.
 * 
 * Features:
 * - User greeting with progress
 * - Daily goal progress
 * - Continue learning section
 * - Recommended content
 * - Quick access to quizzes
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    
    @Inject
    lateinit var accessibilityManager: EduAccessibilityManager
    
    @Inject
    lateinit var ocrEngine: OCREngine
    
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    private var currentPhotoUri: Uri? = null
    
    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Camera result launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                processOcrImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupObservers()
        setupClickListeners()
        setupAccessibilityFeatures()
    }
    
    private fun setupAccessibilityFeatures() {
        // Show OCR button based on accessibility mode
        val modeOrdinal = sharedPreferences.getInt("accessibility_mode", 0)
        val mode = AccessibilityModeType.entries.getOrElse(modeOrdinal) { AccessibilityModeType.NORMAL }
        
        binding.btnOcrScan.visibility = when (mode) {
            AccessibilityModeType.BLIND, AccessibilityModeType.LOW_VISION -> View.VISIBLE
            else -> View.GONE
        }
        
        binding.btnOcrScan.setOnClickListener {
            checkCameraPermissionAndScan()
        }
    }
    
    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun launchCamera() {
        val photoFile = File.createTempFile(
            "ocr_scan_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().cacheDir
        )
        currentPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        currentPhotoUri?.let { uri ->
            cameraLauncher.launch(uri)
            accessibilityManager.announceForAccessibility(getString(R.string.ocr_scanning))
        }
    }
    
    private fun processOcrImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), R.string.ocr_scanning, Toast.LENGTH_SHORT).show()
                
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                
                if (bytes != null) {
                    val result = ocrEngine.processImage(bytes)
                    
                    when (result) {
                        is com.hdaf.eduapp.core.common.Resource.Success -> {
                            val ocrResult = result.data
                            if (ocrResult.text.isNotBlank()) {
                                showOcrResultDialog(ocrResult.text)
                                accessibilityManager.speak(ocrResult.text)
                            } else {
                                Toast.makeText(requireContext(), R.string.ocr_no_text_found, Toast.LENGTH_SHORT).show()
                                accessibilityManager.announceForAccessibility(getString(R.string.ocr_no_text_found))
                            }
                        }
                        is com.hdaf.eduapp.core.common.Resource.Error -> {
                            Toast.makeText(requireContext(), "OCR Error: ${result.message}", Toast.LENGTH_SHORT).show()
                        }
                        is com.hdaf.eduapp.core.common.Resource.Loading -> {
                            // Still loading
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "OCR Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showOcrResultDialog(text: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ocr_scan_complete)
            .setMessage(text)
            .setPositiveButton(R.string.btn_play) { _, _ ->
                accessibilityManager.speak(text)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupViews() {
        // Setup RecyclerViews
        binding.rvRecommended.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }
        
        binding.rvRecent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeUiState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeUiState() {
        viewModel.uiState.collect { state ->
            // Update user greeting
            binding.tvGreeting.text = getString(R.string.home_greeting, state.userName)
            
            // Update progress
            binding.tvLevel.text = getString(R.string.home_level) + ": ${state.level}"
            binding.tvXp.text = "${state.totalXp} ${getString(R.string.home_xp)}"
            binding.tvStreak.text = "${state.streak} ${getString(R.string.home_streak)} 🔥"
            
            // Update daily goal progress
            binding.progressDailyGoal.progress = (state.dailyGoalProgress * 100).toInt()
            
            // Handle loading state
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            // Handle error state
            state.error?.let { error ->
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                viewModel.onAction(HomeAction.ClearError)
            }
            
            // Accessibility announcement
            if (!state.isLoading && state.error == null) {
                accessibilityManager.announceForAccessibility(
                    "होम स्क्रीन। स्तर ${state.level}, ${state.totalXp} XP, ${state.streak} दिन की स्ट्रीक"
                )
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collect { event ->
            when (event) {
                is HomeUiEvent.NavigateToBook -> {
                    val action = HomeFragmentDirections.actionHomeToChapterList(
                        bookId = event.bookId,
                        bookTitle = event.bookTitle
                    )
                    findNavController().navigate(action)
                }
                is HomeUiEvent.NavigateToQuiz -> {
                    val bundle = Bundle().apply {
                        putString("quizId", event.quizId)
                    }
                    findNavController().navigate(R.id.action_home_to_quizList, bundle)
                }
                is HomeUiEvent.ShowBadgeEarned -> {
                    Snackbar.make(
                        binding.root,
                        "🏅 ${event.badge.name}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    accessibilityManager.speak("बधाई! आपने ${event.badge.name} बैज अर्जित किया!")
                }
                is HomeUiEvent.ShowLevelUp -> {
                    Snackbar.make(
                        binding.root,
                        "🎉 ${getString(R.string.level_up_message)} स्तर ${event.newLevel}!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    accessibilityManager.speak("बधाई! आप स्तर ${event.newLevel} पर पहुंच गए!")
                }
                is HomeUiEvent.ShowError -> {
                    Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                }
                else -> { /* Handle other events */ }
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardDailyGoal.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_quizList)
        }
        
        binding.btnAllBooks.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_bookList)
        }
        
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
        
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAction(HomeAction.RefreshData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
