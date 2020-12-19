package com.suihan74.notificationreporter.scenes.lockScreen

import android.service.notification.StatusBarNotification
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class LockScreenViewModel(
    batteryRepo : BatteryRepository,
    notificationRepo : NotificationRepository,
    prefRepo : PreferencesRepository
) : ViewModel() {

    /** 現在時刻 */
    val currentTime : LiveData<LocalDateTime> by lazy { _currentTime }
    private val _currentTime = MutableLiveData<LocalDateTime>()

    /** デフォルトの通知バーの描画設定 */
    val defaultNotificationSetting = prefRepo.defaultNotificationSetting

    /** バックライト最低レベルまで暗くするか */
    val lightOff : LiveData<Boolean> by lazy { _lightOff }
    private val _lightOff = MutableLiveData(false).also { liveData ->
        liveData.observeForever {
            if (!it) {
                viewModelScope.launch(Dispatchers.Main) {
                    delay(lightOffInterval.value ?: 5_000)
                    liveData.value = true
                }
            }
        }
    }

    /** 画面消灯までの待機時間(ミリ秒) */
    val lightOffInterval : LiveData<Long> = prefRepo.lightOffInterval

    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : LiveData<Float> = prefRepo.lightLevel

    /** バッテリーレベル */
    val batteryLevel : LiveData<Int> = batteryRepo.batteryLevel

    /** 充電状態 */
    val batteryCharging : LiveData<Boolean> = batteryRepo.batteryCharging

    /** 発生した通知リスト */
    val statusBarNotifications : LiveData<List<StatusBarNotification>> = notificationRepo.statusBarNotifications

    // ------ //

    init {
        viewModelScope.launch(Dispatchers.Main) {
            prefRepo.init()

            while (true) {
                val now = LocalDateTime.now()
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
}
