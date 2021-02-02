package com.suihan74.nedge.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.suihan74.nedge.Application
import kotlinx.coroutines.launch

/**
 * 充電状態の変化，バッテリ残量の変化を監視する
 */
class BatteryStateReceiver : BroadcastReceiver() {
    /** システムにレシーバを登録 */
    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        context.registerReceiver(this, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        context.registerReceiver(this, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        Application.instance.let { app -> app.coroutineScope.launch {
            app.batteryRepository.setBatteryLevel(intent)
        } }
    }
}
