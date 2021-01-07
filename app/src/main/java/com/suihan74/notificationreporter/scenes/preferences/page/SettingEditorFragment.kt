package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.FragmentSettingEditorBinding
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.launch

class SettingEditorFragment : Fragment() {

    companion object {
        fun createInstance(entity: NotificationEntity) = SettingEditorFragment().also {
            it.lifecycleScope.launchWhenCreated {
                it.viewModel.initialize(entity)
            }
        }
    }

    // ------ //

    private val preferencesActivity
        get() = requireActivity() as PreferencesActivity

    private val preferencesViewModel
        get() = preferencesActivity.viewModel

    val viewModel by lazyProvideViewModel {
        SettingEditorViewModel(Application.instance, preferencesViewModel)
    }

    private var onBackPressedCallback : OnBackPressedCallback? = null

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingEditorBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.saveButton.setOnClickListener {
            onBackPressedCallback = null
            lifecycleScope.launch {
                viewModel.saveSettings()
                preferencesActivity.closeSettingEditor(this@SettingEditorFragment)
            }
        }

        binding.cancelButton.setOnClickListener {
            onBackPressedCallback?.handleOnBackPressed()
        }

        binding.pickOutlinesColorButton.setOnClickListener {
            viewModel.openOutlinesColorPickerDialog(childFragmentManager)
        }

        binding.topNotchTypeSelectionButton.setOnClickListener {
            viewModel.openTopNotchTypeSelectionDialog(childFragmentManager)
        }

        binding.bottomNotchTypeSelectionButton.setOnClickListener {
            viewModel.openBottomNotchTypeSelectionDialog(childFragmentManager)
        }

        viewModel.observeTopNotchType(
            R.id.topNotchSettingFragmentArea,
            viewLifecycleOwner,
            childFragmentManager
        )

        viewModel.observeBottomNotchType(
            R.id.bottomNotchSettingFragmentArea,
            viewLifecycleOwner,
            childFragmentManager
        )

        // 戻るボタンの割り込み
        onBackPressedCallback = preferencesActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            onBackPressedCallback = null
            remove()
            preferencesActivity.closeSettingEditor(this@SettingEditorFragment)
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch {
            viewModel.getNotchRect(preferencesActivity.window)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SliderBindingAdapters.onTerminateLifecycle(this)
    }
}
