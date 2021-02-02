package com.suihan74.nedge.models

import androidx.annotation.StringRes
import com.suihan74.nedge.R

/**
 * 設定が登録されていない通知の処理方法
 */
enum class UnknownNotificationSolution(
    @StringRes val textId : Int
) {
    /** デフォルト通知設定で表示 */
    DEFAULT(R.string.unknown_notice_solution_default),

    /** 無視する */
    IGNORE(R.string.unknown_notice_solution_ignore),
}
