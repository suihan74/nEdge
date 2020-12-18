package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.MutableLiveData
import com.suihan74.notificationreporter.models.NotificationSetting

/**
 * アプリ設定を扱うリポジトリ
 *
 * TODO: `SharedPreferences`または`DataStore`を扱うようにする
 */
class PreferencesRepository {
    /** 画面消灯までの待機時間(ミリ秒) */
    val lightOffInterval = MutableLiveData(5_000L)

    /**
     * バックライト消灯後の画面をさらに暗くする度合い
     *
     * 0.0f ~ 1.0f
     */
    val lightLevel = MutableLiveData(0.5f)

    /**
     * デフォルトの通知バー描画設定
     */
    val defaultNotificationSetting = MutableLiveData(NotificationSetting())
}
