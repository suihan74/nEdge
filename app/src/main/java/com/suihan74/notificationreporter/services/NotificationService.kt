package com.suihan74.notificationreporter.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.suihan74.notificationreporter.Application
import com.suihan74.notificationreporter.scenes.lockScreen.LockScreenActivity
import kotlinx.coroutines.launch

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        Application.instance.let { app -> app.coroutineScope.launch {
            app.notificationRepository.pushNotification(sbn, app.preferencesRepository)
            LockScreenActivity.startWhenAvailable(applicationContext, sbn)
        } }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        Application.instance.let { app -> app.coroutineScope.launch {
            app.notificationRepository.removeNotification(sbn)
        } }
    }
}
