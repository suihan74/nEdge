package com.suihan74.utilities.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.clear
import androidx.datastore.preferences.core.edit
import java.io.File

/**
 * マイグレーション失敗時の処理方法
 */
enum class OnMigrationFailureStrategy {
    /** マイグレーション処理前の状態にロールバック */
    ROLLBACK,

    /** データを全消去 */
    CLEAR,
}

// ------ //

class Migrations {
    private val migrations = ArrayList<Migration>()

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

    // ------ //

    /**
     * データストアのマイグレーション処理
     *
     * @throws IllegalStateException 移行処理の準備に失敗
     * @throws Throwable ユーザーが記述した移行処理内で発生したあらゆる例外
     */
    internal suspend fun migrate(
        rawDataStore: DataStore<Preferences>,
        versionKey: Preferences.Key<Int>,
        oldVersion: Int,
        newVersion: Int,
        onMigrationFailureStrategy: OnMigrationFailureStrategy,
        rawFile: File
    ) {
        val migrationPath = ArrayList<Migration>()
        var from = oldVersion
        while (from < newVersion) {
            val elem = migrations
                .filter { it.from == from }
                .maxWithOrNull { a, b -> a.to - b.to } ?: throw IllegalStateException("migration path is not reached to current version")
            migrationPath.add(elem)
            from = elem.to
        }
        if (migrationPath.last().to != newVersion) {
            throw IllegalStateException("migration path is not reached to current version")
        }

        val migrationBody = suspend {
            rawDataStore.edit { rawEditor ->
                migrationPath.forEach { migration ->
                    migration.migrate(rawEditor)
                    rawEditor[versionKey] = migration.to
                }
            }
        }

        when (onMigrationFailureStrategy) {
            OnMigrationFailureStrategy.ROLLBACK ->
                migrateOrRollback(rawFile, migrationBody)

            OnMigrationFailureStrategy.CLEAR ->
                migrateOrClear(rawDataStore, migrationBody)
        }
    }

    /**
     * バージョン移行を行い、失敗したらロールバックする
     *
     * @throws Throwable ユーザーが記述した移行処理内で発生したあらゆる例外
     */
    private suspend fun migrateOrRollback(rawFile: File, migrationBody: suspend ()->Any) {
        val backupFile = File(rawFile.absolutePath + "_bak")
        rawFile.copyTo(backupFile, overwrite = true)

        val result = runCatching {
            migrationBody()
        }

        result.onFailure {
            rawFile.delete()
            backupFile.renameTo(rawFile)
            throw it
        }

        result.onSuccess {
            backupFile.delete()
        }
    }

    /**
     * バージョン移行を行い、失敗したら全消去する
     *
     * @throws Throwable ユーザーが記述した移行処理内で発生したあらゆる例外
     */
    private suspend fun migrateOrClear(rawDataStore: DataStore<Preferences>, migrationBody: suspend () -> Any) {
        val result = runCatching {
            migrationBody()
        }

        result.onFailure { e ->
            rawDataStore.edit { it.clear() }
            throw e
        }
    }

    // ------ //

    private data class Migration(
        val from: Int,
        val to: Int,
        val migrate: (prefs: MutablePreferences)->Unit
    )
}
