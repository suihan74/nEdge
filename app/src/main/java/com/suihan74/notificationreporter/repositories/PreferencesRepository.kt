package com.suihan74.notificationreporter.repositories

import androidx.lifecycle.MutableLiveData

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
}
