package com.suihan74.notificationreporter.scenes.lockScreen

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.repositories.BatteryRepository
import com.suihan74.notificationreporter.repositories.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

class LockScreenViewModel(
    batteryRepo : BatteryRepository,
    notificationRepo : NotificationRepository
) : ViewModel() {

    /** 現在時刻 */
    val currentTime : LiveData<LocalDateTime> by lazy { _currentTime }
    private val _currentTime = MutableLiveData<LocalDateTime>()

    /** 画面消灯状態 */
    val lightOff : LiveData<Boolean> by lazy { _lightOff }
    private val _lightOff = MutableLiveData<Boolean>()

    /** 画面消灯までの待機時間(ミリ秒) */
    val lightOffInterval : LiveData<Long> by lazy { _lightOffInterval }
    private val _lightOffInterval = MutableLiveData<Long>(5)

    /** 待機開始時刻 */
    private var waitStartTime = LocalDateTime.now()

    /** 通知発生 */
    val existNotifications : LiveData<Boolean> = notificationRepo.existUnreadNotifications

    /** バッテリーレベル */
    val batteryLevel : LiveData<Int> = batteryRepo.batteryLevel

    /** 充電状態 */
    val batteryCharging : LiveData<Boolean> = batteryRepo.batteryCharging

    // ------ //

    init {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                val now = LocalDateTime.now()
                _currentTime.value = now

                viewModelScope.launch(Dispatchers.Default) {
                    if (_lightOff.value != true) {
                        val interval = lightOffInterval.value ?: 0
                        val duration =
                            now.toEpochSecond(ZoneOffset.UTC) - waitStartTime.toEpochSecond(
                                ZoneOffset.UTC
                            )
                        if (duration >= interval) {
                            _lightOff.postValue(true)
                        }
                    }
                }

                delay(
                    (1_000_000_000L - now.nano) / 1_000_000L
                )
            }
        }
    }

    @MainThread
    fun onClickScreen() {
        _lightOff.value = false
        waitStartTime = LocalDateTime.now()
    }
}
