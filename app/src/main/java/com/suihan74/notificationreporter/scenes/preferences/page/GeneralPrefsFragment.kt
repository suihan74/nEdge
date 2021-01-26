package com.suihan74.notificationreporter.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.databinding.FragmentGeneralPrefsBinding
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dataBinding.SliderBindingAdapters
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

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGeneralPrefsBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.fragmentManager = childFragmentManager
            it.lifecycleOwner = viewLifecycleOwner
            it.lifecycle = viewLifecycleOwner.lifecycle
        }

        binding.lightOffIntervalEditText.also { editText ->
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    editText.hideSoftInputMethod(binding.mainLayout)
                    true
                }
                else false
            }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    editText.hideSoftInputMethod(binding.mainLayout)
                }
            }
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
