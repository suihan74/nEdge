package com.suihan74.nedge.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant

class Migration1to2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val now = Instant.now()
        db.execSQL("""ALTER TABLE `NotificationEntity` ADD `lastUpdated` INTEGER NOT NULL DEFAULT ${now.epochSecond};""")
    }
}
