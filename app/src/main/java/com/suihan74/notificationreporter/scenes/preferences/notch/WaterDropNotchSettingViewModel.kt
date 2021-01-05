package com.suihan74.notificationreporter.scenes.preferences.notch

import com.suihan74.notificationreporter.models.WaterDropNotchSetting
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel

class WaterDropNotchSettingViewModel(
    notchPosition: NotchPosition,
    preferencesViewModel: PreferencesViewModel,
) : NotchSettingViewModel<WaterDropNotchSetting>(notchPosition, preferencesViewModel) {

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val topRadius = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            topRadius.value = it.topRadius
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = WaterDropNotchSetting(
            widthAdjustment = widthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            topRadius = topRadius.value!!,
        )
    }
}
