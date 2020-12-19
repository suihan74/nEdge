package com.suihan74.notificationreporter.database.notification

import androidx.room.*
import com.suihan74.notificationreporter.models.NotificationSetting

@Dao
interface NotificationDao {
    companion object {
        private const val DEFAULT_SETTING_NAME = "!!__DEFAULT_SETTING__!!"
    }

    @Query("SELECT * FROM NotificationEntity")
    suspend fun getAll() : List<NotificationEntity>

    @Query("""
        SELECT * FROM NotificationEntity
        WHERE appName = :appName
        LIMIT 1
    """)
    suspend fun findByAppName(appName: String) : NotificationEntity?

    @Transaction
    suspend fun getDefaultSetting() : NotificationSetting {
        return findByAppName(DEFAULT_SETTING_NAME)?.setting ?: NotificationSetting()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity)

    @Transaction
    suspend fun insertDefaultSetting(setting: NotificationSetting) {
        insert(
            NotificationEntity(
                appName = DEFAULT_SETTING_NAME,
                setting = setting
            )
        )
    }
}
