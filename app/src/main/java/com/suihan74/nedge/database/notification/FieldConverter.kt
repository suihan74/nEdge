package com.suihan74.nedge.database.notification

import androidx.room.TypeConverter
import com.suihan74.nedge.models.KeywordMatchingType
import com.suihan74.nedge.models.NotificationSetting
import kotlinx.serialization.ExperimentalSerializationApi
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

    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun fromNotificationSetting(value: NotificationSetting) : String {
        return Json.encodeToString(value)
    }

    @OptIn(ExperimentalSerializationApi::class)
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
