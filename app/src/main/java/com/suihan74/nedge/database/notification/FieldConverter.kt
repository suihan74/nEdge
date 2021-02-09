package com.suihan74.nedge.database.notification

import androidx.room.TypeConverter
import com.suihan74.nedge.models.KeywordMatchingType
import com.suihan74.nedge.models.NotificationSetting
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class FieldConverter {
    @TypeConverter
    fun fromKeywordMatchingType(value: KeywordMatchingType) : String {
        return value.name
    }

    @TypeConverter
    fun toKeywordMatchingType(name: String) : KeywordMatchingType {
        return KeywordMatchingType.valueOf(name)
    }

    // ------ //

    @TypeConverter
    fun fromNotificationSetting(value: NotificationSetting) : String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toNotificationSetting(json: String) : NotificationSetting {
        return Json.decodeFromString(json)
    }

    // ------ //

    @TypeConverter
    fun fromInstant(value: Instant) : Long {
        return value.epochSecond
    }

    @TypeConverter
    fun toInstant(epochSecond: Long) : Instant {
        return Instant.ofEpochSecond(epochSecond)
    }
}
