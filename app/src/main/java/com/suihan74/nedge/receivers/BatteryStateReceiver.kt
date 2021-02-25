package com.suihan74.nedge.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.suihan74.nedge.Application
import com.suihan74.nedge.module.BatteryRepositoryQualifier
import com.suihan74.nedge.repositories.BatteryRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 充電状態の変化，バッテリ残量の変化を監視する
 */
class BatteryStateReceiver @Inject constructor(
    @BatteryRepositoryQualifier private val batteryRepository: BatteryRepository
) : BroadcastReceiver() {

    /** システムにレシーバを登録 */
    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        context.registerReceiver(this, IntentFilter(Intent.ACTION_POWER_CONNECTED))
        context.registerReceiver(this, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        Application.instance.let { app -> app.coroutineScope.launch {
            batteryRepository.setBatteryLevel(intent)
        } }
    }
}
