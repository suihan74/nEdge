package com.suihan74.notificationreporter.models

import kotlinx.serialization.Serializable

/**
 * 通知設定のキーワードマッチ方法
 */
@Serializable
enum class KeywordMatchingType(
    val importance : Int
) {
    /** キーワード設定なし */
    NONE(0),

    /** キーワードを含む */
    INCLUDE(2),

    /** キーワードを除く */
    EXCLUDE(1),

    /*
    /** キーワードと完全一致 */
    EQUAL(3),

    /** キーワードで始まる */
    START_WITH(2),

    /** キーワードで終わる */
    END_WITH(2),
    */
}
