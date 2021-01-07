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
    suspend fun getAll() : List<NotificationEntity>

    @Query("SELECT * FROM NotificationEntity")
    fun getAllSettingsFlow() : Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM NotificationEntity
        WHERE appName = :appName
    """)
    suspend fun findByAppName(appName: String) : List<NotificationEntity>

    @Transaction
    suspend fun getDefaultSetting() : NotificationSetting {
        return findByAppName(DEFAULT_SETTING_NAME).firstOrNull()?.setting ?: NotificationSetting()
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
