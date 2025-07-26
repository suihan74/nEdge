package com.suihan74.nedge.models

import androidx.annotation.StringRes
import com.suihan74.nedge.R

/**
 * 複数通知がある場合の処理方法
 */
enum class MultipleNotificationsSolution(
    @param:StringRes val textId : Int
) {
    /** 最新一件のみ表示 */
    LATEST(R.string.multi_notices_solution_latest),

    /** 順番に切り替え */
    SWITCH_IN_ORDER(R.string.multi_notices_solution_switch_in_order),

    /** ランダムに切替え */
    SWITCH_RANDOMLY(R.string.multi_notices_solution_switch_randomly),
}
