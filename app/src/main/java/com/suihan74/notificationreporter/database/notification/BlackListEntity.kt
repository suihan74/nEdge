package com.suihan74.notificationreporter.database.notification

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suihan74.notificationreporter.models.KeywordMatchingType

@Entity
data class BlackListEntity(
    /** 対象パッケージ名 (com.suihan74.satena など) */
    val packageName : String,

    /** 追加のキーワード */
    val keyword : String = "",

    /** キーワードの検索方法 */
    val keywordMatchingType : KeywordMatchingType = KeywordMatchingType.NONE,

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
)
