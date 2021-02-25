package com.suihan74.nedge.scenes.preferences.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import com.suihan74.nedge.databinding.FragmentGeneralPrefsBinding
import com.suihan74.nedge.scenes.preferences.PreferencesViewModel
import com.suihan74.nedge.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.utilities.extensions.hideSoftInputMethod
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 全般設定画面
 */
@AndroidEntryPoint
class GeneralPrefsFragment : Fragment() {
    companion object {
        fun createInstance() = GeneralPrefsFragment()
    }

    // ------ //

    private val viewModel by activityViewModels<PreferencesViewModel>()

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
        viewModel.viewModelScope.launch {
            viewModel.saveSettings()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SliderBindingAdapters.onTerminateLifecycle(this)
    }
}
