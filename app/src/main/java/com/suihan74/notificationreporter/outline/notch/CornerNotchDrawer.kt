package com.suihan74.notificationreporter.outline.notch

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.notificationreporter.models.CornerNotchSetting
import com.suihan74.utilities.extensions.dp
import kotlin.math.*

/**
 * 角ノッチの描画処理
 *
 * TODO 画面下部の場合
 */
class CornerNotchDrawer : NotchDrawer<CornerNotchSetting> {
    fun draw(displayRealSize: Point, path: Path, rect: Rect, thickness: Float, notchSetting: CornerNotchSetting) {
        val screenWidth = displayRealSize.x
        if (screenWidth / 2 > rect.left) {
            drawTopLeft(path, rect, thickness, notchSetting)
        }
        else {
            drawTopRight(path, rect, thickness, notchSetting)
        }
    }

    private fun drawTopLeft(path: Path, rect: Rect, thickness: Float, notchSetting: CornerNotchSetting) {
        draw(path, rect, thickness, notchSetting)
    }

    private fun drawTopRight(path: Path, rect: Rect, thickness: Float, notchSetting: CornerNotchSetting) {
        val notchPath = Path()
        val notchRect = Rect(0, 0, rect.width(), rect.height())
        draw(notchPath, notchRect, thickness, notchSetting)
        path.addPath(notchPath, Matrix().apply {
            setValues(floatArrayOf(
                -1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f
            ))
            postTranslate(rect.right.toFloat(), 0f)
        })
        val offset = thickness / 2
        val height = rect.height() * notchSetting.heightAdjustment
        val bottom = rect.bottom + offset + height
        path.moveTo(rect.right - offset, bottom + notchSetting.minorRadius.dp)
    }

    // ------ //

    override fun draw(path: Path, rect: Rect, thickness: Float, notchSetting: CornerNotchSetting) {
        val offset = thickness / 2
        val majorWidth = rect.width() * notchSetting.majorWidthAdjustment
        val minorWidth = rect.width() * min(notchSetting.minorWidthAdjustment, notchSetting.majorWidthAdjustment)
        val height = rect.height() * notchSetting.heightAdjustment

        val top = rect.top + offset
        val bottom = rect.bottom + offset + height
        val left = rect.left + offset

        val rad =
            if (majorWidth == minorWidth) (PI * .5)
            else atan((bottom - top) / (majorWidth - minorWidth) / .5)
        val deg = (180 * rad / PI).toFloat()

        val sin = sin(rad)
        val cos = cos(rad)
        val tanHalf = tan(rad / 2)

        notchSetting.minorRadius.dp.let { r ->
            // left to notch corner
            path.arcTo(left, bottom, left + r * 2, bottom + r * 2, 180f, 90f, false)
            // bottom edge
            path.lineTo(left + minorWidth, bottom)
        }

        notchSetting.middleRadius.dp.let { r ->
            val right = rect.right + offset + minorWidth * .5f
            val cy = bottom - r
            val rcx = (right - r * tanHalf).toFloat()
            // bottom right corner
            path.arcTo(rcx - r, cy - r, rcx + r, cy + r, 90f, -deg, false)
        }

        notchSetting.majorRadius.dp.let { r ->
            val right = rect.right + offset + majorWidth * .5f
            val cx = right + r * tanHalf
            val cy = top + r
            val rex = (cx - r * sin).toFloat()
            val rey = (cy - r * cos).toFloat()

            // right edge
            path.lineTo(rex, rey)
            // top right corner
            cx.toFloat().let {
                path.arcTo(it - r, cy - r, it + r, cy + r, 270f - deg, deg, false)
            }
        }
    }
}
