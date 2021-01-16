package com.suihan74.notificationreporter.scenes.lockScreen

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.lifecycle.*
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.MultipleNotificationsSolution
import com.suihan74.notificationreporter.outline.OutlineDrawer
import com.suihan74.utilities.extensions.between
import kotlinx.coroutines.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import kotlin.random.Random

class LockScreenViewModel(
    private val application: Application
) : ViewModel() {

    enum class Extra {
        /** プレビューする設定ID */
        PREVIEW_ENTITY_ID
    }

    // ------ //

    private val batteryRepo = application.batteryRepository

    private val notificationRepo = application.notificationRepository

    private val prefRepo = application.preferencesRepository

    // ------ //

    /** 現在時刻 */
    val currentTime : LiveData<LocalDateTime> by lazy { _currentTime }
    private val _currentTime = MutableLiveData<LocalDateTime>()

    /** 現在通知に対応する設定 */
    val notificationEntity : LiveData<NotificationEntity> by lazy { _notificationEntity }
    private val _notificationEntity = MutableLiveData<NotificationEntity>()

    /** 表示しない開始時刻 */
    private var silentTimezoneStart : LocalTime? = null

    /** 表示しない終了時刻 */
    private var silentTimezoneEnd : LocalTime? = null

    /** バックライト最低レベルまで暗くするか */
    val lightOff : LiveData<Boolean> by lazy { _lightOff }
    private val _lightOff = MutableLiveData<Boolean>().also { liveData ->
        liveData.observeForever {
            // 待機時間が0に設定されている場合は常に明るくする
            if (!it && lightOffInterval > 0L) {
                viewModelScope.launch(Dispatchers.Main) {
                    delay(lightOffInterval)
                    liveData.value = true
                }
            }
        }
    }

    /** 画面消灯までの待機時間(ミリ秒) */
    private var lightOffInterval : Long = 5_000L

    /** 画面起動直後の画面の明るさ */
    private val lightLevelOn : LiveData<Float> by lazy { _lightLevelOn }
    private val _lightLevelOn = MutableLiveData<Float>()

    /** バックライト消灯後の画面の明るさ */
    val lightLevelOff : LiveData<Float> by lazy { _lightLevelOff }
    private val _lightLevelOff = MutableLiveData<Float>()

    /** 画面起動直後の画面の明るさをシステム設定値にする */
    private val useSystemLightLevelOn : LiveData<Boolean> by lazy { _useSystemLightLevelOn }
    private val _useSystemLightLevelOn = MutableLiveData<Boolean>()

    /** バッテリーレベル */
    val batteryLevel : LiveData<Int> = batteryRepo.batteryLevel

    /** 充電状態 */
    val batteryCharging : LiveData<Boolean> = batteryRepo.batteryCharging

    /** 複数通知の処理方法 */
    private lateinit var multipleNotificationsSolution : MultipleNotificationsSolution

    /** 複数通知を切り替える待機時間 */
    private var switchNotificationsDuration : Long = 5_000L

    /** 発生した通知リスト */
    private val statusBarNotifications : LiveData<List<StatusBarNotification>> =
        notificationRepo.statusBarNotifications

    /** 画面に表示中の通知 */
    val currentNotice = MutableLiveData<StatusBarNotification?>()

    // ------ //

    fun init(lifecycleOwner: LifecycleOwner, intent: Intent) {
        var switchNoticeJob : Job? = null

        currentNotice.observe(lifecycleOwner, Observer {
            if (it == null) return@Observer
            viewModelScope.launch(Dispatchers.Main.immediate) {
                _notificationEntity.value = prefRepo.getNotificationEntityOrDefault(it)
            }
        })

        statusBarNotifications.observe(lifecycleOwner, Observer {
            viewModelScope.launch {
                switchNoticeJob?.cancelAndJoin()
                switchNoticeJob = launchNotificationsSwitching()
            }
        })

        viewModelScope.launch(Dispatchers.Main.immediate) {
            prefRepo.preferences().let { prefs ->
                _lightLevelOn.value = prefs.lightLevelOn
                _lightLevelOff.value = prefs.lightLevelOff
                lightOffInterval = prefs.lightOffInterval
                _useSystemLightLevelOn.value = prefs.useSystemLightLevelOn
                _lightOff.value = false
                multipleNotificationsSolution = prefs.multipleNotificationsSolution
                switchNotificationsDuration = prefs.switchNotificationsDuration
                silentTimezoneStart = prefs.silentTimezoneStart
                silentTimezoneEnd = prefs.silentTimezoneEnd
            }

            // 時刻更新開始
            launchClockUpdating()
        }

        // プレビューとして開始
        val previewEntityId = intent.getLongExtra(Extra.PREVIEW_ENTITY_ID.name, -1L)
        if (previewEntityId != -1L) {
            viewModelScope.launch(Dispatchers.Main.immediate) {
                prefRepo.getNotificationEntityOrNull(previewEntityId)?.let {
                    _notificationEntity.value = it
                }
            }
        }
    }

    /** アクティビティがウィンドウにアタッチされている必要がある初期化処理 */
    fun onAttachedToWindow(owner: LifecycleOwner, window: Window, edgeImageView: ImageView) {
        // バックライトの制御
        observeScreenBrightness(owner, window)

        // 輪郭線の描画
        val outlineDrawer = OutlineDrawer(window)
        notificationEntity.observe(owner, Observer {
            outlineDrawer.draw(edgeImageView, it.setting)
        })
    }

    /** 強制的にロックして消灯する */
    private fun sleep() {
        try {
            val dpm =
                application.getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.lockNow()
        }
        catch (e: Throwable) {
            Log.e("DevicePolicyManager", Log.getStackTraceString(e))
        }
    }

    /** アクティビティから抜ける際に通知スタックをクリア */
    fun onFinishActivity() = GlobalScope.launch {
        notificationRepo.clearNotifications()
    }

    // ------ //

    /** 複数通知の表示切替え */
    private fun launchNotificationsSwitching() = viewModelScope.launch {
        when (multipleNotificationsSolution) {
            MultipleNotificationsSolution.LATEST ->
                showLatestNotification()

            MultipleNotificationsSolution.SWITCH_IN_ORDER ->
                switchNotificationsInOrder()

            MultipleNotificationsSolution.SWITCH_RANDOMLY ->
                switchNotificationsRandomly()
        }
    }

    /** 最後に受け取った通知を表示する */
    private suspend fun showLatestNotification() = withContext(Dispatchers.Default) {
        statusBarNotifications.value?.lastOrNull()?.let {
            currentNotice.postValue(it)
        }
    }

    /** 複数通知を新着から順番に切り替える */
    private suspend fun switchNotificationsInOrder() = withContext(Dispatchers.Default) {
        showLatestNotification()

        while (true) {
            delay(switchNotificationsDuration)

            statusBarNotifications.value?.let { n ->
                if (n.size > 1) {
                    val currentIdx = n.indexOf(currentNotice.value)
                    val nextIdx =
                        if (currentIdx <= 0) n.size - 1
                        else currentIdx - 1

                    currentNotice.postValue(n[nextIdx])
                }
            }
        }
    }

    /** 複数通知をランダムに切り替える(新しい通知取得直後は必ずその新着をはじめに表示する) */
    private suspend fun switchNotificationsRandomly() = withContext(Dispatchers.Default) {
        showLatestNotification()

        while (true) {
            delay(switchNotificationsDuration)

            statusBarNotifications.value?.let { n ->
                if (n.size > 1) {
                    val nextIdx = getNextIdxExcludeCurrent(
                        until = n.size,
                        n.indexOf(currentNotice.value)
                    )
                    currentNotice.postValue(n[nextIdx])
                }
            }
        }
    }

    private fun getNextIdxExcludeCurrent(until: Int, current: Int) : Int {
        if (until == 2 && current >= 0) return current xor 1
        while (true) {
            val next = Random.nextInt(until)
            if (next != current) return next
        }
    }

    /** 時計の更新処理 */
    private fun launchClockUpdating() = viewModelScope.launch(Dispatchers.Main) {
        while (true) {
            LocalDateTime.now().let { now ->
                _currentTime.value = now

                if (now.toLocalTime().between(silentTimezoneStart!!, silentTimezoneEnd!!)) {
                    sleep()
                }

                // 1分間隔で時計を更新する
                delay(
                    60_000L - (now.second * 1_000L + now.nano / 1_000_000L)
                )
            }
        }
    }

    // ------ //

    /** 画面タッチでライトオン状態にする */
    val onTouchScreen : (View, MotionEvent)->Boolean = { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                _lightOff.value = false
            }
        }
        true
    }

    // ------ //

    private fun observeScreenBrightness(owner: LifecycleOwner, window: Window) {
        lightOff.observe(owner, Observer { lightOff ->
            window.attributes = window.attributes.also { lp ->
                lp.screenBrightness =
                    when {
                        // 消灯時
                        lightOff -> calcBrightness(lightLevelOff.value)

                        // 点灯時システム設定値の明るさを使用
                        useSystemLightLevelOn.value == true -> calcBrightness(null)

                        // 点灯時
                        else -> calcBrightness(lightLevelOn.value)
                    }
            }
        })

        // 新たな通知を受け取ったら画面を点灯する
        statusBarNotifications.observe(owner, Observer {
            _lightOff.value = false
        })
    }

    private fun calcBrightness(value: Float?) : Float {
        return when {
            // システム設定値(-1.0fよりも小さい値のとき)
            value == null || value < -1.0f -> -1.0f

            // バックライト0+さらに暗くする
            value < .0f -> 0.01f

            // バックライト使用
            else -> 0.01f + (1.0f - 0.01f) * value
        }
    }

    // ------ //

    companion object {
        /**
         * `LockScreenActivity`に遷移するべきかをチェックする
         *
         * 遷移を拒否する条件
         * - 通知が不正(=null)
         * - 画面が点灯中
         * - バッテリレベルが指定値未満
         * - 通知を行わない時間帯
         *
         * @return 遷移できる: true, 遷移できない: false
         */
        suspend fun checkNotifiable(sbn: StatusBarNotification?) : Boolean {
            val app = Application.instance
            val prefRepo = app.preferencesRepository
            val batteryRepo = app.batteryRepository
            val screenRepo = app.screenRepository
            val notificationRepo = app.notificationRepository

            // 通知が不正, 画面が点いている
            if (sbn?.notification == null || screenRepo.screenOn.value == true) {
                return false
            }

            val prefs = prefRepo.preferences()

            // バッテリレベルが指定値未満
            val batteryLevel = batteryRepo.batteryLevel.value ?: 0
            val requiredBatteryLevel = prefs.requiredBatteryLevel
            if (batteryLevel < requiredBatteryLevel && batteryRepo.batteryCharging.value != true) {
                return false
            }

            // 通知を行わない時間帯
            if (LocalTime.now().between(prefs.silentTimezoneStart, prefs.silentTimezoneEnd)) {
                return false
            }

            // 無視する通知
            return notificationRepo.validateNotification(sbn, prefRepo, prefs)
        }
    }
}
