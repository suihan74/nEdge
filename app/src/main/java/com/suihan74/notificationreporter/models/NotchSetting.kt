package com.suihan74.notificationreporter.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 通知表示のノッチ部分の描画設定
 */
@Serializable
sealed class NotchSetting(
    /** ノッチの種類 */
    @SerialName("notch_type")
    val type: NotchType
) {
    companion object {
        fun createInstance(type: NotchType) = when(type) {
            NotchType.NONE -> EmptyNotchSetting()

            NotchType.RECTANGLE -> RectangleNotchSetting()

            NotchType.WATER_DROP -> WaterDropNotchSetting()

            NotchType.PUNCH_HOLE -> PunchHoleNotchSetting()
        }
    }
}

// ------ //

/**
 * ノッチ設定なし
 */
@Serializable
class EmptyNotchSetting : NotchSetting(type = NotchType.NONE)

// ------ //

/**
 * 矩形ノッチ用の描画設定
 */
@Serializable
data class RectangleNotchSetting(
    /** 上部の角丸半径 */
    val majorRadius: Float = 0f,

    /** 下部の角丸半径 */
    val minorRadius: Float = 0f,

    /** 上部幅の伸縮調整 */
    val majorWidthAdjustment: Float = 0f,

    /** 下部幅の伸縮調整 */
    val minorWidthAdjustment: Float = 0f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 0f,

    ) : NotchSetting(NotchType.RECTANGLE)

// ------ //

/**
 * 水滴ノッチ用の描画設定
 */
@Serializable
data class WaterDropNotchSetting(
    /** 上部の角丸半径 */
    val majorRadius: Float = 10f,

    /** 幅の伸縮調整 */
    val widthAdjustment: Float = 0f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 10f,

) : NotchSetting(NotchType.WATER_DROP)

// ------ //

/**
 * パンチホールノッチ用の描画設定
 */
@Serializable
data class PunchHoleNotchSetting(
    /** 中心位置X */
    val cx: Float = 0f,

    /** 中心位置Y */
    val cy: Float = 0f,

    /** 半径 */
    val radius: Float = 16f,

    /** 上下辺 */
    val horizontalEdgeSize: Float = 0f,

    /** 左右辺 */
    val verticalEdgeSize: Float = 0f,
) : NotchSetting(NotchType.PUNCH_HOLE)
