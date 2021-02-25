package com.suihan74.nedge.scenes.preferences.page

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.databinding.FragmentSettingEditorBinding
import com.suihan74.nedge.scenes.preferences.PreferencesActivity
import com.suihan74.nedge.scenes.preferences.PreferencesViewModel
import com.suihan74.nedge.scenes.preferences.dataBinding.SliderBindingAdapters
import com.suihan74.utilities.extensions.hideSoftInputMethod
import com.suihan74.utilities.lazyProvideViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 通知表示編集画面
 */
@AndroidEntryPoint
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

    private val preferencesViewModel by activityViewModels<PreferencesViewModel>()

    // ------ //

    val viewModel by lazyProvideViewModel {
        SettingEditorViewModel(Application.instance, preferencesViewModel)
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
            it.fragmentManager = childFragmentManager
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

        val actionDone = TextView.OnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.hideSoftInputMethod(binding.mainLayout)
                true
            }
            else false
        }

        val focusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                preferencesViewModel.showSystemUI()
            }
            else {
                preferencesViewModel.hideSystemUI()
            }
        }

        binding.displayNameEditText.apply {
            setOnEditorActionListener(actionDone)
            onFocusChangeListener = focusChangeListener
        }

        binding.keywordEditText.apply {
            setOnEditorActionListener(actionDone)
            onFocusChangeListener = focusChangeListener
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

    override fun onDetach() {
        super.onDetach()
        preferencesViewModel.showSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        SliderBindingAdapters.onTerminateLifecycle(this)
    }

    override fun onResume() {
        super.onResume()
        preferencesViewModel.hideSystemUI()
    }
}
