package com.suihan74.notificationreporter.services

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.MainThread
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    @MainThread
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val app = Application.instance

        app.notificationRepository.existUnreadNotifications.value = sbn != null

        if (app.screenRepository.screenOn.value == false) {
            val intent = Intent(applicationContext, LockScreenActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            applicationContext.startActivity(intent)
        }

        Log.i("Notification", "notification posted")
    }
}
