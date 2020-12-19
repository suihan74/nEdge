package com.suihan74.notificationreporter.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.suihan74.notificationreporter.database.notification.FieldConverter
import com.suihan74.notificationreporter.database.notification.NotificationDao
import com.suihan74.notificationreporter.database.notification.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class
    ],
    version = 1
)
@TypeConverters(FieldConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao() : NotificationDao
}
