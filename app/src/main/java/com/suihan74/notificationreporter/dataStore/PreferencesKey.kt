package com.suihan74.notificationreporter.dataStore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import com.suihan74.utilities.dataStore.DataStoreKey
import com.suihan74.utilities.dataStore.Serializer
import com.suihan74.utilities.dataStore.WrappedDataStore
import org.threeten.bp.LocalTime

/** アプリ設定の項目 */
@DataStoreKey("settings", version = 1)
class PreferencesKey<T> private constructor(
    key: Preferences.Key<T>,
    default: ()->T,
) : WrappedDataStore.Key<T>(key, default, PreferencesKey::class) {
    companion object {
        /**
         * 画面を暗くした後の時のライトレベル
         *
         * -1.0f ~ 1.0f
         * マイナス値で黒前景を表示してシステムで設定可能なライトレベル未満にする
         */
        val LIGHT_LEVEL_OFF = makeKey("LIGHT_LEVEL_OFF") { 1f }

        /**
         * アプリが点いてすぐ明るいときのライトレベル
         *
         * 0.0f ~ 1.0f
         * システムで設定可能なライトレベル範囲
         */
        val LIGHT_LEVEL_ON = makeKey("LIGHT_LEVEL_ON") { 0f }

        /**
         * アプリが点いてすぐのライトレベルをシステムの値にする
         */
        val SYSTEM_LIGHT_LEVEL_ON = makeKey("SYSTEM_LIGHT_LEVEL_ON") { false }

        /** 消灯までの待機時間(ミリ秒) */
        val LIGHT_OFF_INTERVAL = makeKey("LIGHT_OFF_INTERVAL") { 5_000L }

        /** 通知を表示しない時間帯(開始時刻) */
        val SILENT_TIMEZONE_START = makeKey("SILENT_TIMEZONE_START") { LocalTime.of(0, 0).toSecondOfDay() }

        /** 通知を表示しない時間帯(終了時刻) */
        val SILENT_TIMEZONE_END = makeKey("SILENT_TIMEZONE_END") { LocalTime.of(7, 0).toSecondOfDay() }

        /** 指定値未満のバッテリレベルでは通知を表示しない */
        val REQUIRED_BATTERY_LEVEL = makeKey("REQUIRED_BATTERY_LEVEL") { 15 }

        // ------ //

        suspend fun dataStore(context: Context) =
            WrappedDataStore.create(context, PreferencesKey::class)

        /** キーを作成する */
        private inline fun <reified T : Any> makeKey(
            name: String,
            noinline default: ()->T,
        ) = PreferencesKey(
            key = preferencesKey(name),
            default = default,
        )
    }
}

class LocalTimeSerializer : Serializer<LocalTime, Long> {
    override fun serialize(value: LocalTime): Long {
        return value.toSecondOfDay().toLong()
    }

    override fun deserialize(value: Long): LocalTime {
        return LocalTime.ofSecondOfDay(value)
    }
}
