package com.suihan74.notificationreporter.scenes.preferences.page

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.databinding.FragmentSettingEditorBinding
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.utilities.lazyProvideViewModel
import kotlinx.coroutines.launch

/**
 * 通知表示編集画面
 */
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

    val viewModel by lazyProvideViewModel {
        SettingEditorViewModel(Application.instance)
    }

    // ------ //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 遷移アニメーション
        enterTransition = TransitionSet().apply {
            addTransition(Fade())
            addTransition(Slide().also {
                it.slideEdge = Gravity.END
            })
            this.ordering = TransitionSet.ORDERING_TOGETHER
        }
    }

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
            lifecycleScope.launch {
                viewModel.saveSettings()
                preferencesActivity.closeSettingEditor()
            }
        }

        binding.cancelButton.setOnClickListener {
            preferencesActivity.closeSettingEditor()
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
