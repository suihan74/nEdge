package com.suihan74.nedge.models

import android.graphics.Rect
import com.suihan74.utilities.serialization.RectSerializer
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
    @Serializable(RectSerializer::class)
    abstract val rect: Rect

    companion object {
        /**
         * ノッチ種類に対応した設定の実体を生成
         */
        fun createInstance(type: NotchType, rect: Rect? = null) = when(type) {
            NotchType.NONE -> EmptyNotchSetting()

            NotchType.RECTANGLE -> RectangleNotchSetting(rect!!)

            NotchType.WATER_DROP -> WaterDropNotchSetting(rect!!)

            NotchType.PUNCH_HOLE -> PunchHoleNotchSetting(rect!!)

            NotchType.CORNER -> CornerNotchSetting(rect!!)
        }

        val None = createInstance(NotchType.NONE)
    }
}

/**
 * ノッチ設定なし
 */
@Serializable
class EmptyNotchSetting : NotchSetting(type = NotchType.NONE) {
    @Serializable(RectSerializer::class)
    override val rect: Rect = Rect()
}

// ------ //

/**
 * 矩形ノッチ用の描画設定
 */
@Serializable
data class RectangleNotchSetting(
    @Serializable(RectSerializer::class)
    override val rect: Rect,

    /** 上部の角丸半径 */
    val majorRadius: Float = 0f,

    /** 下部の角丸半径 */
    val minorRadius: Float = 0f,

    /** 上部幅の伸縮調整 */
    val majorWidthAdjustment: Float = 0f,

    /** 下部幅の伸縮調整 */
    val minorWidthAdjustment: Float = .7f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 0f

) : NotchSetting(NotchType.RECTANGLE)

// ------ //

/**
 * 水滴ノッチ用の描画設定
 */
@Serializable
data class WaterDropNotchSetting(
    @Serializable(RectSerializer::class)
    override val rect: Rect,

    /** 上部の角丸半径 */
    val majorRadius: Float = 10f,

    /** 幅の伸縮調整 */
    val widthAdjustment: Float = 0f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 10f

) : NotchSetting(NotchType.WATER_DROP)

// ------ //

/**
 * パンチホールノッチ用の描画設定
 */
@Serializable
data class PunchHoleNotchSetting(
    @Serializable(RectSerializer::class)
    override val rect: Rect,

    /** 中心位置X */
    val cx: Float = 0f,

    /** 中心位置Y */
    val cy: Float = 0f,

    /** 半径 */
    val radius: Float = 16f,

    /** 上下辺 */
    val horizontalEdgeSize: Float = 0f,

    /** 左右辺 */
    val verticalEdgeSize: Float = 0f

) : NotchSetting(NotchType.PUNCH_HOLE)

// ------ //

/**
 * 角部ノッチ用の描画設定
 */
@Serializable
data class CornerNotchSetting(
    @Serializable(RectSerializer::class)
    override val rect: Rect,

    /** 横方向の辺からノッチへの角丸 */
    val majorRadius: Float = 0f,

    /** 画面内側の角丸 */
    val middleRadius: Float = 0f,

    /** ノッチから縦方向の辺への角丸 */
    val minorRadius: Float = 0f,

    /** 上部幅の伸縮調整 */
    val majorWidthAdjustment: Float = 0f,

    /** 下部幅の伸縮調整 */
    val minorWidthAdjustment: Float = .7f,

    /** 高さの伸縮調整 */
    val heightAdjustment: Float = 0f

) : NotchSetting(NotchType.CORNER)
