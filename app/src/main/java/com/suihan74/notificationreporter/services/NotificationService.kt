package com.suihan74.notificationreporter.services

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val repository = Application.instance.notificationRepository
        repository.existUnreadNotifications.value = sbn != null

        val intent = Intent(applicationContext, LockScreenActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        applicationContext.startActivity(intent)

        Log.i("Notification", "notification posted")
    }
}
