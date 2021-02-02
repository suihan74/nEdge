package com.suihan74.nedge.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.suihan74.nedge.Application
import kotlinx.coroutines.launch

/**
 * 画面ON/OFFを検知するレシーバ
 */
class ScreenReceiver : BroadcastReceiver() {
    /** システムにレシーバを登録 */
    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_SCREEN_OFF))
        context.registerReceiver(this, IntentFilter(Intent.ACTION_SCREEN_ON))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        setScreenState(Application.instance, intent)
    }

    private fun setScreenState(app: Application, intent: Intent?) = app.coroutineScope.launch {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                app.screenRepository.setScreenState(false)
                Log.i("ScreenReceiver", "detected screen off")
            }

            Intent.ACTION_SCREEN_ON -> {
                app.screenRepository.setScreenState(true)
                Log.i("ScreenReceiver", "detected screen on")
            }
        }
    }
}
