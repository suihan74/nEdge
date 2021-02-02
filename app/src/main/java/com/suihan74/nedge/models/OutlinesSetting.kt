package com.suihan74.nedge.models

import kotlinx.serialization.Serializable

/**
 * スクリーン輪郭線の描画設定
 */
@Serializable
data class OutlinesSetting (
    /** 上辺を描画する */
    val topEdgeEnabled : Boolean = true,

    /** 下辺を描画する */
    val bottomEdgeEnabled : Boolean = true,

    /** 左辺を描画する */
    val leftEdgeEnabled : Boolean = true,

    /** 右辺を描画する */
    val rightEdgeEnabled : Boolean = true,

    /** 左上角を描画する */
    val topLeftCornerEnabled : Boolean = true,

    /** 右上角を描画する */
    val topRightCornerEnabled : Boolean = true,

    /** 左下角を描画する */
    val bottomLeftCornerEnabled : Boolean = true,

    /** 右下角を描画する */
    val bottomRightCornerEnabled : Boolean = true,

    /** 画面上部角の角丸半径 */
    val topCornerRadius: Float = 0f,

    /** 画面下部角の角丸半径 */
    val bottomCornerRadius: Float = 0f,
)
