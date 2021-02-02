package com.suihan74.nedge.models

import com.suihan74.nedge.R
import kotlinx.serialization.Serializable

/**
 * 通知設定のキーワードマッチ方法
 */
@Serializable
enum class KeywordMatchingType(
    val importance : Int,
    val textId: Int
) {
    /** キーワード設定なし */
    NONE(0, R.string.keyword_matching_type_none),

    /** キーワードを含む */
    INCLUDE(2, R.string.keyword_matching_type_include),

    /** キーワードを除く */
    EXCLUDE(1, R.string.keyword_matching_type_exclude),

    /*
    /** キーワードと完全一致 */
    EQUAL(3),

    /** キーワードで始まる */
    START_WITH(2),

    /** キーワードで終わる */
    END_WITH(2),
    */
}
