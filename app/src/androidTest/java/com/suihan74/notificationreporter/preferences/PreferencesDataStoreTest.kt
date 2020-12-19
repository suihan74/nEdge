package com.suihan74.notificationreporter.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.test.core.app.ApplicationProvider
import com.suihan74.utilities.DataStoreKey
import com.suihan74.utilities.WrappedDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class PreferencesDataStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun init() {
        runBlocking {
            WrappedDataStore.clear(context, PreferencesKey::class)
            val dataStore = PreferencesKey.dataStore(context)
            dataStore.edit {
                set(PreferencesKey.LIGHT_LEVEL, 0f)
            }
        }
    }

    @After
    fun post() {
        runBlocking {
            WrappedDataStore.clear(context, PreferencesKey::class)
        }
    }

    @Test
    fun インスタンス生成() {
        runBlocking {
            val dataStore = PreferencesKey.dataStore(context)
            assertEquals(0f, dataStore.get(PreferencesKey.LIGHT_LEVEL))
        }
    }

    @Test
    fun 値の書き込み() {
        runBlocking {
            val dataStore = PreferencesKey.dataStore(context)
            dataStore.edit {
                set(PreferencesKey.LIGHT_LEVEL, 123f)
            }
            assertEquals(123f, dataStore.get(PreferencesKey.LIGHT_LEVEL))
        }
    }

    @Test
    fun マイグレーション() {
        runBlocking {
            val dataStore = PreferencesKey.dataStore(context)
            dataStore.edit {
                set(PreferencesKey.LIGHT_LEVEL, 456f)
            }
        }

        runBlocking {
            val dataStore = PreferencesKey2.dataStore(context)
            assertEquals(456, dataStore.get(PreferencesKey2.LIGHT_LEVEL))
            assertEquals(5000, dataStore.get(PreferencesKey2.LIGHT_OFF_INTERVAL))
        }
    }

    @Test
    fun LiveData() {
        val dataStore = runBlocking {
            PreferencesKey.dataStore(context)
        }

        val liveData = runBlocking {
            dataStore.getLiveData(PreferencesKey.LIGHT_LEVEL)
        }

        runBlocking(Dispatchers.Main) {
            liveData.observeForever {
                Log.i("test", it.toString())
            }
        }

        runBlocking {
            dataStore.edit {
                set(PreferencesKey.LIGHT_LEVEL, 678f)
            }

            delay(1000)

            assertEquals(678f, liveData.value)
        }
    }
}

// ------ //

/** マイグレーションテスト用のキー */
@DataStoreKey("settings", version = 3)
class PreferencesKey2<T>(
    key: Preferences.Key<T>,
    default: ()->T
) : WrappedDataStore.Key<T>(key, default, PreferencesKey2::class) {
    companion object {
        /** 消灯時のライトレベル */
        val LIGHT_LEVEL = makeKey<Int>("LIGHT_LEVEL") { 0 }

        /** 消灯までの待機時間(ミリ秒) */
        val LIGHT_OFF_INTERVAL = makeKey<Int>("LIGHT_OFF_INTERVAL") { 5_000 }

        // ------ //

        private val migrations = WrappedDataStore.Migrations()
            .add(1, 2) { prefs ->
                // 1 -> 2; LIGHT_LEVELをFloatからIntに変更
                val key = preferencesKey<Float>(LIGHT_LEVEL.key.name)
                val prevValue = prefs[key]?.toInt() ?: LIGHT_LEVEL.default()
                prefs[LIGHT_LEVEL.key] = prevValue
                Log.i("migration", "1 -> 2")
            }
            .add(2, 3) { prefs ->
                // 2 -> 3; LIGHT_OFF_INTERVALをFloatからIntに変更
                val key = preferencesKey<Float>(LIGHT_OFF_INTERVAL.key.name)
                val prevValue = prefs[key]?.toInt() ?: LIGHT_OFF_INTERVAL.default()
                prefs[LIGHT_OFF_INTERVAL.key] = prevValue
                Log.i("migration", "2 -> 3")
            }
            .add(1, 3) { prefs ->
                // 1 -> 3;
                run {
                    val key = preferencesKey<Float>(LIGHT_LEVEL.key.name)
                    val prevValue = prefs[key]?.toInt() ?: LIGHT_LEVEL.default()
                    prefs[LIGHT_LEVEL.key] = prevValue
                }

                run {
                    val key = preferencesKey<Float>(LIGHT_OFF_INTERVAL.key.name)
                    val prevValue = prefs[key]?.toInt() ?: LIGHT_OFF_INTERVAL.default()
                    prefs[LIGHT_OFF_INTERVAL.key] = prevValue
                }

                Log.i("migration", "1 -> 3")
            }

        suspend fun dataStore(context: Context) = WrappedDataStore.create(
            context,
            PreferencesKey2::class,
            migrations
        )

        // ------ //

        /** キーを作成する */
        private inline fun <reified T : Any> makeKey(
            name: String,
            noinline default: ()->T
        ) = PreferencesKey2(
            key = preferencesKey(name),
            default = default
        )
    }
}
