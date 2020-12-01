package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData

/**
 * 通知を扱うリポジトリ
 */
class NotificationRepository {
    /** 通知スタック */
    val statusBarNotifications = MutableLiveData<List<StatusBarNotification>>()

    /** 直近で通知が発生したか */
    val existNotifications : Boolean
        get() = statusBarNotifications.value.isNullOrEmpty().not()

    // ------ //

    /** 新しく発生した通知を記録する */
    @MainThread
    fun pushNotification(sbn: StatusBarNotification?) {
        if (sbn?.notification == null) return
        val oldList = statusBarNotifications.value.orEmpty()
        statusBarNotifications.value = oldList.plus(sbn)
    }

    /** 記録した通知をクリアする */
    @MainThread
    fun clearNotifications() {
        statusBarNotifications.value = emptyList()
    }
}
