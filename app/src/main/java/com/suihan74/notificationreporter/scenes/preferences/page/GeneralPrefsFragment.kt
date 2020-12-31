package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.databinding.FragmentGeneralPrefsBinding
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.notificationreporter.scenes.preferences.notch.RectangleNotchSettingFragment
import com.suihan74.notificationreporter.scenes.preferences.notch.WaterDropNotchSettingFragment
import com.suihan74.utilities.extensions.hideSoftInputMethod

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

    private var _binding : FragmentGeneralPrefsBinding? = null
    private val binding get() = _binding!!

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralPrefsBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
            it.lifecycle = viewLifecycleOwner.lifecycle
        }

        binding.previewButton.setOnClickListener {
            val intent = Intent(requireContext(), LockScreenActivity::class.java)
            startActivity(intent)
        }

        binding.notifyButton.setOnClickListener {
            Application.instance.notifyDummy(5)
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
            viewModel.openSilentTimezonePickerDialog(viewModel.silentTimezoneStart, childFragmentManager)
        }

        binding.silentTimezoneEndButton.setOnClickListener {
            viewModel.openSilentTimezonePickerDialog(viewModel.silentTimezoneEnd, childFragmentManager)
        }

        binding.pickOutlinesColorButton.setOnClickListener {
            viewModel.openOutlinesColorPickerDialog(childFragmentManager)
        }

        binding.notchTypeSelectionButton.setOnClickListener {
            viewModel.openNotchTypeSelectionDialog(viewModel.topNotchType, childFragmentManager)
        }

        viewModel.topNotchType.observe(viewLifecycleOwner, {
            val fragment = when (it) {
                NotchType.RECTANGLE ->
                    RectangleNotchSettingFragment.createInstance()

                NotchType.WATER_DROP ->
                    WaterDropNotchSettingFragment.createInstance()

                else -> Fragment()
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.notchSettingFragmentArea, fragment)
                .commit()
        })

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        SliderBindingAdapters.onTerminateLifecycle(this)
    }
}
