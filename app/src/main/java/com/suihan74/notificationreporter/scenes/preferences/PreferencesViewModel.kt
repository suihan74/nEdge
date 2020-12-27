package com.suihan74.notificationreporter.scenes.preferences

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.models.OutlinesSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.dialog.TimePickerDialogFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime

class PreferencesViewModel(
    private val prefRepo: PreferencesRepository
) : ViewModel() {
    companion object {
        const val DEFAULT_SETTING_NAME = NotificationEntity.DEFAULT_SETTING_NAME
    }

    /** 選択中のメニュー項目 */
    val selectedMenuItem = MutableLiveData(MenuItem.GENERAL)

    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : MutableLiveData<Float> = prefRepo.lightLevel

    /** 通知を行わない時間帯(開始時刻) */
    val silentTimezoneStart = prefRepo.silentTimezoneStart

    /** 通知を行わない時間帯(終了時刻) */
    val silentTimezoneEnd = prefRepo.silentTimezoneEnd

    /** 通知を行うのに必要な最低バッテリレベル */
    val requiredBatteryLevel = prefRepo.requiredBatteryLevel

    /** 通知表示の輪郭線の色 */
    val notificationColor = mutableLiveData<Int>()

    /** 輪郭線の太さ */
    val lineThickness = mutableLiveData<Float>()

    /** ブラーの強さ */
    val blurSize = mutableLiveData<Float>()

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

    /** 輪郭線の左上角丸を編集中 */
    val editingLeftTopCornerRadius = MutableLiveData(false)

    /** 輪郭線の右上角丸を編集中 */
    val editingRightTopCornerRadius = MutableLiveData(false)

    /** 輪郭線の左下角丸を編集中 */
    val editingLeftBottomCornerRadius = MutableLiveData(false)

    /** 輪郭線の右下角丸を編集中 */
    val editingRightBottomCornerRadius = MutableLiveData(false)

    /** 上部ノッチを編集中 */
    val editingTopNotch = MutableLiveData(false)

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
    private fun setCurrentTarget(appName: String) = viewModelScope.launch(Dispatchers.Main) {
        initialized = false

        prefRepo.getNotificationSetting(appName).let { setting ->
            notificationColor.value = setting.color
            lineThickness.value = setting.thickness
            blurSize.value = setting.blurSize
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
                blurSize = blurSize.value!!,
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

    // ------ //

    /**
     * 通知を行わない時間帯を設定するダイアログを開く
     */
    fun openSilentTimezonePickerDialog(liveData: MutableLiveData<Int>, fragmentManager: FragmentManager) {
        val localTime = liveData.value?.let {
            LocalTime.ofSecondOfDay(it.toLong())
        } ?: LocalTime.of(0, 0)

        val dialog = TimePickerDialogFragment.createInstance(localTime.hour, localTime.minute, true)

        dialog.setOnTimeSetListener { _, value ->
            liveData.value = value.toSecondOfDay()
        }

        dialog.show(fragmentManager, null)
    }

    /**
     * ノッチタイプを選択するダイアログを開く
     */
    fun openNotchTypeSelectionDialog(notchType: MutableLiveData<NotchType>, fragmentManager: FragmentManager) {
        val labels = NotchType.values().map { it.name }
        val initialSelected = labels.indexOf(notchType.value?.name)

        val dialog = AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_notch_type_selection_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val type = NotchType.values()[which]
                if (notchType.value == type) return@setSingleChoiceItems

                when (notchType) {
                    topNotchType ->
                        topNotchSetting.value = NotchSetting.createInstance(type)
                }
                notchType.value = type
            }
            .setNegativeButton(R.string.dialog_cancel)
            .create()
        dialog.show(fragmentManager, null)
    }
}
