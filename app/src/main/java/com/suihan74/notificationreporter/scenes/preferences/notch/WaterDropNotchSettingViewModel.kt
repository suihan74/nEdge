package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.slider.Slider
import com.suihan74.notificationreporter.models.WaterDropNotchSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository

class WaterDropNotchSettingViewModel(
    prefRepo: PreferencesRepository,
    private val settingKey : String
) : ViewModel() {
    // TODO: 任意の設定を対象にできるようにする
    val setting = MutableLiveData(prefRepo.defaultNotificationSetting.value?.topNotchSetting as? WaterDropNotchSetting).also {
        it.observeForever { setting ->
            if (setting != null) {
                prefRepo.defaultNotificationSetting.value =
                    prefRepo.defaultNotificationSetting.value?.copy(
                        topNotchSetting = setting
                    )
            }
        }
    }

    // ------ //

    val widthAdjustmentChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(widthAdjustment = value)
    }

    val heightAdjustmentChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(heightAdjustment = value)
    }

    val topRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(topRadius = value)
    }

    val waterDropRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(waterDropRadius = value)
    }

    val topDegreeChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(topDegree = value)
    }

    val waterDropDegreeChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(waterDropDegree = value)
    }
}
