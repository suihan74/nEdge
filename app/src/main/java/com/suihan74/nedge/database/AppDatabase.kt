package com.suihan74.nedge.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.suihan74.nedge.database.notification.BlackListEntity
import com.suihan74.nedge.database.notification.FieldConverter
import com.suihan74.nedge.database.notification.NotificationDao
import com.suihan74.nedge.database.notification.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        BlackListEntity::class
    ],
    version = 1
)
@TypeConverters(FieldConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao() : NotificationDao
}
