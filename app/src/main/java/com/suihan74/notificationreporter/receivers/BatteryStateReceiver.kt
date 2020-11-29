package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import com.suihan74.notificationreporter.Application

/**
 * 充電状態の変化，バッテリ残量の変化を監視する
 */
class BatteryStateReceiver : BroadcastReceiver() {
    @MainThread
    override fun onReceive(context: Context?, intent: Intent?) {
        val repository = Application.instance.batteryRepository
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                repository.batteryCharging.value = true
                repository.setBatteryLevel(intent)
                Log.i("Power", "connected")
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                repository.batteryCharging.value = false
                repository.setBatteryLevel(intent)
                Log.i("Power", "disconnected")
            }

            Intent.ACTION_BATTERY_CHANGED -> {
                repository.setBatteryLevel(intent)
            }

        }
    }
}
