package com.suihan74.notificationreporter.repositories

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData

/**
 * バッテリに関する情報を扱うリポジトリ
 */
class BatteryRepository {
    /** 充電中かどうか */
    var batteryCharging = MutableLiveData<Boolean>()

    /** バッテリー残量(%) */
    var batteryLevel = MutableLiveData<Int>()

    // ------ //

    /** バッテリ残量を読み込む */
    @MainThread
    fun setBatteryLevel(intent: Intent) {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (rawLevel != -1 && scale != -1) {
            val level = (rawLevel * 100 / scale.toFloat()).toInt()
            batteryLevel.value = level
        }
    }

    /** バッテリ残量を読み込む */
    @MainThread
    fun setBatterLevel(context: Context) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
            context.registerReceiver(null, it)
        }

        batteryStatus?.let {
            setBatteryLevel(it)

            // 充電状態
            val chargePlug = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            batteryCharging.value =
                chargePlug == BatteryManager.BATTERY_PLUGGED_AC or
                        BatteryManager.BATTERY_PLUGGED_USB or
                        BatteryManager.BATTERY_PLUGGED_WIRELESS
        }
    }
}
