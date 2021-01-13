package com.suihan74.notificationreporter.database.notification

import androidx.room.*
import com.suihan74.notificationreporter.models.NotificationSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    companion object {
        const val DEFAULT_SETTING_NAME = "!!__DEFAULT_SETTING__!!"
    }

    @Query("SELECT * FROM NotificationEntity")
    fun getAllSettingsFlow() : Flow<List<NotificationEntity>>

    @Query("SELECT * FROM BlackListEntity")
    fun getAllBlackListItemsFlow() : Flow<List<BlackListEntity>>

    // ------ //

    @Query(
        """
        SELECT * FROM NotificationEntity
        WHERE packageName = :packageName
    """
    )
    suspend fun findByAppName(packageName: String) : List<NotificationEntity>

    @Query("""
        SELECT * FROM NotificationEntity
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun findById(id: Long) : NotificationEntity?

    @Query("""
        SELECT * FROM BlackListEntity
        WHERE packageName = :packageName
    """)
    suspend fun findBlackListItemByAppName(packageName: String) : List<BlackListEntity>

    // ------ //

    @Transaction
    suspend fun getDefaultEntity() : NotificationEntity {
        return findByAppName(DEFAULT_SETTING_NAME).firstOrNull()
            ?: NotificationEntity(packageName = DEFAULT_SETTING_NAME, setting = NotificationSetting())
    }

    @Transaction
    suspend fun getDefaultSetting() : NotificationSetting {
        return findByAppName(DEFAULT_SETTING_NAME).firstOrNull()?.setting ?: NotificationSetting()
    }

    // ------ //

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity)

    @Transaction
    suspend fun insertDefaultSetting(setting: NotificationSetting) {
        insert(
            NotificationEntity(
                packageName = DEFAULT_SETTING_NAME,
                setting = setting
            )
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlackListEntity)

    // ------ //

    @Delete
    suspend fun delete(entity: NotificationEntity)

    @Delete
    suspend fun delete(entity: BlackListEntity)
}
