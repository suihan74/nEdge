package com.suihan74.notificationreporter.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.suihan74.notificationreporter.Application

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val repository = Application.instance.notificationRepository
        repository.existUnreadNotifications.value = sbn != null
    }
}
