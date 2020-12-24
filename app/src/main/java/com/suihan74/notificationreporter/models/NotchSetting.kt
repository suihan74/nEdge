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
    /** 左上の角丸半径 */
    val leftTopRadius: Float = 0f,

    /** 右上の角丸半径 */
    val rightTopRadius: Float = 0f,

    /** 左下の角丸半径 */
    val leftBottomRadius: Float = 0f,

    /** 右下の角丸半径 */
    val rightBottomRadius: Float = 0f,

    /** 幅の伸縮調整 */
    val widthAdjustment: Float = 0f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 0f,

) : NotchSetting(NotchType.RECTANGLE)

// ------ //

/**
 * 水滴ノッチ用の描画設定
 */
@Serializable
data class WaterDropNotchSetting(
    /** 左上の角丸半径 */
    val topRadius: Float = 0f,

    /** 中央下の角丸半径 */
    val waterDropRadius: Float = 0f,

    /** 左上角丸の角度 */
    val topDegree: Float = 0f,

    /** 水滴の角度 */
    val waterDropDegree: Float = 0f,

    /** 水滴の高さ */
    val height: Float = 0f,

    /** 幅の伸縮調整 */
    val widthAdjustment: Float = 0f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 0f,

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

) : NotchSetting(NotchType.PUNCH_HOLE)
