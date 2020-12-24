package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.notificationreporter.models.RectangleNotchSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import com.suihan74.utilities.extensions.alsoAs

class RectangleNotchSettingViewModel(
    preferencesViewModel: PreferencesViewModel,
    prefRepo: PreferencesRepository,
    private val settingKey : String
) : ViewModel() {
    val setting = preferencesViewModel.topNotchSetting

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val leftTopRadius = mutableLiveData<Float>()

    val rightTopRadius = mutableLiveData<Float>()

    val leftBottomRadius = mutableLiveData<Float>()

    val rightBottomRadius = mutableLiveData<Float>()

    // ------ //

    private var initialized = false

    init {
        setting.value!!.alsoAs<RectangleNotchSetting> {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            leftTopRadius.value = it.leftTopRadius
            rightTopRadius.value = it.rightTopRadius
            leftBottomRadius.value = it.leftBottomRadius
            rightBottomRadius.value = it.rightBottomRadius
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
