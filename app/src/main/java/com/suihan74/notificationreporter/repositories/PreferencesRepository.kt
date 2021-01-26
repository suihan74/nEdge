package com.suihan74.notificationreporter.repositories

import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import com.suihan74.notificationreporter.dataStore.Preferences
import com.suihan74.notificationreporter.database.notification.BlackListEntity
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity
import com.suihan74.notificationreporter.models.KeywordMatchingType
import com.suihan74.utilities.extensions.contains
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * アプリ設定を扱うリポジトリ
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val notificationDao: NotificationDao
) {
    /**
     * 設定をDBに保存する
     */
    suspend fun updateNotificationEntity(entity: NotificationEntity) {
        notificationDao.insert(entity)
    }

    /**
     * 設定を削除する
     */
    suspend fun deleteNotificationEntity(entity: NotificationEntity) {
        notificationDao.delete(entity)
    }

    // ------ //

    /**
     * ブラックリスト項目を保存する
     */
    suspend fun updateBlackListEntity(entity: BlackListEntity) {
        notificationDao.insert(entity)
    }

    /**
     * ブラックリスト項目を削除する
     */
    suspend fun deleteBlackListEntity(entity: BlackListEntity) {
        notificationDao.delete(entity)
    }

    // ------ //

    /**
     * すべての通知表示設定を取得する
     */
    val allNotificationEntitiesFlow : Flow<List<NotificationEntity>>
        get() = notificationDao.getAllSettingsFlow()

    /**
     * すべてのブラックリスト項目を取得する
     */
    val allBlackListEntitiesFlow : Flow<List<BlackListEntity>>
        get() = notificationDao.getAllBlackListItemsFlow()

    // ------ //

    suspend fun getDefaultNotificationEntity() : NotificationEntity {
        return notificationDao.getDefaultEntity()
    }

    suspend fun getNotificationEntityOrNull(id: Long) : NotificationEntity? {
        return notificationDao.findById(id)
    }

    suspend fun getNotificationEntityOrNull(sbn: StatusBarNotification) : NotificationEntity? {
        return notificationDao.findByAppName(sbn.packageName)
            .sortedByDescending { it.keywordMatchingType.importance }
            .firstOrNull { isMatchKeyword(sbn, it.keyword, it.keywordMatchingType) }
    }

    suspend fun getNotificationEntityOrDefault(sbn: StatusBarNotification) : NotificationEntity {
        return getNotificationEntityOrNull(sbn) ?: notificationDao.getDefaultEntity()
    }

    // ------ //

    /**
     * ブラックリスト対象通知か確認する
     *
     * @return true - ブラックリスト対象
     */
    suspend fun isBlackListed(sbn: StatusBarNotification) : Boolean {
        return notificationDao.findBlackListItemByAppName(sbn.packageName)
            .any { isMatchKeyword(sbn, it.keyword, it.keywordMatchingType) }
    }

    // ------ //

    /**
     * キーワードにマッチするテキストを含む通知であるか確認する
     */
    private fun isMatchKeyword(
        sbn: StatusBarNotification,
        keyword: String,
        keywordMatchingType: KeywordMatchingType
    ) : Boolean {
        return when (keywordMatchingType) {
            KeywordMatchingType.NONE -> true

            KeywordMatchingType.INCLUDE,
            KeywordMatchingType.EXCLUDE -> {
                val whiteSpaceRegex = Regex("""\s+""")
                val keywords = whiteSpaceRegex.split(keyword)
                val regex = Regex(keywords.joinToString("|") { """\Q$it\E""" })
                sbn.notification?.contains(regex) == (keywordMatchingType == KeywordMatchingType.INCLUDE)
            }
        }
    }

    // ------ //

    /**
     * アプリ設定値を取得する
     */
    suspend fun preferences() : Preferences {
        return dataStore.data.firstOrNull() ?: Preferences()
    }

    /**
     * アプリ設定値を受け取る`Flow`を取得する
     */
    val preferencesFlow : Flow<Preferences>
        get() = dataStore.data

    /**
     * アプリ設定値を更新する
     */
    suspend fun updatePreferences(transform: suspend (Preferences)->Preferences) {
        dataStore.updateData(transform)
    }
}
