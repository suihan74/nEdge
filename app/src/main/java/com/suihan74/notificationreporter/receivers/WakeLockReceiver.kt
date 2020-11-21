package com.suihan74.notificationreporter.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * 画面ON/OFFを検知するレシーバ
 */
class WakeLockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("WakeLock", "detected screen off")
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val app = Application.instance

                val lockScreenIntent = Intent(app, LockScreenActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                app.startActivity(lockScreenIntent)
            }
        }
    }
}
