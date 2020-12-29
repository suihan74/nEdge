@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.suihan74.utilities.dataStore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.suihan74.utilities.dataStore.exception.InvalidKeyException
import com.suihan74.utilities.dataStore.exception.MigrationFailureException
import com.suihan74.utilities.extensions.firstByType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import kotlin.reflect.KClass

/**
 * デフォルト値とデータ型をキーに設定するようにした`DataStore`
 */
class WrappedDataStore<KeyT : WrappedDataStore.Key<*>> private constructor (
    context: Context,
    dataStoreKeyAnn: DataStoreKey
) {
    companion object {
        /**
         * データストアインスタンスを取得する
         *
         * 必要ならマイグレーション処理を行う
         *
         * @throws InvalidKeyException 渡されたキーが不正
         * @throws MigrationFailureException 移行処理中に発生した例外を内包した例外
         */
        suspend fun <KeyT : Key<*>> create(
            context: Context,
            keyClass: KClass<KeyT>,
            migrations: Migrations? = null,
            onMigrationFailureStrategy: OnMigrationFailureStrategy = OnMigrationFailureStrategy.ROLLBACK
        ) : WrappedDataStore<KeyT> {
            val instance =
                try { WrappedDataStore<KeyT>(context, keyClass.annotations.firstByType()) }
                catch (e: Throwable) { throw InvalidKeyException(cause = e) }

            val keyVersion = instance.currentKeyVersion
            val dataStoreVersion = instance.version()

            if (keyVersion != dataStoreVersion) {
                val result = runCatching {
                    when {
                        keyVersion < dataStoreVersion ->
                            throw IllegalStateException(
                                "key version($keyVersion) is older than DataStore version($dataStoreVersion)"
                            )

                        migrations == null ->
                            throw IllegalArgumentException("need to migrate")

                        else ->
                            migrations.migrate(
                                instance.dataStore,
                                instance.versionKey,
                                oldVersion = dataStoreVersion,
                                newVersion = keyVersion,
                                onMigrationFailureStrategy,
                                instance.rawFile
                            )
                    }
                }

                result.onFailure {
                    throw MigrationFailureException(cause = it)
                }
            }

            return instance
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
                keyClass.annotations.firstByType<DataStoreKey>().dataStoreName
            )
    }

    // ------ //

    /** データストア名 */
    private val dataStoreName: String = dataStoreKeyAnn.dataStoreName

    /** データストアインスタンス本体 */
    private val dataStore = context.createDataStore(dataStoreName)

    /** 使用されているキーのバージョン */
    private val currentKeyVersion: Int = dataStoreKeyAnn.version

    /** `Key`バージョン記録用のキー */
    private val versionKey
        get() = preferencesKey<Int>("!!__DATA_STORE_VERSION__!!")

    /**
     * データ保存先ファイル
     *
     * マイグレーション失敗時のロールバックに使用
     */
    private val rawFile =
        File(context.filesDir, "datastore/$dataStoreName.preferences_pb")

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
     * @throws InvalidKeyException データストアとキーが非対応
     */
    private inline fun checkKey(key: Key<*>, lazyMessage: ()->Any = { "invalid key" }) {
        if (key.dataStoreName != dataStoreName) {
            throw InvalidKeyException(message = lazyMessage().toString())
        }
    }

    // ------ //
    // 値取得

    /**
     * 値を取得する
     *
     * @throws InvalidKeyException データストアとキーが非対応
     */
    suspend fun <T> get(key: Key<T>) : T {
        checkKey(key)
        return dataStore.data.first()[key.key] ?: key.default()
    }

    /**
     * 値の変更を監視する`Flow`を取得する
     *
     * @throws InvalidKeyException データストアとキーが非対応
     */
    fun <T> getFlow(key: Key<T>) : Flow<T> {
        checkKey(key)
        return dataStore.data.map { prefs ->
            prefs[key.key] ?: key.default()
        }
    }

    /**
     * 値の変更を監視する`LiveData`を取得する
     */
    fun <T> getLiveData(key: Key<T>, coroutineScope: CoroutineScope) : LiveData<T> {
        checkKey(key)

        return MutableLiveData<T>().also { liveData ->
            getFlow(key)
                .onEach { liveData.value = it }
                .launchIn(coroutineScope)
        }
    }

    /**
     * 値の変更を監視する`MutableLiveData`を取得する
     *
     * これに対して値変更を行うと自動的に`DataStore`側の値も更新される
     */
    fun <T> getMutableLiveData(key: Key<T>, coroutineScope: CoroutineScope) : MutableLiveData<T> {
        checkKey(key)

        return MutableLiveData<T>().also { liveData ->
            liveData.observeForever {
                if (liveData.value == it) return@observeForever
                coroutineScope.launch {
                    edit { set(key, it) }
                }
            }

            getFlow(key)
                .onEach { liveData.value = it }
                .launchIn(coroutineScope)
        }
    }

    // ------ //
    // 値更新

    /**
     * 値更新用のブロックを実行する
     *
     * dataStore.edit {
     *     set(key, value)
     * }
     */
    suspend fun edit(transform: suspend Editor.()->Unit) = withContext(Dispatchers.Main) {
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
         * @throws InvalidKeyException データストアとキーが非対応
         */
        fun <T> set(key: Key<T>, value: T) {
            checkKey(key)
            prefs[key.key] = value
        }

        /**
         * 全データを消去する
         */
        fun clear() {
            prefs.clear()
        }
    }

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
            keyClass.annotations.firstByType<DataStoreKey>().dataStoreName
    }
}

// ------ //

/** キーを表すアノテーション */
@Target(AnnotationTarget.CLASS)
annotation class DataStoreKey(val dataStoreName: String, val version: Int)
