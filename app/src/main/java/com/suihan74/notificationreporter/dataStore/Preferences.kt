package com.suihan74.notificationreporter.dataStore

import com.suihan74.notificationreporter.models.MultipleNotificationsSolution
import com.suihan74.utilities.serialization.LocalTimeSerializer
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalTime

@Serializable
data class Preferences(
    /** 設定バージョン */
    val version: Int = 1,

    /**
     * 画面を暗くした後の時のライトレベル
     *
     * -1.0f ~ 1.0f
     * マイナス値で黒前景を表示してシステムで設定可能なライトレベル未満にする
     */
    val lightLevelOff : Float = 0f,

    /**
     * アプリが点いてすぐ明るいときのライトレベル
     *
     * 0.0f ~ 1.0f
     * システムで設定可能なライトレベル範囲
     */
    val lightLevelOn : Float = 0.5f,

    /**
     * アプリが点いてすぐのライトレベルをシステムの値にする
     */
    val useSystemLightLevelOn : Boolean = false,

    /** 消灯までの待機時間(ミリ秒) */
    val lightOffInterval : Long = 5_000L,

    /** 通知を表示しない時間帯(開始時刻) */
    @Serializable(with = LocalTimeSerializer::class)
    val silentTimezoneStart : LocalTime = LocalTime.of(0, 0),

    /** 通知を表示しない時間帯(終了時刻) */
    @Serializable(with = LocalTimeSerializer::class)
    val silentTimezoneEnd : LocalTime = LocalTime.of(7, 0),

    /** 指定値未満のバッテリレベルでは通知を表示しない */
    val requiredBatteryLevel : Int = 15,

    /** 複数通知がある場合の対処方法 */
    val multipleNotificationsSolution : MultipleNotificationsSolution = MultipleNotificationsSolution.SWITCH_IN_ORDER,

    /** 複数通知を切り替えるまでの待機時間(ミリ秒) */
    val switchNotificationsDuration : Long = 5_000L
)
