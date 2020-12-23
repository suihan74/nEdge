package com.suihan74.notificationreporter.scenes.preferences

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.models.OutlinesSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val prefRepo: PreferencesRepository
) : ViewModel() {
    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : MutableLiveData<Float> = prefRepo.lightLevel

    /** 通知表示の輪郭線の色 */
    val notificationColor = mutableLiveData<Int>()

    /** 輪郭線の太さ */
    val lineThickness = mutableLiveData<Float>()

    /** 輪郭線左上角の角丸半径 */
    val leftTopCornerRadius = mutableLiveData<Float>()

    /** 輪郭線右上角の角丸半径 */
    val rightTopCornerRadius = mutableLiveData<Float>()

    /** 輪郭線左下角の角丸半径 */
    val leftBottomCornerRadius = mutableLiveData<Float>()

    /** 輪郭線右下角の角丸半径 */
    val rightBottomCornerRadius = mutableLiveData<Float>()

    /** ノッチ設定 */
    val topNotchSetting = mutableLiveData<NotchSetting>()

    /** ノッチ種類 */
    val topNotchType = mutableLiveData<NotchType>()

    // ------ //

    private fun <T> mutableLiveData() =
        MutableLiveData<T>().apply {
            observeForever {
                if (initialized) {
                    updateDefaultNotificationSetting()
                }
            }
        }

    private var initialized = false

    init {
        viewModelScope.launch(Dispatchers.Main) {
            prefRepo.init()
            prefRepo.defaultNotificationSetting.value!!.let { setting ->
                notificationColor.value = setting.color
                lineThickness.value = setting.thickness
                setting.outlinesSetting.let { outlines ->
                    leftTopCornerRadius.value = outlines.leftTopCornerRadius
                    rightTopCornerRadius.value = outlines.rightTopCornerRadius
                    leftBottomCornerRadius.value = outlines.leftBottomCornerRadius
                    rightBottomCornerRadius.value = outlines.rightBottomCornerRadius
                }
                setting.topNotchSetting.let { notch ->
                    topNotchSetting.value = notch
                    topNotchType.value = notch.type
                }
            }
            initialized = true

            updateDefaultNotificationSetting()
        }
    }

    // ------ //

    private val _defaultNotificationSetting = MutableLiveData<NotificationSetting>()
    val defaultNotificationSetting : LiveData<NotificationSetting> by lazy { _defaultNotificationSetting }

    private fun updateDefaultNotificationSetting() {
        if (!initialized) return

        val result = runCatching {
            _defaultNotificationSetting.value = NotificationSetting(
                color = notificationColor.value!!,
                thickness = lineThickness.value!!,
                outlinesSetting = OutlinesSetting(
                    leftTopCornerRadius = leftTopCornerRadius.value!!,
                    rightTopCornerRadius = rightTopCornerRadius.value!!,
                    leftBottomCornerRadius = leftBottomCornerRadius.value!!,
                    rightBottomCornerRadius = rightBottomCornerRadius.value!!
                ),
                topNotchSetting = topNotchSetting.value!!
            )
        }

        result.onFailure {
            Log.e("PreferencesViewModel", Log.getStackTraceString(it))
        }
    }

    fun saveSettings() = viewModelScope.launch {
        prefRepo.updateDefaultNotificationSetting(defaultNotificationSetting.value!!)
    }
}
