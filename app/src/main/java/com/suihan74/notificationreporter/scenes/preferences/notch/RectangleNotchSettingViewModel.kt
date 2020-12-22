package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.slider.Slider
import com.suihan74.notificationreporter.models.RectangleNotchSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository

class RectangleNotchSettingViewModel(
    prefRepo: PreferencesRepository,
    private val settingKey : String
) : ViewModel() {
    // TODO: 任意の設定を対象にできるようにする
    val setting = MutableLiveData(prefRepo.defaultNotificationSetting.value?.topNotchSetting as? RectangleNotchSetting).also {
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

    val leftTopRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(leftTopRadius = value)
    }

    val rightTopRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(rightTopRadius = value)
    }

    val leftBottomRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(leftBottomRadius = value)
    }

    val rightBottomRadiusChangeListener = Slider.OnChangeListener { _, value, _ ->
        setting.value = setting.value?.copy(rightBottomRadius = value)
    }
}
