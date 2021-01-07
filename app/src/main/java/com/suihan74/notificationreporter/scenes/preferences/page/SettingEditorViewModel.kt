package com.suihan74.notificationreporter.scenes.preferences.page

import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Window
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.models.OutlinesSetting
import com.suihan74.notificationreporter.scenes.preferences.dialog.ColorPickerDialogFragment
import com.suihan74.notificationreporter.scenes.preferences.notch.NotchPosition
import com.suihan74.notificationreporter.scenes.preferences.notch.RectangleNotchSettingFragment
import com.suihan74.notificationreporter.scenes.preferences.notch.WaterDropNotchSettingFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SettingEditorViewModel(private val application: Application) : ViewModel() {

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

    // --- //

    /** 画面上部ノッチが設定可能 */
    val topNotchEnabled = MutableLiveData(false)

    /** 画面上部ノッチ設定 */
    val topNotchSetting = mutableLiveData<NotchSetting>()

    /** 画面上部ノッチ種類 */
    val topNotchType = mutableLiveData<NotchType>()

    /** 画面下部ノッチが設定可能 */
    val bottomNotchEnabled = MutableLiveData(false)

    /** 画面下部ノッチ設定 */
    val bottomNotchSetting = mutableLiveData<NotchSetting>()

    /** 画面下部ノッチ種類 */
    val bottomNotchType = mutableLiveData<NotchType>()

    /** 輪郭線の上部角丸を編集中 */
    val editingTopCornerRadius = MutableLiveData(false)

    /** 輪郭線の下部角丸を編集中 */
    val editingBottomCornerRadius = MutableLiveData(false)

    /** 上部ノッチを編集中 */
    val editingTopNotch = MutableLiveData(false)

    /** 下部ノッチを編集中 */
    val editingBottomNotch = MutableLiveData(false)

    // ------ //

    private var targetEntity: NotificationEntity? = null

    fun initialize(entity: NotificationEntity) {
        setCurrentTarget(entity)
    }

    /**
     * 編集したデータを保存する
     */
    suspend fun saveSettings() {
        targetEntity?.let {
            application.preferencesRepository.updateNotificationSetting(
                it.copy(setting = notificationSetting.value!!)
            )
        }
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

    /** 現在の画面で編集中のアプリ設定をセットする */
    private fun setCurrentTarget(entity: NotificationEntity) = viewModelScope.launch(Dispatchers.Main) {
        currentTargetMutex.withLock {
            targetEntity = entity
            application.preferencesRepository.getNotificationSettingOrDefault(entity).let { setting ->
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
                setting.bottomNotchSetting.let { notch ->
                    bottomNotchSetting.value = notch
                    bottomNotchType.value = notch.type
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
    val notificationSetting: LiveData<NotificationSetting> by lazy { _notificationSetting }
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
                topNotchSetting = topNotchSetting.value!!,
                bottomNotchSetting = bottomNotchSetting.value!!,
            )
        }

        result.onFailure {
            Log.e("PreferencesViewModel", Log.getStackTraceString(it))
        }

        currentTargetMutex.unlock()
    }

    // ------ //

    suspend fun getNotchRect(window: Window) = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val verticalCenter = Point().let {
                window.windowManager.defaultDisplay.getSize(it)
                it.y * .5f
            }
            val topRect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top < verticalCenter
            }
            val bottomRect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top > verticalCenter
            }

            topNotchEnabled.value = topRect != null
            bottomNotchEnabled.value = bottomRect != null
        }
        else {
            topNotchEnabled.value = false
            bottomNotchEnabled.value = false
        }
    }

    // ------ //

    /**
     * ノッチタイプ変更に伴いノッチ設定項目を切り替える
     */
    private fun observeNotchType(
        notchType: MutableLiveData<NotchType>,
        @IdRes targetViewId: Int,
        lifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager
    ) {
        val notchPosition =
            when (notchType) {
                topNotchType -> NotchPosition.TOP
                bottomNotchType -> NotchPosition.BOTTOM
                else -> throw IllegalArgumentException()
            }

        notchType.observe(lifecycleOwner, {
            val fragment = when (it) {
                NotchType.RECTANGLE ->
                    RectangleNotchSettingFragment.createInstance(notchPosition)

                NotchType.WATER_DROP ->
                    WaterDropNotchSettingFragment.createInstance(notchPosition)

                else -> Fragment()
            }

            fragmentManager.beginTransaction()
                .replace(targetViewId, fragment)
                .commit()
        })
    }

    /**
     * ノッチタイプ変更に伴いノッチ設定項目を切り替える
     */
    fun observeTopNotchType(
        @IdRes targetViewId: Int,
        lifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager
    ) {
        observeNotchType(topNotchType, targetViewId, lifecycleOwner, fragmentManager)
    }

    /**
     * ノッチタイプ変更に伴いノッチ設定項目を切り替える
     */
    fun observeBottomNotchType(
        @IdRes targetViewId: Int,
        lifecycleOwner: LifecycleOwner,
        fragmentManager: FragmentManager
    ) {
        observeNotchType(bottomNotchType, targetViewId, lifecycleOwner, fragmentManager)
    }

    // ------ //

    /**
     * ノッチタイプを選択するダイアログを開く
     */
    private fun openNotchTypeSelectionDialog(
        notchType: MutableLiveData<NotchType>,
        fragmentManager: FragmentManager
    ) {
        val notchTypes = NotchType.values()
        val labels = notchTypes.map { it.textId }
        val initialSelected = notchTypes.indexOf(notchType.value)

        val titleId = when (notchType) {
            topNotchType -> R.string.prefs_top_notch_type_selection_desc
            bottomNotchType -> R.string.prefs_bottom_notch_type_selection_desc
            else -> throw IllegalArgumentException()
        }

        val dialog = AlertDialogFragment.Builder()
            .setTitle(titleId)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val type = notchTypes[which]
                if (notchType.value == type) return@setSingleChoiceItems

                when (notchType) {
                    topNotchType ->
                        topNotchSetting.value = NotchSetting.createInstance(type)

                    bottomNotchType ->
                        bottomNotchSetting.value = NotchSetting.createInstance(type)
                }
                notchType.value = type
            }
            .setNegativeButton(R.string.dialog_cancel)
            .create()
        dialog.show(fragmentManager, null)
    }

    /**
     * ノッチタイプを選択するダイアログを開く
     */
    fun openTopNotchTypeSelectionDialog(fragmentManager: FragmentManager) {
        openNotchTypeSelectionDialog(topNotchType, fragmentManager)
    }

    /**
     * ノッチタイプを選択するダイアログを開く
     */
    fun openBottomNotchTypeSelectionDialog(fragmentManager: FragmentManager) {
        openNotchTypeSelectionDialog(bottomNotchType, fragmentManager)
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
