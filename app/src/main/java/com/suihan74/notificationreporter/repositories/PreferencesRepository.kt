package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.utilities.dataStore.WrappedDataStore
import kotlinx.coroutines.CoroutineScope

/**
 * アプリ設定を扱うリポジトリ
 */
class PreferencesRepository(
    private val dataStore: WrappedDataStore<PreferencesKey<*>>,
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
    suspend fun <T> getPreference(key: PreferencesKey<T>) : T {
        return dataStore.get(key)
    }

    /**
     * アプリ設定値の`LiveData`を取得する
     */
    fun <T> getLiveData(key: PreferencesKey<T>, coroutineScope: CoroutineScope) : LiveData<T> {
        return dataStore.getLiveData(key, coroutineScope)
    }

    /**
     * アプリ設定値の`MutableLiveData`を取得する
     */
    fun <T> getMutableLiveData(key: PreferencesKey<T>, coroutineScope: CoroutineScope) : MutableLiveData<T> {
        return dataStore.getMutableLiveData(key, coroutineScope)
    }
}
