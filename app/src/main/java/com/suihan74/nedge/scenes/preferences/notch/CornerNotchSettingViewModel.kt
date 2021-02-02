package com.suihan74.nedge.scenes.preferences.notch

import com.suihan74.nedge.models.CornerNotchSetting
import com.suihan74.nedge.scenes.preferences.page.SettingEditorViewModel

class CornerNotchSettingViewModel(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : NotchSettingViewModel<CornerNotchSetting>(notchPosition, editorViewModel) {

    val majorWidthAdjustment = mutableLiveData<Float>()

    val minorWidthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val majorRadius = mutableLiveData<Float>()

    val minorRadius = mutableLiveData<Float>()

    val middleRadius = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            majorWidthAdjustment.value = it.majorWidthAdjustment
            minorWidthAdjustment.value = it.minorWidthAdjustment
            heightAdjustment.value = it.heightAdjustment
            majorRadius.value = it.majorRadius
            minorRadius.value = it.minorRadius
            middleRadius.value = it.middleRadius
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = setting.value!!.copy(
            majorWidthAdjustment = majorWidthAdjustment.value!!,
            minorWidthAdjustment = minorWidthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            majorRadius = majorRadius.value!!,
            minorRadius = minorRadius.value!!,
            middleRadius = middleRadius.value!!,
        )
    }
}
