package com.suihan74.notificationreporter.database.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suihan74.notificationreporter.models.KeywordMatchingType
import com.suihan74.notificationreporter.models.NotificationSetting

@Entity
data class NotificationEntity(
    /** 対象アプリ名 (com.suihan74.satena など) */
    val appName : String,

    /** 追加のキーワード */
    val keyword : String = "",

    /** キーワードの検索方法 */
    val keywordMatchingType : KeywordMatchingType = KeywordMatchingType.NONE,

    /** 通知表示設定 */
    val setting : NotificationSetting = NotificationSetting(),

    @PrimaryKey
    val key : String = "$appName!$keyword!$keywordMatchingType"
) {
}
