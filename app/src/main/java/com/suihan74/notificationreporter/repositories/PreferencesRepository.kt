package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.KeywordMatchingType
import com.suihan74.utilities.extensions.contains
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * アプリ設定を扱うリポジトリ
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val notificationDao: NotificationDao
) {
    /**
     * 設定をDBに保存する
     */
    suspend fun updateNotificationEntity(entity: NotificationEntity) {
        notificationDao.insert(entity)
    }

    suspend fun deleteNotificationEntity(entity: NotificationEntity) {
        notificationDao.delete(entity)
    }

    // ------ //

    /**
     * すべての通知表示設定を取得する
     */
    val allNotificationSettingsFlow : Flow<List<NotificationEntity>>
        get() = notificationDao.getAllSettingsFlow()

    // ------ //

    suspend fun getDefaultNotificationEntity() : NotificationEntity {
        return notificationDao.getDefaultEntity()
    }

    suspend fun getNotificationEntityOrNull(id: Long) : NotificationEntity? {
        return notificationDao.findById(id)
    }

    suspend fun getNotificationEntityOrNull(sbn: StatusBarNotification) : NotificationEntity? {
        return notificationDao.findByAppName(sbn.packageName)
            .sortedByDescending { it.keywordMatchingType.importance }
            .firstOrNull {
                when (it.keywordMatchingType) {
                    KeywordMatchingType.NONE -> true

                    KeywordMatchingType.INCLUDE ->
                        sbn.notification?.contains(it.keyword) == true

                    KeywordMatchingType.EXCLUDE ->
                        sbn.notification?.contains(it.keyword) == false
                }
            }
    }

    suspend fun getNotificationEntityOrDefault(sbn: StatusBarNotification) : NotificationEntity {
        return getNotificationEntityOrNull(sbn) ?: notificationDao.getDefaultEntity()
    }

    // ------ //

    /**
     * アプリ設定値を取得する
     */
    suspend fun preferences() : Preferences {
        return dataStore.data.firstOrNull() ?: Preferences()
    }

    /**
     * アプリ設定値を受け取る`Flow`を取得する
     */
    val preferencesFlow : Flow<Preferences>
        get() = dataStore.data

    /**
     * アプリ設定値を更新する
     */
    suspend fun updatePreferences(transform: suspend (Preferences)->Preferences) {
        dataStore.updateData(transform)
    }
}
