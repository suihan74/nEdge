package com.suihan74.notificationreporter.repositories

import android.graphics.Color
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

    /**
     * 通知バーの角丸のスケール
     *
     * 左上から時計回りに(X_rad, Y_rad)のペアを4角分設定する
     */
    val screenCornerRadii = MutableLiveData(floatArrayOf(120.0f, 120.0f, 120.0f, 120.0f, 56.0f, 56.0f, 56.0f, 56.0f))

    /**
     * デフォルトの通知バー色
     */
    val defaultNotificationColor = MutableLiveData(Color.GREEN)
}
