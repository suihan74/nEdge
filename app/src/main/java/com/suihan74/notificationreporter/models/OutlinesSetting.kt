package com.suihan74.notificationreporter.models

/**
 * スクリーン輪郭線の描画設定
 */
data class OutlinesSetting (
    /** 描画タイプ */
    val type: OutlinesType = OutlinesType.FULL,

    /** 左上の角丸半径 */
    val leftTopCornerRadius: Float = 0f,

    /** 右上の角丸半径 */
    val rightTopCornerRadius: Float = 0f,

    /** 左下の角丸半径 */
    val leftBottomCornerRadius: Float = 0f,

    /** 右下の角丸半径 */
    val rightBottomCornerRadius: Float = 0f,
)
