package com.suihan74.notificationreporter.scenes.preferences

import android.graphics.Color
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.models.*
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import com.suihan74.notificationreporter.scenes.preferences.dialog.ColorPickerDialogFragment
import com.suihan74.notificationreporter.scenes.preferences.dialog.TimePickerDialogFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
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
        const val DEFAULT_SETTING_NAME = NotificationDao.DEFAULT_SETTING_NAME
    }

    /** 選択中のメニュー項目 */
    val selectedMenuItem = MutableLiveData(MenuItem.GENERAL)

    // ------ //

    /** ロック画面起動直後の画面の明るさ */
    val lightLevelOn = MutableLiveData<Float>()

    /** バックライト消灯後の画面の明るさ */
    val lightLevelOff = MutableLiveData<Float>()

    /** ライトレベルを編集中 */
    val editingLightLevel = MutableLiveData<EditingLightLevel>()

    /** ライトレベルプレビュー時の値 */
    val previewLightLevel = MutableLiveData<Float>()

    /** `lightLevelOn`で端末のシステム設定値を使用する */
    val useSystemLightLevelOn = MutableLiveData<Boolean>()

    /** ライト消灯までの待機時間(秒数で編集してミリ秒で保存する) */
    val lightOffInterval = MutableLiveData<Long>()

    /** 通知を行わない時間帯(開始時刻) */
    val silentTimezoneStart = MutableLiveData<LocalTime>()

    /** 通知を行わない時間帯(終了時刻) */
    val silentTimezoneEnd = MutableLiveData<LocalTime>()

    /** 通知を行うのに必要な最低バッテリレベル */
    val requiredBatteryLevel = MutableLiveData<Int>()

    /** ライトレベル編集状態 */
    enum class EditingLightLevel {
        NONE,
        ON,
        OFF
    }

    /** 複数通知の表示方法 */
    val multipleNotificationsSolution = MutableLiveData<MultipleNotificationsSolution>()

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
     * アプリ設定値すべての更新が完了してから保存するためのロック
     */
    private val prefsMutex = Mutex()

    /**
     * 初期値をセットする
     */
    init {
        prefRepo.preferencesFlow
            .onEach {
                prefsMutex.withLock {
                    lightLevelOn.value = it.lightLevelOn
                    lightLevelOff.value = it.lightLevelOff
                    useSystemLightLevelOn.value = it.useSystemLightLevelOn
                    lightOffInterval.value = it.lightOffInterval / 1_000L
                    silentTimezoneStart.value = it.silentTimezoneStart
                    silentTimezoneEnd.value = it.silentTimezoneEnd
                    requiredBatteryLevel.value = it.requiredBatteryLevel
                    multipleNotificationsSolution.value = it.multipleNotificationsSolution
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)

        setCurrentTarget(DEFAULT_SETTING_NAME)
    }

    /**
     * 編集したデータを保存する
     */
    fun saveSettings() = viewModelScope.launch {
        prefsMutex.withLock {
            prefRepo.updatePreferences {
                Preferences(
                    lightLevelOff = lightLevelOff.value!!,
                    lightLevelOn = lightLevelOn.value!!,
                    useSystemLightLevelOn = useSystemLightLevelOn.value!!,
                    lightOffInterval = lightOffInterval.value!! * 1_000L,
                    silentTimezoneStart = silentTimezoneStart.value!!,
                    silentTimezoneEnd = silentTimezoneEnd.value!!,
                    requiredBatteryLevel = requiredBatteryLevel.value!!,
                    multipleNotificationsSolution = multipleNotificationsSolution.value!!
                )
            }
        }
        prefRepo.updateNotificationSetting(
            targetAppName,
            setting = notificationSetting.value!!
        )
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

    // ------ //

    /**
     * 設定値すべての更新が完了してからプレビューに反映するためのロック
     */
    private val currentTargetMutex = Mutex()

    /** 編集中の対象アプリ名 */
    private var targetAppName : String = NotificationDao.DEFAULT_SETTING_NAME

    /** 現在の画面で編集中のアプリ設定をセットする */
    private fun setCurrentTarget(appName: String) = viewModelScope.launch(Dispatchers.Main) {
        currentTargetMutex.withLock {
            prefRepo.getNotificationSettingOrDefault(appName).let { setting ->
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
    fun openSilentTimezonePickerDialog(liveData: MutableLiveData<LocalTime>, fragmentManager: FragmentManager) {
        val localTime = liveData.value!!
        val dialog = TimePickerDialogFragment.createInstance(localTime.hour, localTime.minute, true)

        dialog.setOnTimeSetListener { _, value ->
            liveData.value = value
        }

        dialog.show(fragmentManager, null)
    }

    /**
     * 複数通知切替え方法を選択するダイアログを開く
     */
    fun openMultipleNotificationsSolutionSelectionDialog(fragmentManager: FragmentManager) {
        val solutions = MultipleNotificationsSolution.values()
        val labels = solutions.map { it.textId }
        val initialSelected = solutions.indexOf(multipleNotificationsSolution.value)

        val dialog = AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_multi_notices_solution_selection_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val solution = solutions[which]
                if (multipleNotificationsSolution.value != solution) {
                    multipleNotificationsSolution.value = solution
                }
            }
            .setNegativeButton(R.string.dialog_cancel)
            .create()
        dialog.show(fragmentManager, null)
    }

    /**
     * ノッチタイプを選択するダイアログを開く
     */
    fun openNotchTypeSelectionDialog(notchType: MutableLiveData<NotchType>, fragmentManager: FragmentManager) {
        val notchTypes = NotchType.values()
        val labels = notchTypes.map { it.textId }
        val initialSelected = notchTypes.indexOf(notchType.value)

        val dialog = AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_notch_type_selection_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val type = notchTypes[which]
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

    /**
     * 輪郭線の色を選択するダイアログを開く
     */
    fun openOutlinesColorPickerDialog(fragmentManager: FragmentManager) {
        val dialog = ColorPickerDialogFragment.createInstance(notificationColor.value ?: Color.WHITE)
        dialog.setOnColorPickedListener { _, value ->
            // alpha成分の編集は無視して必ず1.0にする
            val r = Color.red(value)
            val g = Color.green(value)
            val b = Color.blue(value)
            notificationColor.value = Color.argb(255, r, g, b)
        }
        dialog.show(fragmentManager, null)
    }
}
