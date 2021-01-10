package com.suihan74.notificationreporter.scenes.preferences.notch

import com.suihan74.notificationreporter.models.RectangleNotchSetting
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorViewModel

class RectangleNotchSettingViewModel(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : NotchSettingViewModel<RectangleNotchSetting>(notchPosition, editorViewModel) {

    val majorWidthAdjustment = mutableLiveData<Float>()

    val minorWidthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val majorRadius = mutableLiveData<Float>()

    val minorRadius = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            majorWidthAdjustment.value = it.majorWidthAdjustment
            minorWidthAdjustment.value = it.minorWidthAdjustment
            heightAdjustment.value = it.heightAdjustment
            majorRadius.value = it.majorRadius
            minorRadius.value = it.minorRadius
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = RectangleNotchSetting(
            majorWidthAdjustment = majorWidthAdjustment.value!!,
            minorWidthAdjustment = minorWidthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            majorRadius = majorRadius.value!!,
            minorRadius = minorRadius.value!!,
        )
    }
}
