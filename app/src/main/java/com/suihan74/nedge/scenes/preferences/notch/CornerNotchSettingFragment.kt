package com.suihan74.nedge.scenes.preferences.notch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.nedge.databinding.FragmentCornerNotchSettingBinding
import com.suihan74.nedge.scenes.preferences.page.SettingEditorFragment
import com.suihan74.utilities.extensions.getEnum
import com.suihan74.utilities.extensions.putEnum
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel

class CornerNotchSettingFragment : Fragment() {
    companion object {
        fun createInstance(notchPosition: NotchPosition) = CornerNotchSettingFragment().withArguments {
            putEnum(Arg.NOTCH_POSITION.name, notchPosition)
        }
    }

    // ------ //

    private val settingEditorFragment
        get() = requireParentFragment() as SettingEditorFragment

    private val settingEditorViewModel
        get() = settingEditorFragment.viewModel

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val notchPosition = requireArguments().let {
            it.getEnum<NotchPosition>(Arg.NOTCH_POSITION.name)!!
        }
        CornerNotchSettingViewModel(notchPosition, settingEditorViewModel)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCornerNotchSettingBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }
}
