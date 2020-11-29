package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * 画面ON/OFFを検知するレシーバ
 */
class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val lockScreenIntent = Intent(context, LockScreenActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(lockScreenIntent)
                Log.i("WakeLock", "detected screen off")
            }

            Intent.ACTION_SCREEN_ON -> {
                Log.i("WakeLock", "detected screen on")
            }
        }
    }
}
