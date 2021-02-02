package com.suihan74.nedge.database.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suihan74.nedge.models.KeywordMatchingType
import com.suihan74.nedge.models.NotificationSetting

@Entity
data class NotificationEntity(
    /** 対象パッケージ名 (com.suihan74.satena など) */
    val packageName : String,

    /** 追加のキーワード */
    val keyword : String = "",

    /** キーワードの検索方法 */
    val keywordMatchingType : KeywordMatchingType = KeywordMatchingType.NONE,

    /**
     * 表示名
     */
    val displayName : String = "",

    /** 通知表示設定 */
    val setting : NotificationSetting = NotificationSetting(),

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0
)

/** デフォルト設定である */
val NotificationEntity.isDefault : Boolean
    get() = packageName == NotificationDao.DEFAULT_SETTING_NAME
