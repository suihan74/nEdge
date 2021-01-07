package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.databinding.FragmentGeneralPrefsBinding
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.utilities.extensions.hideSoftInputMethod
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 全般設定画面
 */
class GeneralPrefsFragment : Fragment() {
    companion object {
        fun createInstance() = GeneralPrefsFragment()
    }

    // ------ //

    private val preferencesActivity
        get() = requireActivity() as PreferencesActivity

    private val viewModel
        get() = preferencesActivity.viewModel

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGeneralPrefsBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
            it.lifecycle = viewLifecycleOwner.lifecycle
        }

        binding.previewButton.setOnClickListener {
            val intent = Intent(requireContext(), LockScreenActivity::class.java)
            startActivity(intent)
        }

        binding.notifyButton.setOnClickListener {
            val id = Random.nextInt().absoluteValue
            Application.instance.notifyDummy(5, id, "dummy-$id")
        }

        binding.lightOffIntervalEditText.also { editText ->
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editText.clearFocus()
                }
                false
            }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    preferencesActivity.showSystemUI()
                }
                else {
                    preferencesActivity.hideSoftInputMethod(binding.mainLayout)
                    preferencesActivity.hideSystemUI()
                }
            }
        }

        binding.silentTimezoneStartButton.setOnClickListener {
            viewModel.openSilentTimezoneStartPickerDialog(childFragmentManager)
        }

        binding.silentTimezoneEndButton.setOnClickListener {
            viewModel.openSilentTimezoneEndPickerDialog(childFragmentManager)
        }

        binding.multiNoticesSolutionSelectionButton.setOnClickListener {
            viewModel.openMultipleNotificationsSolutionSelectionDialog(childFragmentManager)
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        SliderBindingAdapters.onTerminateLifecycle(this)
    }
}
