package com.suihan74.notificationreporter.scenes.lockScreen

import android.service.notification.StatusBarNotification
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.lifecycle.*
import com.suihan74.notificationreporter.models.MultipleNotificationsSolution
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import kotlinx.coroutines.*
import org.threeten.bp.LocalDateTime
import kotlin.random.Random

class LockScreenViewModel(
    batteryRepo : BatteryRepository,
    notificationRepo : NotificationRepository,
    private val prefRepo : PreferencesRepository
) : ViewModel() {

    /** 現在時刻 */
    val currentTime : LiveData<LocalDateTime> by lazy { _currentTime }
    private val _currentTime = MutableLiveData<LocalDateTime>()

    /** 通知バーの描画設定 */
    val notificationSetting by lazy { _notificationSetting }
    private val _notificationSetting = MutableLiveData<NotificationSetting>()

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
    private val _lightLevelOn = MutableLiveData<Float>()
    private val lightLevelOn : LiveData<Float> by lazy { _lightLevelOn }

    /** バックライト消灯後の画面の明るさ */
    private val _lightLevelOff = MutableLiveData<Float>()
    val lightLevelOff : LiveData<Float> by lazy { _lightLevelOff }

    /** 画面起動直後の画面の明るさをシステム設定値にする */
    private val _useSystemLightLevelOn = MutableLiveData<Boolean>()
    private val useSystemLightLevelOn : LiveData<Boolean> by lazy { _useSystemLightLevelOn }

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

    fun init(lifecycleOwner: LifecycleOwner) {
        var switchNoticeJob : Job? = null

        currentNotice.observe(lifecycleOwner, {
            if (it == null) return@observe
            viewModelScope.launch(Dispatchers.Main.immediate) {
                _notificationSetting.value = prefRepo.getNotificationSettingOrDefault(it)
            }
        })

        statusBarNotifications.observe(lifecycleOwner, {
            viewModelScope.launch {
                switchNoticeJob?.cancelAndJoin()
                switchNoticeJob = launchNotificationsSwitching()
            }
        })

        viewModelScope.launch(Dispatchers.Main.immediate) {
            prefRepo.getPreferences().let { prefs ->
                _lightLevelOn.value = prefs.lightLevelOn
                _lightLevelOff.value = prefs.lightLevelOff
                lightOffInterval = prefs.lightOffInterval
                _useSystemLightLevelOn.value = prefs.useSystemLightLevelOn
                _lightOff.value = false
                multipleNotificationsSolution = prefs.multipleNotificationsSolution
                switchNotificationsDuration = prefs.switchNotificationsDuration
            }

            // 時刻更新開始
            launchClockUpdating()
        }
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

                // 1分間隔で時計を更新する
                delay(
                    60_000L - (now.second * 1_000L + now.nano / 1_000_000L)
                )
            }
        }
    }

    // ------ //

    val onTouchScreen : (View, MotionEvent)->Boolean = { _, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                _lightOff.value = false
            }
        }
        true
    }

    // ------ //

    fun observeScreenBrightness(owner: LifecycleOwner, window: Window) {
        lightOff.observe(owner, { lightOff ->
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
}
