package com.suihan74.notificationreporter.scenes.lockScreen

import android.graphics.*
import android.os.Build
import android.view.Window
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toRect
import com.suihan74.notificationreporter.models.*

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
        val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = notificationSetting.thickness
            color = notificationSetting.color
//            pathEffect = notificationSetting.pathEffect
        }

        // スクリーンの輪郭線
        drawOutLines(canvas, paint, notificationSetting)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // スクリーン輪郭線とノッチの縁を描画する
            notificationSetting.topNotchSetting.let { notchSetting ->
                val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                    it.top < window.decorView.height * .5f
                } ?: return@let

                when (notchSetting.type) {
                    NotchType.NONE -> {}

                    NotchType.RECTANGLE ->
                        drawTopRectangleNotch(canvas, paint, rect, notchSetting as RectangleNotchSetting)

                    NotchType.WATER_DROP ->
                        drawTopWaterDropNotch(canvas, paint, rect, notchSetting as WaterDropNotchSetting)

                    NotchType.PUNCH_HOLE ->
                        drawPunchHoleNotch(canvas, paint, notchSetting as PunchHoleNotchSetting)
                }
            }

            notificationSetting.bottomNotchSetting.let { notchSetting ->
                val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                    it.top > window.decorView.height * .5f
                } ?: return@let

                when (notchSetting.type) {
                    NotchType.NONE -> {}

                    NotchType.RECTANGLE ->
                        drawBottomRectangleNotch(canvas, paint, rect, notchSetting as RectangleNotchSetting)

                    NotchType.WATER_DROP ->
                        drawBottomWaterDropNotch(canvas, paint, rect, notchSetting as WaterDropNotchSetting)

                    NotchType.PUNCH_HOLE ->
                        drawPunchHoleNotch(canvas, paint, notchSetting as PunchHoleNotchSetting)
                }
            }
        }

        imageView.setImageBitmap(bitmap)
    }

    // ------ //

    /**
     * 通知バーの外周のラインを描画する
     */
    private fun drawOutLines(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        when (notificationSetting.outlinesSetting.type) {
            OutlinesType.NONE -> {}

            OutlinesType.FULL -> drawFullOutLines(canvas, paint, notificationSetting)

            OutlinesType.TOP -> drawOutLineOnlyTop(canvas, paint, notificationSetting)

            OutlinesType.BOTTOM -> drawOutLineOnlyBottom(canvas, paint, notificationSetting)

            OutlinesType.LEFT -> drawOutLineOnlyLeft(canvas, paint, notificationSetting)

            OutlinesType.RIGHT -> drawOutLineOnlyRight(canvas, paint, notificationSetting)

            OutlinesType.HORIZONTAL -> drawOutLinesHorizontal(canvas, paint, notificationSetting)

            OutlinesType.VERTICAL -> drawOutLinesVertical(canvas, paint, notificationSetting)
        }
    }

    /**
     * 外周を描画
     */
    private fun drawFullOutLines(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        val path = Path().apply {
            val offset = paint.strokeWidth / 2

            val cornerRadii = notificationSetting.outlinesSetting.run {
                floatArrayOf(
                    leftTopCornerRadius, leftTopCornerRadius,
                    rightTopCornerRadius, rightTopCornerRadius,
                    rightBottomCornerRadius, rightBottomCornerRadius,
                    leftBottomCornerRadius, leftBottomCornerRadius,
                )
            }

            addRoundRect(
                offset, // left
                offset, // top
                screenWidth - offset, // right
                screenHeight - offset, // bottom
                cornerRadii,
                Path.Direction.CW
            )
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 上辺のみ描画
     */
    private fun drawOutLineOnlyTop(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        val offset = paint.strokeWidth / 2
        canvas.drawLine(0f, offset, screenWidth.toFloat(), offset, paint)
    }

    /**
     * 下辺のみ描画
     */
    private fun drawOutLineOnlyBottom(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        val offset = paint.strokeWidth / 2
        canvas.drawLine(0f, screenHeight - offset, screenWidth.toFloat(), screenHeight - offset, paint)
    }

    /**
     * 上下辺のみ描画
     */
    private fun drawOutLinesHorizontal(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        drawOutLineOnlyTop(canvas, paint, notificationSetting)
        drawOutLineOnlyBottom(canvas, paint, notificationSetting)
    }

    /**
     * 左辺のみ描画
     */
    private fun drawOutLineOnlyLeft(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        val offset = paint.strokeWidth / 2
        canvas.drawLine(offset, 0f, offset, screenHeight.toFloat(), paint)
    }

    /**
     * 右辺のみ描画
     */
    private fun drawOutLineOnlyRight(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        val offset = paint.strokeWidth / 2
        canvas.drawLine(screenWidth - offset, 0f, screenWidth - offset, screenHeight.toFloat(), paint)
    }

    /**
     * 左右辺のみ描画
     */
    private fun drawOutLinesVertical(
        canvas: Canvas,
        paint: Paint,
        notificationSetting: NotificationSetting
    ) {
        drawOutLineOnlyLeft(canvas, paint, notificationSetting)
        drawOutLineOnlyRight(canvas, paint, notificationSetting)
    }

    // ------ //

    /**
     * 既に描画した内容を消去する
     *
     * ノッチ部分の輪郭線削除を行う用途
     */
    private fun Canvas.eraseRect(rect: Rect) {
        val unPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        this.drawRect(rect, unPaint)
    }

    /**
     * 通知バーの矩形ノッチ縁を描画する(画面上部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawTopRectangleNotch(
        canvas: Canvas,
        paint: Paint,
        rect: Rect,
        notchSetting: RectangleNotchSetting
    ) {
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top + offset
        val bottom = rect.bottom + offset + notchSetting.heightAdjustment

        val path = Path().apply {
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

        // ノッチ部分に被るスクリーン輪郭線を消す
        RectF(left + offset - NOTCH_SURPLUS, top - offset, right - offset + NOTCH_SURPLUS, bottom).toRect().let {
            canvas.eraseRect(it)
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーの矩形ノッチ縁を描画する(画面下部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawBottomRectangleNotch(
        canvas: Canvas,
        paint: Paint,
        rect: Rect,
        notchSetting: RectangleNotchSetting
    ) {
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top - offset - notchSetting.heightAdjustment
        val bottom = rect.bottom - offset

        val path = Path().apply {
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

        // ノッチ部分に被るスクリーン輪郭線を消す
        RectF(left + offset - NOTCH_SURPLUS, top, right - offset + NOTCH_SURPLUS, bottom + offset).toRect().let {
            canvas.eraseRect(it)
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する(画面上部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawTopWaterDropNotch(
        canvas: Canvas,
        paint: Paint,
        rect: Rect,
        notchSetting: WaterDropNotchSetting
    ) {
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top + offset
        val bottom = rect.bottom + offset + notchSetting.heightAdjustment

        val path = Path().apply {
            notchSetting.topRadius.let { r ->
                // top left
                moveTo(left - NOTCH_SURPLUS, top)
                lineTo(left - r, top)
                arcTo(left - r * 2, top, left, top + r * 2, 270f, notchSetting.topDegree, false)
            }

            notchSetting.waterDropRadius.let { r ->
                // water drop
                val degree = notchSetting.waterDropDegree
                val cx = screenWidth * .5f
                arcTo(cx - r, bottom - r * 2, cx + r, bottom, 90f + degree, -degree * 2, false)
            }

            notchSetting.topRadius.let { r ->
                // top right
                val degree = notchSetting.topDegree
                arcTo(right, top, right + r * 2, top + r * 2, 270f - degree, degree, false)
                lineTo(right + NOTCH_SURPLUS, top)
            }
        }

        // ノッチ部分に被るスクリーン輪郭線を消す
        RectF(left + offset - NOTCH_SURPLUS, top - offset, right - offset + NOTCH_SURPLUS, bottom).toRect().let {
            canvas.eraseRect(it)
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する(画面下部)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawBottomWaterDropNotch(
        canvas: Canvas,
        paint: Paint,
        rect: Rect,
        notchSetting: WaterDropNotchSetting
    ) {
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top - offset - notchSetting.heightAdjustment
        val bottom = rect.bottom - offset

        val path = Path().apply {
            notchSetting.topRadius.let { r ->
                // bottom left
                moveTo(left - NOTCH_SURPLUS, bottom)
                lineTo(left - r, bottom)
                arcTo(left - r * 2, bottom - r * 2, left, bottom, 90f, -notchSetting.topDegree, false)
            }

            notchSetting.waterDropRadius.let { r ->
                // water drop
                val degree = notchSetting.waterDropDegree
                val cx = screenWidth * .5f
                arcTo(cx - r, top, cx + r, top + r * 2, 270f - degree, degree * 2, false)
            }

            notchSetting.topRadius.let { r ->
                // top right
                val degree = notchSetting.topDegree
                arcTo(right, bottom - r * 2, right + r * 2, bottom, 90f + degree, -degree, false)
                lineTo(right + NOTCH_SURPLUS, bottom)
            }
        }

        // ノッチ部分に被るスクリーン輪郭線を消す
        RectF(left + offset - NOTCH_SURPLUS, top, right - offset + NOTCH_SURPLUS, bottom + offset).toRect().let {
            canvas.eraseRect(it)
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーのパンチホールノッチ縁を描画する
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawPunchHoleNotch(
        canvas: Canvas,
        paint: Paint,
        notchSetting: PunchHoleNotchSetting
    ) {
        canvas.drawCircle(notchSetting.cx, notchSetting.cy, notchSetting.radius, paint)
    }
}
