package com.suihan74.notificationreporter.scenes.lockScreen

import android.graphics.*
import android.os.Build
import android.view.Window
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.suihan74.notificationreporter.models.*
import com.suihan74.utilities.extensions.onNot
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 通知表示を生成する
 */
class NotificationDrawer(
    private val window: Window
) {
    companion object {
        /** 輪郭線とノッチ縁を繋いで描画する際のノッチ部分の余剰の長さ */
        private const val NOTCH_SURPLUS = 50f
    }

    /** 画面サイズ */
    private val displayRealSize : Point by lazy {
        Point().also {
            window.decorView.display.getRealSize(it)
        }
    }

    /** 画面の幅 */
    private val screenWidth: Int by lazy {
        displayRealSize.x
    }

    /** 画面の高さ */
    private val screenHeight: Int by lazy {
        displayRealSize.y
    }

    // ------ //

    /** 通知バーを描画する */
    fun draw(
        imageView: ImageView,
        notificationSetting: NotificationSetting
    ) {
        val thickness = notificationSetting.thickness

        val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = thickness
            color = notificationSetting.color
            notificationSetting.blurSize.onNot(0f) {
                maskFilter = BlurMaskFilter(it, BlurMaskFilter.Blur.SOLID)
            }
        }

        val path = Path()

        // スクリーン輪郭線
        drawOutLines(path, thickness, notificationSetting)

        // ノッチ輪郭線
        drawNotches(path, thickness, notificationSetting)

        canvas.drawPath(path, paint)
        imageView.setImageBitmap(bitmap)
    }

    // ------ //

    /**
     * パスにスクリーン輪郭線を反映
     */
    private fun drawOutLines(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        when (notificationSetting.outlinesSetting.type) {
            OutlinesType.NONE -> {}

            OutlinesType.FULL -> drawFullOutLines(path, thickness, notificationSetting)

            OutlinesType.TOP -> drawOutLineOnlyTop(path, thickness)

            OutlinesType.BOTTOM -> drawOutLineOnlyBottom(path, thickness)

            OutlinesType.LEFT -> drawOutLineOnlyLeft(path, thickness)

            OutlinesType.RIGHT -> drawOutLineOnlyRight(path, thickness)

            OutlinesType.HORIZONTAL -> drawOutLinesHorizontal(path, thickness)

            OutlinesType.VERTICAL -> drawOutLinesVertical(path, thickness)
        }
    }

    /**
     * 外周を描画
     */
    private fun drawFullOutLines(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        val offset = thickness / 2

        val cornerRadii = notificationSetting.outlinesSetting.run {
            floatArrayOf(
                topCornerRadius, topCornerRadius,
                topCornerRadius, topCornerRadius,
                bottomCornerRadius, bottomCornerRadius,
                bottomCornerRadius, bottomCornerRadius,
            )
        }

        path.addRoundRect(
            offset, // left
            offset, // top
            screenWidth - offset, // right
            screenHeight - offset, // bottom
            cornerRadii,
            Path.Direction.CW
        )
    }

    /**
     * 上辺のみ描画
     */
    private fun drawOutLineOnlyTop(
        path: Path,
        thickness: Float
    ) {
        val offset = thickness / 2
        path.moveTo(offset, offset)
        path.lineTo(screenWidth - offset, offset)
    }

    /**
     * 下辺のみ描画
     */
    private fun drawOutLineOnlyBottom(
        path: Path,
        thickness: Float
    ) {
        val offset = thickness / 2
        val y = screenHeight - offset
        path.moveTo(offset, y)
        path.lineTo(screenWidth - offset, y)
    }

    /**
     * 上下辺のみ描画
     */
    private fun drawOutLinesHorizontal(
        path: Path,
        thickness: Float
    ) {
        drawOutLineOnlyTop(path, thickness)
        drawOutLineOnlyBottom(path, thickness)
    }

    /**
     * 左辺のみ描画
     */
    private fun drawOutLineOnlyLeft(
        path: Path,
        thickness: Float
    ) {
        val offset = thickness / 2
        path.moveTo(offset, offset)
        path.lineTo(offset, screenHeight.toFloat() - offset)
    }

    /**
     * 右辺のみ描画
     */
    private fun drawOutLineOnlyRight(
        path: Path,
        thickness: Float
    ) {
        val offset = thickness / 2
        val x = screenWidth - offset
        path.moveTo(x, offset)
        path.lineTo(x, screenHeight - offset)
    }

    /**
     * 左右辺のみ描画
     */
    private fun drawOutLinesVertical(
        path: Path,
        thickness: Float
    ) {
        drawOutLineOnlyLeft(path, thickness)
        drawOutLineOnlyRight(path, thickness)
    }

    // ------ //

    private fun Path.eraseRect(left: Float, top: Float, right: Float, bottom: Float) {
        val p = Path().apply {
            val scLeft = 0f
            val scRight = screenWidth.toFloat()
            val scTop = 0f
            val scBottom = screenHeight.toFloat()
            addRect(RectF(scLeft, scTop, left, scBottom), Path.Direction.CW)
            addRect(RectF(left, scTop, right, top), Path.Direction.CW)
            addRect(RectF(right, scTop, scRight, scBottom), Path.Direction.CW)
            addRect(RectF(left, bottom, right, scBottom), Path.Direction.CW)
        }
        this.op(p, Path.Op.REVERSE_DIFFERENCE)
    }

    /**
     * パスにノッチ輪郭線を反映
     */
    private fun drawNotches(path: Path, thickness: Float, notificationSetting: NotificationSetting) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        // スクリーン輪郭線とノッチの縁を描画する
        notificationSetting.topNotchSetting.let { notchSetting ->
            val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top < window.decorView.height * .5f
            } ?: return@let

            when (notchSetting.type) {
                NotchType.NONE -> {}

                NotchType.RECTANGLE ->
                    drawTopRectangleNotch(path, rect, thickness, notchSetting as RectangleNotchSetting)

                NotchType.WATER_DROP ->
                    drawTopWaterDropNotch(path, rect, thickness, notchSetting as WaterDropNotchSetting)

                NotchType.PUNCH_HOLE ->
                    drawPunchHoleNotch(path, notchSetting as PunchHoleNotchSetting)
            }
        }

        notificationSetting.bottomNotchSetting.let { notchSetting ->
            val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top > window.decorView.height * .5f
            } ?: return@let

            when (notchSetting.type) {
                NotchType.NONE -> {}

                NotchType.RECTANGLE ->
                    drawBottomRectangleNotch(path, rect, thickness, notchSetting as RectangleNotchSetting)

                NotchType.WATER_DROP ->
                    drawBottomWaterDropNotch(path, rect, thickness, notchSetting as WaterDropNotchSetting)

                NotchType.PUNCH_HOLE ->
                    drawPunchHoleNotch(path, notchSetting as PunchHoleNotchSetting)
            }
        }
    }

    /**
     * 通知バーの矩形ノッチ縁を描画する(画面上部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawTopRectangleNotch(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: RectangleNotchSetting
    ) {
        val offset = thickness / 2
        val widthAdjustmentPx = rect.width() * .5f * notchSetting.widthAdjustment
        val heightAdjustmentPx = rect.height() * notchSetting.heightAdjustment

        val left = rect.left - offset - widthAdjustmentPx
        val right = rect.right + offset + widthAdjustmentPx
        val top = rect.top + offset
        val bottom = rect.bottom + offset + heightAdjustmentPx

        // ノッチ部分に被るスクリーン輪郭線を消す
        path.eraseRect(left + offset - NOTCH_SURPLUS, top - offset, right - offset + NOTCH_SURPLUS, bottom)

        path.run {
            notchSetting.leftTopRadius.let { r ->
                // top left
                moveTo(left - NOTCH_SURPLUS, top)
                lineTo(left - r, top)
                arcTo(left - r * 2, top, left, top + r * 2, 270f, 90f, true)
            }

            notchSetting.leftBottomRadius.let { r ->
                // bottom left
                lineTo(left, bottom - r)
                arcTo(left, bottom - r * 2, left + r * 2, bottom, 180f, -90f, true)
            }

            notchSetting.rightBottomRadius.let { r ->
                // bottom edge
                lineTo(right - r, bottom)

                // bottom right
                arcTo(right - r * 2, bottom - r * 2, right, bottom, 90f, -90f, true)
            }

            notchSetting.rightTopRadius.let { r ->
                // top right
                lineTo(right, top + r)
                arcTo(right, top, right + r * 2, top + r * 2, 180f, 90f, true)
                lineTo(right + NOTCH_SURPLUS, top)
            }
        }
    }

    /**
     * 通知バーの矩形ノッチ縁を描画する(画面下部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawBottomRectangleNotch(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: RectangleNotchSetting
    ) {
        val offset = thickness / 2
        val widthAdjustmentPx = rect.width() * .5f * notchSetting.widthAdjustment
        val heightAdjustmentPx = rect.height() * notchSetting.heightAdjustment

        val left = rect.left - offset - widthAdjustmentPx
        val right = rect.right + offset + widthAdjustmentPx
        val top = rect.top - offset - heightAdjustmentPx
        val bottom = rect.bottom - offset

        // ノッチ部分に被るスクリーン輪郭線を消す
        path.eraseRect(left + offset - NOTCH_SURPLUS, top - offset, right - offset + NOTCH_SURPLUS, bottom)

        path.apply {
            notchSetting.leftTopRadius.let { r ->
                // bottom left
                moveTo(left - NOTCH_SURPLUS, bottom)
                lineTo(left - r, bottom)
                arcTo(left - r * 2, bottom - r * 2, left, bottom, 90f, -90f, true)
            }

            notchSetting.leftBottomRadius.let { r ->
                // top left
                lineTo(left, top + r)
                arcTo(left, top, left + r * 2, top + r * 2, 180f, 90f, true)
            }

            notchSetting.rightBottomRadius.let { r ->
                // top edge
                lineTo(right - r, top)

                // bottom right
                arcTo(right - r * 2, top, right, top + r * 2, 270f, 90f, true)
            }

            notchSetting.rightTopRadius.let { r ->
                // bottom right
                lineTo(right, bottom - r)
                arcTo(right, bottom - r * 2, right + r * 2, bottom, 180f, -90f, true)
                lineTo(right + NOTCH_SURPLUS, bottom)
            }
        }
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する(画面上部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawTopWaterDropNotch(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: WaterDropNotchSetting
    ) {
        val offset = thickness / 2
        val widthAdjustmentPx = rect.width() * .5f * notchSetting.widthAdjustment

        val topRadius = notchSetting.topRadius
        val topDegree = notchSetting.heightAdjustment

        val left = rect.left - offset - widthAdjustmentPx
        val right = rect.right + offset + widthAdjustmentPx
        val top = rect.top + offset
        val bottom = rect.bottom + offset

        path.apply {
            val lx = left - topRadius
            val rx = right + topRadius
            val ly = top + topRadius

            // ノッチ部分に被るスクリーン輪郭線を消す
            path.eraseRect(lx, top - offset, rx, bottom)

            // top left
            moveTo(lx - NOTCH_SURPLUS, top)
            lineTo(lx, top)
            arcTo(lx - topRadius, top, lx + topRadius, ly + topRadius, 270f, topDegree, false)

            // water drop
            val (sin, cos) = (PI * topDegree / 180f).let {
                sin(it) to cos(it)
            }
            val r = ((rx - lx - 2 * topRadius * sin) / (2 * sin)).toFloat()
            val cx = (lx + (topRadius + r) * sin).toFloat()
            val cy = (ly - (topRadius + r) * cos).toFloat()
            arcTo(cx - r, cy - r, cx + r, cy + r, 90f + topDegree, -topDegree * 2, false)

            // top right
            arcTo(right, top, rx + topRadius, ly + topRadius, 270f - topDegree, topDegree, false)
            lineTo(rx + NOTCH_SURPLUS, top)
        }
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する(画面下部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawBottomWaterDropNotch(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: WaterDropNotchSetting
    ) {
        // TODO
    }

    /**
     * 通知バーのパンチホールノッチ縁を描画する
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawPunchHoleNotch(
        path: Path,
        notchSetting: PunchHoleNotchSetting
    ) {
        path.addCircle(notchSetting.cx, notchSetting.cy, notchSetting.radius, Path.Direction.CW)
    }
}
