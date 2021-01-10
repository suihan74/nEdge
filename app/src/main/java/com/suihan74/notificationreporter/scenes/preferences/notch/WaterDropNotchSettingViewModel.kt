package com.suihan74.notificationreporter.scenes.preferences.notch

import com.suihan74.notificationreporter.models.WaterDropNotchSetting
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorViewModel

class WaterDropNotchSettingViewModel(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : NotchSettingViewModel<WaterDropNotchSetting>(notchPosition, editorViewModel) {

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val majorRadius = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            majorRadius.value = it.majorRadius
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = WaterDropNotchSetting(
            widthAdjustment = widthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            majorRadius = majorRadius.value!!,
        )
    }
}
