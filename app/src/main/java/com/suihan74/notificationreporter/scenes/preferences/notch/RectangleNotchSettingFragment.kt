package com.suihan74.notificationreporter.scenes.preferences.notch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.databinding.FragmentRectangleNotchSettingBinding
import com.suihan74.notificationreporter.scenes.preferences.PreferencesActivity
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import com.suihan74.utilities.lazyProvideViewModel

class RectangleNotchSettingFragment : Fragment() {
    companion object {
        fun createInstance() = RectangleNotchSettingFragment()
    }

    // ------ //

    private val preferencesActivity : PreferencesActivity
        get() = requireActivity() as PreferencesActivity

    private val preferencesViewModel : PreferencesViewModel
        get() = preferencesActivity.viewModel

    // ------ //

    private val viewModel by lazyProvideViewModel {
        RectangleNotchSettingViewModel(preferencesViewModel)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRectangleNotchSettingBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.prefVm = preferencesViewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }
}
