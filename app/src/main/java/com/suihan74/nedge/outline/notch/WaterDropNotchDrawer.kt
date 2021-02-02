package com.suihan74.nedge.outline.notch

import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.nedge.models.WaterDropNotchSetting
import com.suihan74.utilities.extensions.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 水滴ノッチの描画処理
 */
class WaterDropNotchDrawer(displaySize: Point) : EdgeNotchDrawer<WaterDropNotchSetting>(displaySize) {
    override fun draw(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: WaterDropNotchSetting
    ) {
        val offset = thickness / 2
        val widthAdjustmentPx = rect.width() * .5f * notchSetting.widthAdjustment

        val rootRadius = notchSetting.majorRadius.dp
        val rootDegree = notchSetting.heightAdjustment.dp

        val left = rect.left - offset - widthAdjustmentPx
        val right = rect.right + offset + widthAdjustmentPx
        val top = rect.top + offset

        val lx = left - rootRadius
        val rx = right + rootRadius
        val wy = top + rootRadius

        path.apply {
            // top left
            lineTo(lx, top)
            arcTo(lx - rootRadius, top, lx + rootRadius, wy + rootRadius, 270f, rootDegree, false)

            // water drop
            val (sin, cos) = (PI * rootDegree / 180f).let {
                sin(it) to cos(it)
            }
            val r = ((rx - lx - 2 * rootRadius * sin) / (2 * sin)).toFloat()
            val cx = (lx + (rootRadius + r) * sin).toFloat()
            val cy = (wy - (rootRadius + r) * cos).toFloat()
            arcTo(cx - r, cy - r, cx + r, cy + r, 90f + rootDegree, -rootDegree * 2, false)

            // top right
            arcTo(right, top, rx + rootRadius, wy + rootRadius, 270f - rootDegree, rootDegree, false)
        }
    }
}
