package com.suihan74.notificationreporter.database.notification

import androidx.room.TypeConverter
import com.suihan74.notificationreporter.models.NotificationSetting
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FieldConverter {
    @TypeConverter
    fun fromNotificationSetting(value: NotificationSetting) : String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toNotificationSetting(json: String) : NotificationSetting {
        return Json.decodeFromString(json)
    }
}
