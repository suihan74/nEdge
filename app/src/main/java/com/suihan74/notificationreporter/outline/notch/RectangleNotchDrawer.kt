package com.suihan74.notificationreporter.outline.notch

import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.notificationreporter.models.RectangleNotchSetting
import com.suihan74.utilities.extensions.dp
import kotlin.math.*

/**
 * 矩形ノッチの描画処理
 */
class RectangleNotchDrawer(displaySize: Point) : EdgeNotchDrawer<RectangleNotchSetting>(displaySize) {
    override fun draw(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: RectangleNotchSetting
    ) {
        val offset = thickness / 2
        val majorWidth = rect.width() * notchSetting.majorWidthAdjustment
        val minorWidth = rect.width() * min(notchSetting.minorWidthAdjustment, notchSetting.majorWidthAdjustment)
        val height = rect.height() * notchSetting.heightAdjustment

        val top = rect.top + offset
        val bottom = rect.bottom + offset + height

        val rad =
            if (majorWidth == minorWidth) (PI * .5)
            else atan((bottom - top) / (majorWidth - minorWidth) / .5)
        val deg = (180 * rad / PI).toFloat()

        val sin = sin(rad)
        val cos = cos(rad)
        val tanHalf = tan(rad / 2)

        path.run {
            notchSetting.majorRadius.dp.let { r ->
                val left = rect.left - offset - majorWidth * .5f
                val cx = (left - r * tanHalf).toFloat()
                val cy = top + r

                // top edge (left of the notch)
                lineTo(cx, top)
                // top left corner
                arcTo(cx - r, cy - r, cx + r, cy + r, 270f, deg, false)
            }

            notchSetting.minorRadius.dp.let { r ->
                val left = rect.left - offset - minorWidth * .5f
                val right = rect.right + offset + minorWidth * .5f
                val cy = bottom - r
                val lcx = left + r * tanHalf
                val rcx = (right - r * tanHalf).toFloat()
                val lex = (lcx - r * sin).toFloat()
                val ley = (cy + r * cos).toFloat()

                // left edge
                lineTo(lex, ley)
                // bottom left corner
                lcx.toFloat().let {
                    arcTo(it - r, cy - r, it + r, cy + r, 90f + deg, -deg, false)
                }
                // bottom edge
                lineTo(rcx, bottom)
                // bottom right corner
                arcTo(rcx - r, cy - r, rcx + r, cy + r, 90f, -deg, false)
            }

            notchSetting.majorRadius.dp.let { r ->
                val right = rect.right + offset + majorWidth * .5f
                val cx = right + r * tanHalf
                val cy = top + r
                val rex = (cx - r * sin).toFloat()
                val rey = (cy - r * cos).toFloat()

                // right edge
                lineTo(rex, rey)
                // top right corner
                cx.toFloat().let {
                    arcTo(it - r, cy - r, it + r, cy + r, 270f - deg, deg, false)
                }
            }
        }
    }
}
