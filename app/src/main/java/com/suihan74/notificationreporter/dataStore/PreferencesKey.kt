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
class PreferencesKey<T>(
    key: Preferences.Key<T>,
    default: ()->T,
) : WrappedDataStore.Key<T>(key, default, PreferencesKey::class) {
    companion object {
        /** 消灯時のライトレベル */
        val LIGHT_LEVEL = makeKey("LIGHT_LEVEL") { 1f }

        /** 消灯までの待機時間(ミリ秒) */
        val LIGHT_OFF_INTERVAL = makeKey("LIGHT_OFF_INTERVAL") { 5_000L }

        /** 通知を表示しない時間帯(開始時刻) */
        val DISABLE_TIME_START = makeKey("DISABLE_TIME_START") { LocalTime.of(0, 0).toSecondOfDay() }

        /** 通知を表示しない時間帯(終了時刻) */
        val DISABLE_TIME_END = makeKey("DISABLE_TIME_END") { LocalTime.of(7, 0).toSecondOfDay() }

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
