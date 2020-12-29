package com.suihan74.notificationreporter.scenes.preferences

import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.models.OutlinesSetting
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.dialog.TimePickerDialogFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.threeten.bp.LocalTime

class PreferencesViewModel(
    private val prefRepo: PreferencesRepository
) : ViewModel() {
    companion object {
        const val DEFAULT_SETTING_NAME = NotificationEntity.DEFAULT_SETTING_NAME
    }

    /** 選択中のメニュー項目 */
    val selectedMenuItem = MutableLiveData(MenuItem.GENERAL)

    // ------ //

    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel = MutableLiveData<Float>()

    /** 通知を行わない時間帯(開始時刻) */
    val silentTimezoneStart = MutableLiveData<Int>()

    /** 通知を行わない時間帯(終了時刻) */
    val silentTimezoneEnd = MutableLiveData<Int>()

    /** 通知を行うのに必要な最低バッテリレベル */
    val requiredBatteryLevel = MutableLiveData<Int>()

    // ------ //

    /** 通知表示の輪郭線の色 */
    val notificationColor = mutableLiveData<Int>()

    /** 輪郭線の太さ */
    val lineThickness = mutableLiveData<Float>()

    /** ブラーの強さ */
    val blurSize = mutableLiveData<Float>()

    /** 輪郭線の角丸半径(画面上部) */
    val topCornerRadius = mutableLiveData<Float>()

    /** 輪郭線の角丸半径(画面下部) */
    val bottomCornerRadius = mutableLiveData<Float>()

    /** ノッチ設定 */
    val topNotchSetting = mutableLiveData<NotchSetting>()

    /** ノッチ種類 */
    val topNotchType = mutableLiveData<NotchType>()

    /** 輪郭線の上部角丸を編集中 */
    val editingTopCornerRadius = MutableLiveData(false)

    /** 輪郭線の下部角丸を編集中 */
    val editingBottomCornerRadius = MutableLiveData(false)

    /** 上部ノッチを編集中 */
    val editingTopNotch = MutableLiveData(false)

    // ------ //

    /**
     * 初期値をセットする
     */
    init {
        bindFlow(PreferencesKey.LIGHT_LEVEL, lightLevel)
        bindFlow(PreferencesKey.SILENT_TIMEZONE_START, silentTimezoneStart)
        bindFlow(PreferencesKey.SILENT_TIMEZONE_END, silentTimezoneEnd)
        bindFlow(PreferencesKey.REQUIRED_BATTERY_LEVEL, requiredBatteryLevel)

        setCurrentTarget(DEFAULT_SETTING_NAME)
    }

    /**
     * 編集したデータを保存する
     */
    fun saveSettings() = viewModelScope.launch {
        prefRepo.editPreferences {
            set(PreferencesKey.LIGHT_LEVEL, lightLevel)
            set(PreferencesKey.SILENT_TIMEZONE_START, silentTimezoneStart)
            set(PreferencesKey.SILENT_TIMEZONE_END, silentTimezoneEnd)
            set(PreferencesKey.REQUIRED_BATTERY_LEVEL, requiredBatteryLevel)
        }
        prefRepo.updateNotificationSetting(targetAppName, notificationSetting.value!!)
    }

    // ------ //

    /**
     * 輪郭線用の各設定値の変更をビュー表示用の`LiveData`に反映させる
     */
    private fun <T> mutableLiveData() =
        MutableLiveData<T>().apply {
            observeForever {
                updateNotificationSetting()
            }
        }

    /**
     * `Flow`で流れてくる設定値と`LiveData`を紐づける
     */
    private fun <T> bindFlow(key: PreferencesKey<T>, liveData: MutableLiveData<T>) {
        prefRepo.getPreferenceFlow(key)
            .onEach { liveData.value = it }
            .launchIn(viewModelScope)
    }

    // ------ //

    /**
     * 設定値すべての更新が完了してからプレビューに反映するためのロック
     */
    private val currentTargetMutex = Mutex()

    /** 編集中の対象アプリ名 */
    private var targetAppName : String = NotificationEntity.DEFAULT_SETTING_NAME

    /** 現在の画面で編集中のアプリ設定をセットする */
    private fun setCurrentTarget(appName: String) = viewModelScope.launch(Dispatchers.Main) {
        currentTargetMutex.withLock {
            prefRepo.getNotificationSetting(appName).let { setting ->
                notificationColor.value = setting.color
                lineThickness.value = setting.thickness
                blurSize.value = setting.blurSize
                setting.outlinesSetting.let { outlines ->
                    topCornerRadius.value = outlines.topCornerRadius
                    bottomCornerRadius.value = outlines.bottomCornerRadius
                }
                setting.topNotchSetting.let { notch ->
                    topNotchSetting.value = notch
                    topNotchType.value = notch.type
                }
            }
        }

        updateNotificationSetting()
    }

    // ------ //

    /**
     * 編集中の各設定値を反映した`NotificationSetting`
     *
     * プレビューの表示、データ保存に使用
     */
    val notificationSetting : LiveData<NotificationSetting> by lazy { _notificationSetting }
    private val _notificationSetting = MutableLiveData<NotificationSetting>()

    /** 編集中の設定を表示用のサンプルデータに反映する */
    private fun updateNotificationSetting() {
        if (!currentTargetMutex.tryLock()) return

        val result = runCatching {
            _notificationSetting.value = NotificationSetting(
                color = notificationColor.value!!,
                thickness = lineThickness.value!!,
                blurSize = blurSize.value!!,
                outlinesSetting = OutlinesSetting(
                    topCornerRadius = topCornerRadius.value!!,
                    bottomCornerRadius = bottomCornerRadius.value!!,
                ),
                topNotchSetting = topNotchSetting.value!!
            )
        }

        result.onFailure {
            Log.e("PreferencesViewModel", Log.getStackTraceString(it))
        }

        currentTargetMutex.unlock()
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
