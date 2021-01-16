package com.suihan74.notificationreporter.outline

import android.graphics.*
import android.os.Build
import android.view.Window
import android.widget.ImageView
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.models.NotchType
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.models.PunchHoleNotchSetting
import com.suihan74.notificationreporter.outline.notch.EdgeNotchDrawer
import com.suihan74.utilities.extensions.dp
import com.suihan74.utilities.extensions.onNot

/**
 * 通知表示を生成する
 */
class OutlineDrawer(
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
        val thickness = notificationSetting.thickness.dp

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
        notificationSetting.outlinesSetting.run {
            val topCornerRadius = topCornerRadius.dp
            val bottomCornerRadius = bottomCornerRadius.dp

            val offset = thickness / 2
            val left = offset
            val top = offset
            val right = screenWidth - offset
            val bottom = screenHeight - offset

            // top left corner
            if (topLeftCornerEnabled) {
                path.arcTo(left, top, left + topCornerRadius * 2f, top + topCornerRadius * 2, 180f, 90f, true)
            }
            else {
                path.moveTo(left + topCornerRadius, top)
            }

            // top edge
            if (topEdgeEnabled) {
                drawTopOutLine(path, thickness, notificationSetting)
            }
            else {
                path.moveTo(right - topCornerRadius, top)
            }

            // top right corner
            if (topRightCornerEdgeEnabled) {
                path.arcTo(right - topCornerRadius * 2, top, right, top + topCornerRadius * 2 + offset, 270f, 90f, true)
            }
            else {
                path.moveTo(right, top + topCornerRadius)
            }

            // right edge
            if (rightEdgeEnabled) {
                path.lineTo(right, bottom - bottomCornerRadius)
            }
            else {
                path.moveTo(right, bottom - bottomCornerRadius)
            }

            // bottom right corner
            if (bottomRightCornerEnabled) {
                path.arcTo(right - bottomCornerRadius * 2, bottom - bottomCornerRadius * 2, right, bottom, 0f, 90f, true)
            }
            else {
                path.moveTo(right - bottomCornerRadius, bottom)
            }

            // bottom edge
            if (bottomEdgeEnabled) {
                drawBottomOutLine(path, thickness, notificationSetting)
            }
            else {
                path.moveTo(left + bottomCornerRadius, bottom)
            }

            // bottom left corner
            if (bottomLeftCornerEnabled) {
                path.arcTo(left, bottom - bottomCornerRadius * 2, left + bottomCornerRadius * 2, bottom, 90f, 90f, true)
            }
            else {
                path.moveTo(left, bottom - bottomCornerRadius)
            }

            // left edge
            if (leftEdgeEnabled) {
                path.lineTo(left, top + topCornerRadius)
            }
            else {
                path.moveTo(left, top + topCornerRadius)
            }
        }

        when (notificationSetting.topNotchSetting.type) {
            NotchType.PUNCH_HOLE -> {
                drawFloatingNotch(path, thickness, notificationSetting.topNotchSetting)
            }
            else -> {}
        }

        when (notificationSetting.bottomNotchSetting.type) {
            NotchType.PUNCH_HOLE -> {
                drawFloatingNotch(path, thickness, notificationSetting.bottomNotchSetting)
            }
            else -> {}
        }
    }

    /**
     * 上辺のみ描画
     */
    private fun drawTopOutLine(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting,
    ) {
        val offset = thickness / 2
        val right = screenWidth - offset
        val topCornerRadius = notificationSetting.outlinesSetting.topCornerRadius.dp

        path.moveTo(offset + topCornerRadius, offset)
        drawTopNotch(path, thickness, topCornerRadius, notificationSetting.topNotchSetting)
        path.lineTo(right - topCornerRadius, offset)
    }

    /**
     * 下辺のみ描画
     */
    private fun drawBottomOutLine(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        val offset = thickness / 2
        val bottom = screenHeight - offset
        val right = screenWidth - offset
        val bottomCornerRadius = notificationSetting.outlinesSetting.bottomCornerRadius.dp

        path.moveTo(right - bottomCornerRadius, bottom)
        drawBottomNotch(path, thickness, bottomCornerRadius, notificationSetting.bottomNotchSetting)
        path.lineTo(offset + bottomCornerRadius, bottom)
    }

    // ------ //

    private fun drawTopNotch(
        path: Path,
        thickness: Float,
        cornerRadius: Float,
        notchSetting: NotchSetting
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        val verticalCenter = screenHeight / 2
        val rect =
            window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top < verticalCenter
            } ?: return

        EdgeNotchDrawer.draw(displayRealSize, path, rect, thickness, cornerRadius, notchSetting)
    }

    private fun drawBottomNotch(
        path: Path,
        thickness: Float,
        cornerRadius: Float,
        notchSetting: NotchSetting
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        val verticalCenter = screenHeight / 2
        val rect =
            window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top > verticalCenter
            } ?: return

        EdgeNotchDrawer.draw(displayRealSize, path, rect, thickness, cornerRadius, notchSetting)
    }

    private fun drawFloatingNotch(
        path: Path,
        thickness: Float,
        notchSetting: NotchSetting
    ) {
        when (notchSetting.type) {
            NotchType.PUNCH_HOLE ->
                drawPunchHoleNotch(path, notchSetting as PunchHoleNotchSetting)

            else -> {}
        }
    }

    // ------ //

    /**
     * パンチホールノッチ
     */
    private fun drawPunchHoleNotch(
        path: Path,
        notchSetting: PunchHoleNotchSetting
    ) {
        val cx = screenWidth * notchSetting.cx
        val cy = screenHeight * notchSetting.cy
        val radius = notchSetting.radius.dp
        if (notchSetting.horizontalEdgeSize == 0f && notchSetting.verticalEdgeSize == 0f) {
            path.addCircle(cx, cy, radius, Path.Direction.CW)
        }
        else {
            val widthHalf = (notchSetting.horizontalEdgeSize.dp + radius * 2) / 2
            val heightHalf = (notchSetting.verticalEdgeSize.dp + radius * 2) / 2
            path.addRoundRect(
                cx - widthHalf,
                cy - heightHalf,
                cx + widthHalf,
                cy + heightHalf,
                floatArrayOf(
                    radius, radius,
                    radius, radius,
                    radius, radius,
                    radius, radius
                ),
                Path.Direction.CW
            )
        }
    }
}
