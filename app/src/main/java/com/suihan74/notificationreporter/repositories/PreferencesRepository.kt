package com.suihan74.notificationreporter.repositories

import androidx.datastore.core.DataStore
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotificationSetting
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
        private const val DEFAULT_SETTING_NAME = NotificationEntity.DEFAULT_SETTING_NAME
    }

    // ------ //

    /**
     * 設定をDBに保存する
     */
    suspend fun updateNotificationSetting(appName: String, setting: NotificationSetting) {
        notificationDao.insert(NotificationEntity(appName, setting))
    }

    /**
     * 対象アプリ用の通知表示設定を取得する
     *
     * @return `appName`に対応する設定か、見つからなければデフォルト設定
     */
    suspend fun getNotificationSetting(appName: String = DEFAULT_SETTING_NAME) : NotificationSetting {
        return getNotificationSettingOrNull(appName) ?: notificationDao.getDefaultSetting()
    }

    /**
     * 対象アプリ用の通知表示設定を取得する
     *
     * @return `appName`に対応する設定か、見つからなければnull
     */
    suspend fun getNotificationSettingOrNull(appName: String = DEFAULT_SETTING_NAME) : NotificationSetting? {
        return notificationDao.findByAppName(appName)?.setting
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
