package com.suihan74.notificationreporter.scenes.preferences.notch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.databinding.FragmentWaterDropNotchSettingBinding
import com.suihan74.utilities.fragment.withArguments
import com.suihan74.utilities.lazyProvideViewModel

class WaterDropNotchSettingFragment : Fragment() {
    companion object {
        fun createInstance(settingKey: String) = WaterDropNotchSettingFragment().withArguments {
            putString(ARG_SETTING_KEY, settingKey)
        }

        private const val ARG_SETTING_KEY = "ARG_SETTING_KEY"
    }

    // ------ //

    private val viewModel by lazyProvideViewModel {
        val settingKey = requireArguments().getString(ARG_SETTING_KEY)!!
        val app = Application.instance
        WaterDropNotchSettingViewModel(app.preferencesRepository, settingKey)
    }

    // ------ //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWaterDropNotchSettingBinding.inflate(inflater, container, false).also {
            it.vm = viewModel
            it.lifecycleOwner = viewLifecycleOwner
        }

        binding.widthAdjustmentSlider.addOnChangeListener(viewModel.widthAdjustmentChangeListener)

        binding.heightAdjustmentSlider.addOnChangeListener(viewModel.heightAdjustmentChangeListener)

        binding.topRadiusSlider.addOnChangeListener(viewModel.topRadiusChangeListener)

        binding.waterDropRadiusSlider.addOnChangeListener(viewModel.waterDropRadiusChangeListener)

        binding.topDegreeSlider.addOnChangeListener(viewModel.topDegreeChangeListener)

        binding.waterDropDegreeSlider.addOnChangeListener(viewModel.waterDropDegreeChangeListener)

        return binding.root
    }
}
