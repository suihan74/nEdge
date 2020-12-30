package com.suihan74.notificationreporter.models

import androidx.annotation.DrawableRes
import com.suihan74.notificationreporter.R

/**
 * ノッチの種類
 */
enum class NotchType(
    @DrawableRes val iconId: Int
) {
    /** ノッチ無し */
    NONE(0),

    /** 矩形 */
    RECTANGLE(R.drawable.ic_notch_rect),

    /** 水滴 (O型も含む) */
    WATER_DROP(R.drawable.ic_notch_water_drop),

    /** ○の切り抜き (辺に接しない) */
    PUNCH_HOLE(R.drawable.ic_notch_punch_hole),

    ;

    companion object {
        /**
         * 名前からインスタンスを取得
         *
         * @throws NoSuchElementException IDが存在しない
         */
        fun fromName(name: String) = values().first { it.name == name }
    }
}
