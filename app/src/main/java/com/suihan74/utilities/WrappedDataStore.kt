@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.suihan74.utilities

import android.content.Context
import androidx.annotation.MainThread
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.MutableLiveData
import com.suihan74.utilities.extensions.alsoAs
import com.suihan74.utilities.extensions.firstByType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 * デフォルト値とデータ型をキーに設定するようにした`DataStore`
 */
class WrappedDataStore private constructor (
    context: Context,
    keyClass: KClass<*>
) {
    companion object {
        /**
         * データストアインスタンスを取得する
         *
         * 必要ならマイグレーション処理を行う
         *
         * @throws MigrationFailureException
         */
        suspend fun create(
            context: Context,
            keyClass: KClass<*>,
            migrations: Migrations? = null
        ) : WrappedDataStore {
            val instance = WrappedDataStore(context, keyClass)
            val keyVersion = instance.currentKeyVersion
            val dataStoreVersion = instance.version()

            if (keyVersion != dataStoreVersion) {
                val result = runCatching {
                    if (keyVersion < dataStoreVersion) {
                        throw IllegalStateException(
                            "key version($keyVersion) is older than DataStore version($dataStoreVersion)"
                        )
                    }

                    migrate(
                        instance,
                        oldVersion = dataStoreVersion,
                        newVersion = keyVersion,
                        migrations = migrations ?: throw IllegalArgumentException("need to migrate")
                    )
                }

                if (result.isFailure) {
                    throw MigrationFailureException(cause = result.exceptionOrNull())
                }
            }

            return instance
        }

        /**
         * データストアのマイグレーション処理
         *
         * @throws IllegalStateException
         */
        private suspend fun migrate(
            instance: WrappedDataStore,
            oldVersion: Int,
            newVersion: Int,
            migrations: Migrations
        ) {
            val migrationPath = ArrayList<Migration>()
            var from = oldVersion
            while (from < newVersion) {
                val elem = migrations.migrations
                    .filter { it.from == from }
                    .maxWithOrNull { a, b -> a.to - b.to } ?: throw IllegalStateException("migration path is not reached to current version")
                migrationPath.add(elem)
                from = elem.to
            }
            if (migrationPath.last().to != newVersion) {
                throw IllegalStateException("migration path is not reached to current version")
            }

            instance.dataStore.edit {
                val versionKey = instance.versionKey
                migrationPath.forEach { migration ->
                    migration.migrate(it)
                    it[versionKey] = migration.to
                }
            }
        }

        /**
         * データストアを消去する
         */
        suspend fun clear(context: Context, dataStoreName: String) {
            val dataStore = context.createDataStore(dataStoreName)
            dataStore.edit {
                it.clear()
            }
        }

        /**
         * データストアを消去する
         */
        suspend fun clear(context: Context, keyClass: KClass<*>) =
            clear(
                context,
                keyClass.java.annotations.firstByType<DataStoreKey>().dataStoreName
            )
    }

    // ------ //

    /** データストア名 */
    private val dataStoreName: String =
        keyClass.java.annotations.firstByType<DataStoreKey>().dataStoreName

    /** データストアインスタンス本体 */
    private val dataStore = context.createDataStore(
        dataStoreName
    )

    /** 使用されているキーのバージョン */
    private val currentKeyVersion: Int =
        keyClass.java.annotations.firstByType<DataStoreKey>().version

    /** `Key`バージョン記録用のキー */
    private val versionKey
        get() = preferencesKey<Int>("!!__DATA_STORE_VERSION__!!")

    // ------ //

    /**
     * 最後に記録した`Key`バージョンを取得する
     */
    suspend fun version() : Int =
        dataStore.data.first()[versionKey] ?: currentKeyVersion

    // ------ //

    /**
     * 各メソッドに渡されたキーがこのデータストアで有効なものかを検証する
     *
     * @throws IllegalStateException データストアとキーが非対応
     */
    private inline fun checkKey(key: Key<*>, lazyMessage: ()->Any = { "invalid key" }) {
        check(key.dataStoreName == dataStoreName, lazyMessage)
    }

    // ------ //
    // 値取得

    /**
     * 値を取得する
     *
     * @throws IllegalStateException データストアとキーが非対応
     */
    suspend fun <T> get(key: Key<T>) : T {
        checkKey(key)
        return dataStore.data.first()[key.key] ?: key.default()
    }

    /**
     * 値の変更を監視する`Flow`を取得する
     *
     * @throws IllegalStateException データストアとキーが非対応
     */
    fun <T> getFlow(key: Key<T>) : Flow<T> {
        checkKey(key)
        return dataStore.data.map { prefs ->
            prefs[key.key] ?: key.default()
        }
    }

    /**
     * 値の変更を監視する`MutableLiveData`を取得する
     *
     * この`LiveData`に対して値変更を行うと自動的に`DataStore`側の値も更新される
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getLiveData(key: Key<T>, coroutineScope: CoroutineScope = GlobalScope) : MutableLiveData<T> {
        checkKey(key)

        val liveData = runBlocking {
            liveDataCacheMutex.withLock {
                liveDataCache[key.key.name]?.get() ?: createLiveData(key, coroutineScope)
            }
        }

        return liveData as MutableLiveData<T>
    }

    /**
     * キーに対応する新しい`LiveData`を生成する
     */
    private fun <T> createLiveData(
        key: Key<T>,
        coroutineScope: CoroutineScope
    ) : MutableLiveData<T> {
        val liveData = MutableLiveData<T>()

        coroutineScope.launch(Dispatchers.Main) {
            liveData.observeForever { value ->
                coroutineScope.launch {
                    edit {
                        set(key, value)
                    }
                }
            }

            liveData.value = get(key)
        }

        liveDataCache[key.key.name] = WeakReference(liveData)

        return liveData
    }

    /**
     * 使用されている`LiveData`のキャッシュ
     */
    private val liveDataCache = HashMap<String, WeakReference<MutableLiveData<*>>>()

    /** `liveDataCache`アクセス用の排他ロック */
    private val liveDataCacheMutex = Mutex()

    // ------ //
    // 値更新

    /**
     * 値更新用のブロックを実行する
     *
     * dataStore.edit {
     *     set(key, value)
     * }
     */
    suspend fun edit(transform: suspend Editor.()->Unit) = withContext(Dispatchers.Default) {
        dataStore.edit { prefs ->
            val result = runCatching {
                transform.invoke(Editor(prefs))
            }

            if (result.isSuccess) {
                prefs[versionKey] = currentKeyVersion
            }
        }
    }

    inner class Editor(private val prefs: MutablePreferences) {
        /**
         * 対応キーの値を変更する
         *
         * @throws IllegalStateException データストアとキーが非対応
         */
        suspend fun <T> set(key: Key<T>, value: T) {
            checkKey(key)
            prefs[key.key] = value

            // 該当キーの生きている`LiveData`が存在する場合、その値を更新する

            liveDataCacheMutex.withLock {
                liveDataCache[key.key.name]?.get()?.alsoAs<MutableLiveData<T>> { liveData ->
                    if (liveData.value != value) {
                        liveData.postValue(value)
                    }
                }
            }
        }

        /**
         * 全データを消去する
         */
        fun clear() {
            prefs.clear()
        }
    }

    // ------ //
    // バージョン移行

    internal data class Migration internal constructor(
        val from: Int,
        val to: Int,
        val migrate: (prefs: MutablePreferences)->Unit
    )

    class Migrations {
        internal val migrations = ArrayList<Migration>()

        /**
         * マイグレーション処理を登録する
         *
         * @param from 移行前のバージョン番号
         * @param to 移行後のバージョン番号
         * @param migrate 移行処理
         *
         * @throws IllegalArgumentException バージョン指定が不正
         */
        fun add(from: Int, to: Int, migrate: (prefs: MutablePreferences)->Unit) : Migrations {
            if (from >= to) {
                throw IllegalArgumentException("`from` version is greater than `to` version")
            }
            if (migrations.any { it.from == from && it.to == to }) {
                throw IllegalArgumentException("duplicate migration for the pair of versions")
            }
            migrations.add(Migration(from, to, migrate))

            return this
        }
    }

    /**
     * マイグレーション失敗時の例外
     */
    class MigrationFailureException(cause: Throwable? = null) : Throwable(cause = cause)

    // ------ //

    /**
     * キー
     */
    abstract class Key<T>(
        val key: Preferences.Key<T>,
        val default: ()->T,
        keyClass: KClass<*>,
    ) {
        internal val dataStoreName: String =
            keyClass.java.annotations.firstByType<DataStoreKey>().dataStoreName
    }
}

/** キーを表すアノテーション */
@Target(AnnotationTarget.CLASS)
annotation class DataStoreKey(val dataStoreName: String, val version: Int)
