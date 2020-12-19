package com.suihan74.notificationreporter.database.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suihan74.notificationreporter.models.NotificationSetting

@Entity
data class NotificationEntity(
    /** 対象アプリ名 (com.suihan74.satena など) */
    @PrimaryKey val appName: String,

    /** 通知表示設定 */
    val setting: NotificationSetting
)
