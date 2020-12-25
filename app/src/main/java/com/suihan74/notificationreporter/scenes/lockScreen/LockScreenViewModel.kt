package com.suihan74.notificationreporter.scenes.lockScreen

import android.service.notification.StatusBarNotification
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.*
import com.suihan74.notificationreporter.models.NotificationSetting
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
    private val lightOffInterval : LiveData<Long> = prefRepo.lightOffInterval

    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : LiveData<Float> = prefRepo.lightLevel

    /** バッテリーレベル */
    val batteryLevel : LiveData<Int> = batteryRepo.batteryLevel

    /** 充電状態 */
    val batteryCharging : LiveData<Boolean> = batteryRepo.batteryCharging

    /** 発生した通知リスト */
    val statusBarNotifications : LiveData<List<StatusBarNotification>> = notificationRepo.statusBarNotifications

    val currentNotice = MutableLiveData<StatusBarNotification?>()

    // ------ //

    fun init(lifecycleOwner: LifecycleOwner) {
        statusBarNotifications.observe(lifecycleOwner, {
            val item = it.lastOrNull() ?: return@observe
            viewModelScope.launch(Dispatchers.Main) {
                currentNotice.value = item
                _notificationSetting.value = prefRepo.getNotificationSetting(item.packageName)
            }
        })

        viewModelScope.launch(Dispatchers.Main) {
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
