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

    /** 画面上部角の角丸半径 */
    val topCornerRadius: Float = 0f,

    /** 画面下部角の角丸半径 */
    val bottomCornerRadius: Float = 0f,
)
