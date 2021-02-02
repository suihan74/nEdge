package com.suihan74.nedge.scenes.preferences.notch

import com.suihan74.nedge.models.PunchHoleNotchSetting
import com.suihan74.nedge.scenes.preferences.page.SettingEditorViewModel

class PunchHoleNotchSettingViewModel(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : NotchSettingViewModel<PunchHoleNotchSetting>(notchPosition, editorViewModel) {

    val cx = mutableLiveData<Float>()

    val cy = mutableLiveData<Float>()

    val radius = mutableLiveData<Float>()

    val horizontalEdgeSize = mutableLiveData<Float>()

    val verticalEdgeSize = mutableLiveData<Float>()

    // ------ //

    override suspend fun initialize() {
        setting.value!!.let {
            cx.value = it.cx
            cy.value = it.cy
            radius.value = it.radius
            horizontalEdgeSize.value = it.horizontalEdgeSize
            verticalEdgeSize.value = it.verticalEdgeSize
        }
    }

    override suspend fun updateNotchSetting() {
        setting.value = setting.value!!.copy(
            cx = cx.value!!,
            cy = cy.value!!,
            radius = radius.value!!,
            horizontalEdgeSize = horizontalEdgeSize.value!!,
            verticalEdgeSize = verticalEdgeSize.value!!
        )
    }
}
