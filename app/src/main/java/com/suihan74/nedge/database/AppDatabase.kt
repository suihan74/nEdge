package com.suihan74.nedge.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.suihan74.nedge.Application
import com.suihan74.nedge.database.notification.BlackListEntity
import com.suihan74.nedge.database.notification.FieldConverter
import com.suihan74.nedge.database.notification.NotificationDao
import com.suihan74.nedge.database.notification.NotificationEntity

@Database(
    entities = [
        NotificationEntity::class,
        BlackListEntity::class
    ],
    version = 2
)
@TypeConverters(FieldConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao() : NotificationDao
}

// ------ //

/** AppDatabaseインスタンスを作成する */
fun Application.createAppDatabase() =
    Room.databaseBuilder(this, AppDatabase::class.java, "app-db")
        .addMigrations(
            Migration1to2()
        )
        .build()
