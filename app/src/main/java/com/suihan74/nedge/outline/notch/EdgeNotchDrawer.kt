package com.suihan74.nedge.outline.notch

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.nedge.models.NotchSetting
import com.suihan74.nedge.models.NotchType
import com.suihan74.nedge.models.OutlinesSetting
import com.suihan74.nedge.models.RectangleNotchSetting
import com.suihan74.nedge.models.WaterDropNotchSetting

/**
 * 辺に接するノッチパスの描画
 *
 * 継承して実装するときは画面上辺に描画する処理(左から右に向かって一筆描き)だけを書けばよい。
 * 画面下部ノッチに対しては`EdgeNotchDrawer`内の処理によって反転移動を行う
 */
abstract class EdgeNotchDrawer<SettingType : NotchSetting>(
    private val displaySize: Point
) : NotchDrawer<SettingType> {
    companion object {
        /**
         * ノッチタイプごとに適切な描画クラスのインスタンスを生成してパスを描画する
         */
        fun draw(
            displaySize: Point,
            path: Path,
            thickness: Float,
            cornerRadius: Float,
            offset: Int,
            notchSetting: NotchSetting,
            outlinesSetting: OutlinesSetting
        ) {
            when (notchSetting.type) {
                NotchType.RECTANGLE -> {
                    val drawer = RectangleNotchDrawer(displaySize)
                    drawer.draw(
                        displaySize,
                        path,
                        thickness,
                        cornerRadius,
                        offset,
                        notchSetting as RectangleNotchSetting,
                        outlinesSetting
                    )
                }

                NotchType.WATER_DROP -> {
                    val drawer = WaterDropNotchDrawer(displaySize)
                    drawer.draw(
                        displaySize,
                        path,
                        thickness,
                        cornerRadius,
                        offset,
                        notchSetting as WaterDropNotchSetting,
                        outlinesSetting
                    )
                }

                else -> {}
            }
        }
    }

    /**
     * ノッチ位置によって画面上端・下端を判別して描画処理を切り替える
     */
    fun draw(
        displaySize: Point,
        path: Path,
        thickness: Float,
        cornerRadius: Float,
        offset: Int,
        notchSetting: SettingType,
        outlinesSetting: OutlinesSetting
    ) {
        val verticalCenter = displaySize.y / 2

        if (notchSetting.rect.top < verticalCenter) {
            val rect =
                if (offset > 0) Rect(notchSetting.rect).also { it.offset(0, offset) }
                else notchSetting.rect

            drawTopEdge(path, rect, thickness, cornerRadius, notchSetting, outlinesSetting)
        }
        else {
            val rect =
                if (offset > 0) Rect(notchSetting.rect).also { it.offset(0, -offset) }
                else notchSetting.rect

            drawBottomEdge(path, rect, thickness, cornerRadius, notchSetting, outlinesSetting)
        }
    }

    /**
     * 画面上端にノッチを描画する
     */
    private fun drawTopEdge(
        path: Path,
        rect: Rect,
        thickness: Float,
        cornerRadius: Float,
        notchSetting: SettingType,
        outlinesSetting: OutlinesSetting
    ) {
        val notchPath = Path().apply {
            val offset = thickness * .5f
            if (outlinesSetting.topLeftCornerEnabled) {
                moveTo(offset + cornerRadius, offset + rect.top)
            }
            else {
                moveTo(offset, offset + rect.top)
            }
        }
        draw(notchPath, rect, thickness, notchSetting)
        path.addPath(notchPath)
    }

    /**
     * 画面下端辺にノッチを描画する
     *
     * 上端に左から右に向かって描画したものを180度回転させて右下から左下に向かって描画する
     */
    private fun drawBottomEdge(
        path: Path,
        rect: Rect,
        thickness: Float,
        cornerRadius: Float,
        notchSetting: SettingType,
        outlinesSetting: OutlinesSetting
    ) {
        val notchPath = Path().apply {
            val offset = thickness * .5f
            if (outlinesSetting.bottomRightCornerEnabled) {
                moveTo(offset + cornerRadius, offset + rect.top)
            }
            else {
                moveTo(offset, offset + rect.top)
            }
        }
        val notchRect = Rect(rect.left, 0, rect.right, rect.height())
        draw(notchPath, notchRect, thickness, notchSetting)
        path.addPath(notchPath, Matrix().apply {
            preRotate(180f, notchRect.centerX().toFloat(), 0f)
            postTranslate(0f, rect.bottom.toFloat())
        })
    }
}
