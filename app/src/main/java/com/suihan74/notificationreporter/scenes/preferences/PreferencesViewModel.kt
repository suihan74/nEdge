package com.suihan74.notificationreporter.scenes.preferences

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.database.notification.NotificationEntity
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
    companion object {
        const val DEFAULT_SETTING_NAME = NotificationEntity.DEFAULT_SETTING_NAME
    }

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
                    updateNotificationSetting()
                }
            }
        }

    init {
        setCurrentTarget(DEFAULT_SETTING_NAME)
    }

    private var initialized = false

    /** 編集中の対象アプリ名 */
    private var targetAppName : String = NotificationEntity.DEFAULT_SETTING_NAME

    /** 現在の画面で編集中のアプリ設定をセットする */
    fun setCurrentTarget(appName: String) = viewModelScope.launch(Dispatchers.Main) {
        initialized = false

        if (appName == NotificationEntity.DEFAULT_SETTING_NAME) {
            prefRepo.init()
        }

        prefRepo.getNotificationSetting(appName).let { setting ->
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

        updateNotificationSetting()
    }

    // ------ //

    private val _notificationSetting = MutableLiveData<NotificationSetting>()
    val notificationSetting : LiveData<NotificationSetting> by lazy { _notificationSetting }

    /** 編集中の設定を表示用のサンプルデータに反映する */
    private fun updateNotificationSetting() {
        if (!initialized) return

        val result = runCatching {
            _notificationSetting.value = NotificationSetting(
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

    /** 編集中のデータをDBに保存する */
    fun saveSettings() = viewModelScope.launch {
        prefRepo.updateNotificationSetting(targetAppName, notificationSetting.value!!)
    }
}
