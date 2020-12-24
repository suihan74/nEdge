package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.notificationreporter.models.WaterDropNotchSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import com.suihan74.utilities.extensions.alsoAs

class WaterDropNotchSettingViewModel(
    preferencesViewModel: PreferencesViewModel,
    prefRepo: PreferencesRepository,
    private val settingKey : String
) : ViewModel() {
    val setting = preferencesViewModel.topNotchSetting

    val widthAdjustment = mutableLiveData<Float>()

    val heightAdjustment = mutableLiveData<Float>()

    val topRadius = mutableLiveData<Float>()

    val waterDropRadius = mutableLiveData<Float>()

    val topDegree = mutableLiveData<Float>()

    val waterDropDegree = mutableLiveData<Float>()

    // ------ //

    private var initialized = false

    init {
        setting.value!!.alsoAs<WaterDropNotchSetting> {
            widthAdjustment.value = it.widthAdjustment
            heightAdjustment.value = it.heightAdjustment
            topRadius.value = it.topRadius
            waterDropRadius.value = it.waterDropRadius
            topDegree.value = it.topDegree
            waterDropDegree.value = it.waterDropDegree
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
            waterDropRadius = waterDropRadius.value!!,
            topDegree = topDegree.value!!,
            waterDropDegree = waterDropDegree.value!!,
        )
    }
}
