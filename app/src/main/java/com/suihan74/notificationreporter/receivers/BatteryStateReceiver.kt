package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suihan74.notificationreporter.Application
import kotlinx.coroutines.launch

/**
 * 充電状態の変化，バッテリ残量の変化を監視する
 */
class BatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        Application.instance.let { app -> app.coroutineScope.launch {
            app.batteryRepository.setBatteryLevel(intent)
        } }
    }
}
