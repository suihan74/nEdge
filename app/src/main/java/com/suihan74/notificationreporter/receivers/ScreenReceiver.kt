package com.suihan74.notificationreporter.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * 画面ON/OFFを検知するレシーバ
 */
class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val lockScreenIntent = Intent().also {
                    it.setClassName(
                        "com.suihan74.notificationreporter",
                        "com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity"
                    )
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
