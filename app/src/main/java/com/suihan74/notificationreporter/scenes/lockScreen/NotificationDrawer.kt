package com.suihan74.notificationreporter.scenes.lockScreen

import android.graphics.*
import android.os.Build
import android.view.Window
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.suihan74.notificationreporter.models.*

/**
 * 通知表示を生成する
 */
class NotificationDrawer(
    private val window: Window
) {
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

        // ノッチの縁
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            /*
            // テスト用の矩形ノッチ設定
            val notchSetting = RectangleNotchSetting(
                leftTopRadius = 12.dp,
                rightTopRadius = 12.dp,
                leftBottomRadius = 26.dp,
                rightBottomRadius = 26.dp,
                widthAdjustment = 18f,
                heightAdjustment = 0f
            )
            drawRectangleNotch(canvas, paint, notchSetting)
            */

            /*
            // テスト用のパンチホールノッチ設定
            val notchSetting = PunchHoleNotchSetting(
                cx = 84f,
                cy = 84f,
                radius = 40f + 3.dp
            )
            drawNotificationBarPunchHoleNotch(canvas, paint, notchSetting)
            */
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
     * 通知バーのノッチ縁を描画する
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawRectangleNotch(
        canvas: Canvas,
        paint: Paint,
        notchSetting: RectangleNotchSetting
    ) {
        val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull() ?: return

        val surplus = 50f
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top + offset
        val bottom = rect.bottom + offset + notchSetting.heightAdjustment

        val path = Path().apply {
            notchSetting.leftTopRadius.let { r ->
                // top left
                moveTo(left - surplus, top)
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
                lineTo(right + surplus, top)
            }
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawWaterDropNotch(
        canvas: Canvas,
        paint: Paint,
        notchSetting: WaterDropNotchSetting
    ) {
        val rect = window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull() ?: return

        val surplus = 50f
        val offset = paint.strokeWidth / 2

        val left = rect.left - offset + notchSetting.widthAdjustment
        val right = rect.right + offset - notchSetting.widthAdjustment
        val top = rect.top + offset
        val bottom = rect.bottom + offset + notchSetting.heightAdjustment

        val path = Path().apply {
            notchSetting.leftTopRadius.let { r ->
                // top left
                moveTo(left - surplus, top)
                lineTo(left - r, top)
                arcTo(left - r * 2, top, left, top + r * 2, 270f, 90f, true)
                // height adjustment
                if (top + r != bottom - r) {
                    lineTo(left, bottom - r)
                }
            }

            notchSetting.waterDropRadius.let { r ->
                // water drop
                arcTo(left, bottom - r * 2, right, bottom, 180f, -180f, true)
            }

            notchSetting.rightTopRadius.let { r ->
                // height adjustment
                if (top + r != bottom - r) {
                    lineTo(right, top + r)
                }
                // top right
                arcTo(right, top, right + r * 2, top + r * 2, 180f, 90f, true)
                lineTo(right + surplus, top)
            }
        }

        canvas.drawPath(path, paint)
    }

    /**
     * 通知バーの水滴ノッチ縁を描画する
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun drawNotificationBarPunchHoleNotch(
        canvas: Canvas,
        paint: Paint,
        notchSetting: PunchHoleNotchSetting
    ) {
        canvas.drawCircle(notchSetting.cx, notchSetting.cy, notchSetting.radius, paint)
    }
}
