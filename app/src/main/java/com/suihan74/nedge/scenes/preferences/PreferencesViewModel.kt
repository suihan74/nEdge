package com.suihan74.nedge.scenes.preferences

import android.Manifest
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.nedge.Application
import com.suihan74.nedge.R
import com.suihan74.nedge.dataStore.Preferences
import com.suihan74.nedge.models.ClockStyle
import com.suihan74.nedge.models.MultipleNotificationsSolution
import com.suihan74.nedge.models.UnknownNotificationSolution
import com.suihan74.nedge.repositories.PreferencesRepository
import com.suihan74.nedge.scenes.lockScreen.LockScreenActivity
import com.suihan74.nedge.scenes.preferences.dialog.TimePickerDialogFragment
import com.suihan74.utilities.fragment.AlertDialogFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val application : Application,
    private val prefRepo : PreferencesRepository
) : ViewModel() {

    /** 選択中のメニュー項目 */
    val selectedMenuItem = MutableLiveData(MenuItem.GENERAL)

    // ------ //

    /** nEdgeを利用する */
    val enabled = MutableLiveData<Boolean>()

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

    /** 一定時間経過後にnEdgeを終了する */
    val terminationInterval = MutableLiveData<Long>()

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

    /** 時刻の表示形式 */
    val clockStyle = MutableLiveData<ClockStyle>()

    /** 複数通知の表示方法 */
    val multipleNotificationsSolution = MutableLiveData<MultipleNotificationsSolution>()

    /** 設定登録されていない通知の処理方法 */
    val unknownNotificationSolution = MutableLiveData<UnknownNotificationSolution>()

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
                    enabled.value = it.enabled
                    lightLevelOn.value = it.lightLevelOn
                    lightLevelOff.value = it.lightLevelOff
                    useSystemLightLevelOn.value = it.useSystemLightLevelOn
                    lightOffInterval.value = it.lightOffInterval / 1_000L
                    terminationInterval.value = it.terminationInterval / 1_000L
                    silentTimezoneStart.value = it.silentTimezoneStart
                    silentTimezoneEnd.value = it.silentTimezoneEnd
                    requiredBatteryLevel.value = it.requiredBatteryLevel
                    multipleNotificationsSolution.value = it.multipleNotificationsSolution
                    unknownNotificationSolution.value = it.unknownNotificationSolution
                    clockStyle.value = it.clockStyle
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    /**
     * 編集したデータを保存する
     */
    suspend fun saveSettings() {
        prefsMutex.withLock {
            prefRepo.updatePreferences {
                Preferences(
                    enabled = enabled.value!!,
                    lightLevelOff = lightLevelOff.value!!,
                    lightLevelOn = lightLevelOn.value!!,
                    useSystemLightLevelOn = useSystemLightLevelOn.value!!,
                    lightOffInterval = lightOffInterval.value!! * 1_000L,
                    terminationInterval = terminationInterval.value!! * 1_000L,
                    silentTimezoneStart = silentTimezoneStart.value!!,
                    silentTimezoneEnd = silentTimezoneEnd.value!!,
                    requiredBatteryLevel = requiredBatteryLevel.value!!,
                    multipleNotificationsSolution = multipleNotificationsSolution.value!!,
                    unknownNotificationSolution = unknownNotificationSolution.value!!,
                    clockStyle = clockStyle.value!!
                )
            }
            // 常駐タスクを再起動する
            application.startPeriodicWork()
        }
    }

    // ------ //

    fun onCreateActivity(
        activityResultRegistry: ActivityResultRegistry?,
        lifecycle: Lifecycle?
    ) {
        lifecycleObserver = LifecycleObserver(activityResultRegistry!!)
        lifecycle?.addObserver(lifecycleObserver)
    }

    // ------ //

    private var window : Window? = null

    suspend fun onAttachedToWindow(activity: PreferencesActivity) = withContext(Dispatchers.Main) {
        this@PreferencesViewModel.window = activity.window
        showSystemUI(activity)

        // 画面明度のプレビュー
        previewLightLevel.observe(activity) {
            activity.window.attributes = activity.window.attributes.also { lp ->
                lp.screenBrightness =
                    when {
                        // システム設定値(-1.0fよりも小さい値のとき)
                        it == null || it < -1.0f -> -1.0f

                        // バックライト0+さらに暗くする
                        it < .0f -> 0.01f

                        // バックライト使用
                        else -> 0.01f + (1.0f - 0.01f) * it
                    }
            }
        }

        editingLightLevel.observe(activity) {
            when (it) {
                EditingLightLevel.NONE ->
                    observeScreenBrightness(activity, null)

                EditingLightLevel.ON ->
                    observeScreenBrightness(activity, lightLevelOn)

                EditingLightLevel.OFF ->
                    observeScreenBrightness(activity, lightLevelOff)

                else -> {}
            }
        }
    }

    private var previewLightLevelObserver : Observer<Float>? = null

    @MainThread
    private fun observeScreenBrightness(owner: LifecycleOwner, liveData: LiveData<Float>?) {
        previewLightLevelObserver?.let { observer ->
            lightLevelOn.removeObserver(observer)
            lightLevelOff.removeObserver(observer)
        }

        if (liveData == null) {
            previewLightLevel.value = -100.0f
            return
        }

        val observer = Observer<Float> {
            previewLightLevel.value = it
        }
        previewLightLevelObserver = observer

        liveData.observe(owner, observer)
    }

    /** ステータスバーとナビゲーションバーを隠してフルスクリーンにする */
    fun hideSystemUI(activity: PreferencesActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            WindowInsetsControllerCompat(activity.window, activity.binding.root).let { controller ->
                controller.hide(
                    WindowInsetsCompat.Type.systemBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    /**
     * ステータスバーとナビゲーションバーを表示する
     *
     * ステータスバーは背景を透過しフルスクリーンで表示したアクティビティに重ねて表示する
     */
    fun showSystemUI(activity: PreferencesActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            WindowInsetsControllerCompat(activity.window, activity.binding.root).show(
                WindowInsetsCompat.Type.systemBars()
            )
        }
        else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    // ------ //

    /**
     * デフォルト通知表示設定でプレビューを表示する
     */
    fun startPreview() = viewModelScope.launch {
        saveSettings()
        val entity = prefRepo.getDefaultNotificationEntity()
        LockScreenActivity.startPreview(application, entity)
    }

    /**
     * テスト用のダミー通知を発生させる
     */
    fun notifyDummy() {
        application.notifyDummy { lifecycleObserver.launchPermissionRequest() }
    }

    /**
     * 通知を行わない時間帯を設定するダイアログを開く
     */
    private fun openSilentTimezonePickerDialog(
        liveData: MutableLiveData<LocalTime>,
        fragmentManager: FragmentManager
    ) {
        val localTime = liveData.value!!
        val dialog = TimePickerDialogFragment.createInstance(localTime.hour, localTime.minute, true)

        dialog.setOnTimeSetListener { _, value ->
            liveData.value = value
        }

        dialog.show(fragmentManager, null)
    }

    /**
     * 通知を行わない時間帯を設定するダイアログを開く
     */
    fun openSilentTimezoneStartPickerDialog(fragmentManager: FragmentManager) {
        openSilentTimezonePickerDialog(silentTimezoneStart, fragmentManager)
    }

    /**
     * 通知を行わない時間帯を設定するダイアログを開く
     */
    fun openSilentTimezoneEndPickerDialog(fragmentManager: FragmentManager) {
        openSilentTimezonePickerDialog(silentTimezoneEnd, fragmentManager)
    }

    /**
     * 複数通知切替え方法を選択するダイアログを開く
     */
    fun openMultipleNotificationsSolutionSelectionDialog(fragmentManager: FragmentManager) {
        val solutions = MultipleNotificationsSolution.entries
        val labels = solutions.map { it.textId }
        val initialSelected = solutions.indexOf(multipleNotificationsSolution.value)

        AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_multi_notices_solution_selection_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val solution = solutions[which]
                if (multipleNotificationsSolution.value != solution) {
                    multipleNotificationsSolution.value = solution
                }
            }
            .dismissOnClickItem(true)
            .setNegativeButton(R.string.dialog_cancel)
            .create()
            .show(fragmentManager, null)
    }

    /**
     * 設定登録されていない通知の処理方法を選択するダイアログを開く
     */
    fun openUnknownNotificationSolutionSelectionDialog(fragmentManager: FragmentManager) {
        val solutions = UnknownNotificationSolution.entries
        val labels = solutions.map { it.textId }
        val initialSelected = solutions.indexOf(unknownNotificationSolution.value)

        AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_unknown_notice_solution_selection_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                val solution = solutions[which]
                if (unknownNotificationSolution.value != solution) {
                    unknownNotificationSolution.value = solution
                }
            }
            .dismissOnClickItem(true)
            .setNegativeButton(R.string.dialog_cancel)
            .create()
            .show(fragmentManager, null)
    }

    /**
     * 時刻表示形式を選択するダイアログを開く
     */
    fun openClockStyleSelectionDialog(fragmentManager: FragmentManager) {
        val now = LocalDateTime.now()
        val items = ClockStyle.entries
        val labels = items.map { now.format(DateTimeFormatter.ofPattern(it.pattern)) }
        val initialSelected = items.indexOf(clockStyle.value)

         AlertDialogFragment.Builder()
            .setTitle(R.string.prefs_clock_style_desc)
            .setSingleChoiceItems(labels, initialSelected) { _, which ->
                clockStyle.value = items[which]
            }
            .dismissOnClickItem(true)
            .setNegativeButton(R.string.dialog_cancel)
            .create()
            .show(fragmentManager, null)
    }

    // ------ //

    private lateinit var lifecycleObserver : LifecycleObserver

    inner class LifecycleObserver(
        private val registry : ActivityResultRegistry
    ) : DefaultLifecycleObserver {
        private lateinit var requestNotifyPermissionLauncher : ActivityResultLauncher<String>

        override fun onCreate(owner: LifecycleOwner) {
            requestNotifyPermissionLauncher = registry.register(
                "RequestNotifyPermissionLauncher",
                owner,
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    application.notifyDummy()
                }
                else {
                    // todo
                    Toast.makeText(
                        application,
                        application.getString(R.string.denied_notification_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        fun launchPermissionRequest() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotifyPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
