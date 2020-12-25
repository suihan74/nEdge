package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    suspend fun pushNotification(sbn: StatusBarNotification?) = withContext(Dispatchers.Default) {
        if (sbn?.notification != null) {
            val oldList = statusBarNotifications.value.orEmpty()
            statusBarNotifications.postValue(oldList.plus(sbn))
        }
    }

    /** 記録した通知をクリアする */
    suspend fun clearNotifications() = withContext(Dispatchers.Default) {
        statusBarNotifications.postValue(emptyList())
    }
}
