package com.suihan74.notificationreporter.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * デバイスのすべての通知発生を検知するサービス
 */
class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }
}
