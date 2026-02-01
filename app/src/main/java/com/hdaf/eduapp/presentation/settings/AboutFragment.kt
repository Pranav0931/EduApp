package com.hdaf.eduapp.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hdaf.eduapp.BuildConfig
import com.hdaf.eduapp.R
import com.hdaf.eduapp.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment showing app information.
 */
@AndroidEntryPoint
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupVersion()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupVersion() {
        binding.tvVersion.text = getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME
    }

    private fun setupClickListeners() {
        binding.cardPrivacy.setOnClickListener {
            openUrl("https://eduapp.com/privacy")
        }

        binding.cardTerms.setOnClickListener {
            openUrl("https://eduapp.com/terms")
        }

        binding.cardContact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@eduapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "EduApp Support Request")
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
