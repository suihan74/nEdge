package com.suihan74.nedge.models

import android.graphics.Color
import kotlinx.serialization.Serializable

/**
 * 通知表示の設定
 */
@Serializable
data class NotificationSetting (
    /** 線の色 */
    val color: Int = Color.WHITE,

    /** 線の太さ */
    val thickness: Float = 5f,

    /** ブラー効果の太さ(0で無効化) */
    val blurSize: Float = 0f,

    /**
     * 線の効果
     *
     * `DashPathEffect`, `DashPathEffect`, `CornerPathEffect`, `DiscretePathEffect`,
     * `SumPathEffect`, `ComposePathEffect`
     */
//    val pathEffect: PathEffect? = null,

    /** スクリーン輪郭線の描画設定 */
    val outlinesSetting: OutlinesSetting = OutlinesSetting(),

    /** 画面上部ノッチ描画設定 */
    val topNotchSetting: NotchSetting = NotchSetting.createInstance(NotchType.NONE),

    /** 画面下部ノッチ描画設定 */
    val bottomNotchSetting: NotchSetting = NotchSetting.createInstance(NotchType.NONE),

    /** 通知内容を表示する */
    val informationDisplayMode : InformationDisplayMode = InformationDisplayMode.FULL,
)
