package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.lifecycle.MutableLiveData
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.models.UnknownNotificationSolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 通知を扱うリポジトリ
 */
class NotificationRepository {
    /** 通知スタック */
    val statusBarNotifications = MutableLiveData<List<StatusBarNotification>>()

    // ------ //

    /** 新しく発生した通知を記録する */
    suspend fun pushNotification(
        sbn: StatusBarNotification?,
        prefRepo: PreferencesRepository
    ) = withContext(Dispatchers.Default) {
        if (validateNotification(sbn, prefRepo)) {
            val oldList = statusBarNotifications.value.orEmpty()
            statusBarNotifications.postValue(oldList.plus(sbn!!))
        }
    }

    /** ステータスバーから削除された通知をリポジトリに反映させる */
    suspend fun removeNotification(sbn: StatusBarNotification?) = withContext(Dispatchers.Default) {
        if (sbn?.notification == null) return@withContext
        val oldList = statusBarNotifications.value.orEmpty()
        statusBarNotifications.postValue(
            oldList.filter {
                it.packageName == sbn.packageName && it.id == sbn.id
            }
        )
    }

    /** 記録した通知をクリアする */
    suspend fun clearNotifications() = withContext(Dispatchers.Default) {
        statusBarNotifications.postValue(emptyList())
    }

    // ------ //

    /** このアプリで扱う通知かを判別する */
    suspend fun validateNotification(
        sbn: StatusBarNotification?,
        prefRepo: PreferencesRepository,
        prefs: Preferences? = null
    ) : Boolean {
        if (sbn?.notification == null) return false

        // 無視する通知
        when ((prefs ?: prefRepo.preferences()).unknownNotificationSolution) {
            UnknownNotificationSolution.IGNORE ->
                if (null == prefRepo.getNotificationEntityOrNull(sbn)) {
                    return false
                }

            else -> {}
        }

        // ブラックリスト設定に含まれるか確認
        if (prefRepo.isBlackListed(sbn)) {
            return false
        }

        return true
    }
}
