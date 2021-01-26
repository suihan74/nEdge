package com.suihan74.notificationreporter.models

import androidx.annotation.StringRes
import com.suihan74.notificationreporter.R
import kotlinx.serialization.Serializable

/**
 * 通知内容の表示モード
 */
@Serializable
enum class InformationDisplayMode(
    val code: Int,
    @StringRes val textId: Int
) {
    /** 何も表示しない */
    NONE(0, R.string.information_display_mode_none),

    /** 表示名と通知テキストを表示する */
    FULL(0b111, R.string.information_display_mode_full),

    /** 表示名のみ表示する */
    Label(0b100, R.string.information_display_mode_label),

    /** 通知タイトルのみ表示する */
    TITLE(0b010, R.string.information_display_mode_title),

    /** 通知テキストのみ表示する */
    TEXT(0b001, R.string.information_display_mode_text),

    /** 通知タイトル・テキストを表示する */
    TITLE_AND_TEXT(0b011, R.string.information_display_mode_title_and_text),

    /** 表示名と通知タイトルを表示する */
    LABEL_AND_TITLE(0b110, R.string.information_display_mode_label_and_title),

    /** 表示名と通知テキストを表示する */
    LABEL_AND_TEXT(0b101, R.string.information_display_mode_label_and_text)
}
