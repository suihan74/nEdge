package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.MutableLiveData

/**
 * 通知を扱うリポジトリ
 */
class NotificationRepository {
    /** 未読の通知が存在する */
    val existUnreadNotifications = MutableLiveData<Boolean>()
}
