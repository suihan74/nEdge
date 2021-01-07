package com.suihan74.notificationreporter.scenes.preferences.notch

import com.suihan74.notificationreporter.models.RectangleNotchSetting
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorViewModel

class RectangleNotchSettingViewModel(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : NotchSettingViewModel<RectangleNotchSetting>(notchPosition, editorViewModel) {

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val leftTopRadius = mutableLiveData<Float>()

    val rightTopRadius = mutableLiveData<Float>()

    val leftBottomRadius = mutableLiveData<Float>()

    val rightBottomRadius = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            leftTopRadius.value = it.leftTopRadius
            rightTopRadius.value = it.rightTopRadius
            leftBottomRadius.value = it.leftBottomRadius
            rightBottomRadius.value = it.rightBottomRadius
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = RectangleNotchSetting(
            widthAdjustment = widthAdjustment.value!!,
            heightAdjustment = heightAdjustment.value!!,
            leftTopRadius = leftTopRadius.value!!,
            rightTopRadius = rightTopRadius.value!!,
            leftBottomRadius = leftBottomRadius.value!!,
            rightBottomRadius = rightBottomRadius.value!!,
        )
    }
}
