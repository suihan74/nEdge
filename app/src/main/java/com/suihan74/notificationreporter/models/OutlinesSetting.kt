package com.suihan74.notificationreporter.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * スクリーン輪郭線の描画設定
 */
@Serializable
data class OutlinesSetting (
    /** 描画タイプ */
    @SerialName("outlines_type")
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
