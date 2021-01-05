package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.notificationreporter.models.WaterDropNotchSetting
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import com.suihan74.utilities.extensions.alsoAs

class WaterDropNotchSettingViewModel(
    notchPosition: NotchPosition,
    preferencesViewModel: PreferencesViewModel,
) : ViewModel() {
    val setting =
        when (notchPosition) {
            NotchPosition.TOP -> preferencesViewModel.topNotchSetting
            NotchPosition.BOTTOM -> preferencesViewModel.bottomNotchSetting
        }

    val editing =
        when (notchPosition) {
            NotchPosition.TOP -> preferencesViewModel.editingTopNotch
            NotchPosition.BOTTOM -> preferencesViewModel.editingBottomNotch
        }

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val topRadius = mutableLiveData<Float>()

    // ------ //

    private var initialized = false

    init {
        setting.value!!.alsoAs<WaterDropNotchSetting> {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            topRadius.value = it.topRadius
        }
        initialized = true
    }

    private fun <T> mutableLiveData() =
        MutableLiveData<T>().apply {
            observeForever {
                if (initialized) {
                    updateNotchSetting()
                }
            }
        }

    @MainThread
    private fun updateNotchSetting() {
        if (!initialized) return

        setting.value = WaterDropNotchSetting(
            widthAdjustment = widthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            topRadius = topRadius.value!!,
        )
    }
}
