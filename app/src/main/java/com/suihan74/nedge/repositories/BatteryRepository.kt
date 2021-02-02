package com.suihan74.nedge.repositories

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * バッテリに関する情報を扱うリポジトリ
 */
class BatteryRepository {
    /** 充電中かどうか */
    val batteryCharging = MutableLiveData<Boolean>()

    /** バッテリー残量(%) */
    val batteryLevel = MutableLiveData<Int>()

    // ------ //

    /**
     * バッテリ残量を読み込む
     *
     * `BatteryStateReceiver`で得られる`Intent`から情報を得るパターン
     */
    suspend fun setBatteryLevel(intent: Intent) = withContext(Dispatchers.Main) {
        val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (rawLevel != -1 && scale != -1 && scale != 0) {
            val level = (rawLevel * 100 / scale.toFloat()).toInt()
            batteryLevel.value = level
        }

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                batteryCharging.value = true
                Log.i("Power", "connected")
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                batteryCharging.value = false
                Log.i("Power", "disconnected")
            }

            else -> {}
        }
    }

    /**
     * バッテリ残量を読み込む
     *
     * 即時にバッテリ残量と充電状態の現在値を取得するパターン
     */
    suspend fun setBatterLevel(context: Context) = withContext(Dispatchers.Main) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
            context.registerReceiver(null, it)
        }

        batteryStatus?.let {
            setBatteryLevel(it)

            // 充電状態
            batteryCharging.value =
                when (it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                    BatteryManager.BATTERY_PLUGGED_AC,
                    BatteryManager.BATTERY_PLUGGED_USB,
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> true
                    else -> false
                }
        }
    }
}
