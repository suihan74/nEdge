package com.suihan74.nedge.outline.notch

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.nedge.models.NotchSetting
import com.suihan74.nedge.models.NotchType
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
            notchSetting: NotchSetting
        ) {
            when (notchSetting.type) {
                NotchType.RECTANGLE -> {
                    val drawer = RectangleNotchDrawer(displaySize)
                    drawer.draw(displaySize, path, thickness, cornerRadius, notchSetting as RectangleNotchSetting)
                }

                NotchType.WATER_DROP -> {
                    val drawer = WaterDropNotchDrawer(displaySize)
                    drawer.draw(displaySize, path, thickness, cornerRadius, notchSetting as WaterDropNotchSetting)
                }

                else -> {}
            }
        }
    }

    /**
     * ノッチ位置によって画面上端・下端を判別して描画処理を切り替える
     */
    fun draw(displaySize: Point, path: Path, thickness: Float, cornerRadius: Float, notchSetting: SettingType) {
        val verticalCenter = displaySize.y / 2
        val rect = notchSetting.rect
        if (rect.top < verticalCenter) {
            drawTopEdge(path, rect, thickness, cornerRadius, notchSetting)
        }
        else {
            drawBottomEdge(path, rect, thickness, cornerRadius, notchSetting)
        }
    }

    /**
     * 画面上端にノッチを描画する
     */
    private fun drawTopEdge(path: Path, rect: Rect, thickness: Float, cornerRadius: Float, notchSetting: SettingType) {
        val notchPath = Path().apply {
            val offset = thickness * .5f
            moveTo(offset + cornerRadius, offset)
        }
        draw(notchPath, rect, thickness, notchSetting)
        path.addPath(notchPath)
    }

    /**
     * 画面下端辺にノッチを描画する
     *
     * 上端に左から右に向かって描画したものを180度回転させて右下から左下に向かって描画する
     */
    private fun drawBottomEdge(path: Path, rect: Rect, thickness: Float, cornerRadius: Float, notchSetting: SettingType) {
        val notchPath = Path().apply {
            val offset = thickness * .5f
            moveTo(offset + cornerRadius, offset)
        }
        val notchRect = Rect(rect.left, 0, rect.right, rect.bottom - rect.top)
        draw(notchPath, notchRect, thickness, notchSetting)
        path.addPath(notchPath, Matrix().apply {
            preRotate(180f, notchRect.centerX().toFloat(), 0f)
            postTranslate(0f, rect.bottom.toFloat())
        })
    }
}
