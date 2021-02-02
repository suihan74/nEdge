package com.suihan74.nedge.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.suihan74.nedge.R

/**
 * ノッチの種類
 */
enum class NotchType(
    @StringRes val textId : Int,
    @DrawableRes val iconId : Int,
) {
    /** ノッチ無し */
    NONE(R.string.notch_type_none, 0),

    /** 矩形 */
    RECTANGLE(R.string.notch_type_rectangle, R.drawable.ic_notch_rect),

    /** 水滴 */
    WATER_DROP(R.string.notch_type_water_drop, R.drawable.ic_notch_water_drop),

    /** ○の切り抜き (辺に接しない) */
    PUNCH_HOLE(R.string.notch_type_punch_hole, R.drawable.ic_notch_punch_hole),

    /** コーナー */
    CORNER(R.string.notch_type_corner, R.drawable.ic_notch_corner),

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
