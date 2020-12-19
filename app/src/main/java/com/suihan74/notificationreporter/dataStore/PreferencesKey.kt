package com.suihan74.notificationreporter.dataStore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import com.suihan74.utilities.DataStoreKey
import com.suihan74.utilities.WrappedDataStore

/** アプリ設定の項目 */
@DataStoreKey("settings", version = 1)
class PreferencesKey<T>(
    key: Preferences.Key<T>,
    default: ()->T
) : WrappedDataStore.Key<T>(key, default, PreferencesKey::class) {
    companion object {
        /** 消灯時のライトレベル */
        val LIGHT_LEVEL = makeKey("LIGHT_LEVEL") { 0f }

        /** 消灯までの待機時間(ミリ秒) */
        val LIGHT_OFF_INTERVAL = makeKey("LIGHT_OFF_INTERVAL") { 5_000L }

        // ------ //

        suspend fun dataStore(context: Context) = WrappedDataStore.create(
            context,
            PreferencesKey::class
        )

        // ------ //

        /** キーを作成する */
        private inline fun <reified T : Any> makeKey(
            name: String,
            noinline default: ()->T
        ) = PreferencesKey(
            key = preferencesKey(name),
            default = default
        )
    }
}
