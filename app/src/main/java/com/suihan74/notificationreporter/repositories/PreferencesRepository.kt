package com.suihan74.notificationreporter.repositories

import com.suihan74.notificationreporter.dataStore.PreferencesKey
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.utilities.dataStore.WrappedDataStore

/**
 * アプリ設定を扱うリポジトリ
 *
 * TODO: `SharedPreferences`または`DataStore`を扱うようにする
 */
class PreferencesRepository(
    dataStore: WrappedDataStore<PreferencesKey<*>>,
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val DEFAULT_SETTING_NAME = NotificationEntity.DEFAULT_SETTING_NAME
    }

    // ------ //

    /** 画面消灯までの待機時間(ミリ秒) */
    val lightOffInterval = dataStore.getLiveData(PreferencesKey.LIGHT_OFF_INTERVAL)

    /**
     * バックライト消灯後の画面をさらに暗くする度合い
     *
     * 0.0f ~ 1.0f
     */
    val lightLevel = dataStore.getLiveData(PreferencesKey.LIGHT_LEVEL)

    /** 通知しない時間帯(開始時刻) */
    val disableTimeStart = dataStore.getLiveData<Int>(PreferencesKey.DISABLE_TIME_START)

    /** 通知しない時間帯(終了時刻) */
    val disableTimeEnd = dataStore.getLiveData<Int>(PreferencesKey.DISABLE_TIME_END)

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
     * @return `appName`に対応する設定か、それが無ければデフォルト設定
     */
    suspend fun getNotificationSetting(appName: String = DEFAULT_SETTING_NAME) : NotificationSetting {
        return notificationDao.findByAppName(appName)?.setting ?: notificationDao.getDefaultSetting()
    }
}
