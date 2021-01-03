package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.KeywordMatchingType
import com.suihan74.notificationreporter.models.NotificationSetting
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
    companion object {
        private const val DEFAULT_SETTING_NAME = NotificationDao.DEFAULT_SETTING_NAME
    }

    // ------ //

    /**
     * 設定をDBに保存する
     */
    suspend fun updateNotificationSetting(
        appName: String,
        keyword: String = "",
        keywordMatchingType: KeywordMatchingType = KeywordMatchingType.NONE,
        setting: NotificationSetting = NotificationSetting()
    ) {
        NotificationEntity(
            appName = appName,
            keyword = keyword,
            keywordMatchingType = keywordMatchingType,
            setting = setting
        ).let {
            notificationDao.insert(it)
        }
    }

    suspend fun getNotificationSettingOrNull(sbn: StatusBarNotification) : NotificationSetting? {
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
            ?.setting
    }

    suspend fun getNotificationSettingOrDefault(sbn: StatusBarNotification) : NotificationSetting =
        getNotificationSettingOrNull(sbn) ?: notificationDao.getDefaultSetting()

    suspend fun getNotificationSettingOrNull(
        appName: String,
        keyword: String = "",
        keywordMatchingType: KeywordMatchingType = KeywordMatchingType.NONE
    ) : NotificationSetting? {
        return notificationDao.findByAppName(appName)
            .firstOrNull { it.keyword == keyword && it.keywordMatchingType == keywordMatchingType }
            ?.setting
    }

    suspend fun getNotificationSettingOrDefault(
        appName: String,
        keyword: String = "",
        keywordMatchingType: KeywordMatchingType = KeywordMatchingType.NONE
    ) : NotificationSetting {
        return notificationDao.findByAppName(appName)
            .firstOrNull { it.keyword == keyword && it.keywordMatchingType == keywordMatchingType }
            ?.setting
            ?: notificationDao.getDefaultSetting()
    }

    /**
     * アプリ設定値を取得する
     */
    suspend fun getPreferences() : Preferences {
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
