package com.suihan74.nedge.repositories

import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import com.suihan74.nedge.dataStore.Preferences
import com.suihan74.nedge.database.notification.BlackListEntity
import com.suihan74.nedge.database.notification.NotificationDao
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.models.KeywordMatchingType
import com.suihan74.utilities.extensions.contains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.Instant

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
     * 形状データを他の設定にコピーする
     *
     * 他の条件やカラーなどは元々のものを引き継ぐ
     */
    suspend fun copyShapes(
        from: NotificationEntity,
        to: NotificationEntity
    ) = withContext(Dispatchers.Default) {
        updateNotificationEntity(
            to.copy(
                lastUpdated = Instant.now(),
                setting = to.setting.copy(
                    thickness = from.setting.thickness,
                    blurSize = from.setting.blurSize,
                    outlinesSetting = to.setting.outlinesSetting.copy(
                        topCornerRadius = from.setting.outlinesSetting.topCornerRadius,
                        bottomCornerRadius = from.setting.outlinesSetting.bottomCornerRadius,
                        topEdgeOffset = from.setting.outlinesSetting.topEdgeOffset,
                        bottomEdgeOffset = from.setting.outlinesSetting.bottomEdgeOffset
                    ),
                    topNotchSetting = from.setting.topNotchSetting,
                    bottomNotchSetting = from.setting.bottomNotchSetting
                )
            )
        )
    }

    /**
     * 形状データをデフォルトのもので上書きする
     *
     * 他の条件やカラーなどは元々のものを引き継ぐ
     */
    suspend fun copyShapesFromDefault(entity: NotificationEntity) = withContext(Dispatchers.Default) {
        val defaultEntity = notificationDao.getDefaultEntity()
        copyShapes(from = defaultEntity, to = entity)
    }

    /**
     * 複数の設定の形状データをデフォルトのもので上書きする
     *
     * 他の条件やカラーなどは元々のものを引き継ぐ
     */
    suspend fun copyShapesFromDefault(entities: List<NotificationEntity>) = withContext(Dispatchers.Default) {
        if (entities.isEmpty()) return@withContext
        val defaultEntity = notificationDao.getDefaultEntity()
        for (entity in entities) {
            copyShapes(from = defaultEntity, to = entity)
        }
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
