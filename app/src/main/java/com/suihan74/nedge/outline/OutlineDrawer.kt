package com.suihan74.nedge.outline

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.Window
import android.widget.ImageView
import com.suihan74.nedge.models.CornerNotchSetting
import com.suihan74.nedge.models.NotchSetting
import com.suihan74.nedge.models.NotchType
import com.suihan74.nedge.models.NotificationSetting
import com.suihan74.nedge.models.OutlinesSetting
import com.suihan74.nedge.models.PunchHoleNotchSetting
import com.suihan74.nedge.outline.notch.CornerNotchDrawer
import com.suihan74.nedge.outline.notch.EdgeNotchDrawer
import com.suihan74.nedge.outline.notch.PunchHoleNotchDrawer
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
        Point().also { p ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.windowManager.currentWindowMetrics.bounds.also { bounds ->
                    p.set(bounds.width(), bounds.height())
                }
            }
            else {
                @Suppress("deprecation")
                window.decorView.display.getRealSize(p)
            }
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
        drawTopLeftCorner(path, thickness, notificationSetting)
        drawTopOutLine(path, thickness, notificationSetting)
        drawTopRightCorner(path, thickness, notificationSetting)
        drawRightOutLine(path, thickness, notificationSetting)
        drawBottomRightCorner(path, thickness, notificationSetting)
        drawBottomOutLine(path, thickness, notificationSetting)
        drawBottomLeftCorner(path, thickness, notificationSetting)
        drawLeftOutline(path, thickness, notificationSetting)

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
     * 上辺の描画
     */
    private fun drawTopOutLine(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting,
    ) {
        val leftCornerEnabled = notificationSetting.outlinesSetting.topLeftCornerEnabled
        val rightCornerEnabled = notificationSetting.outlinesSetting.topRightCornerEnabled
        notificationSetting.outlinesSetting.let {
            val offset = thickness / 2
            val top = offset + it.topEdgeOffset
            val topCornerRadius = it.topCornerRadius.dp
            val left =
                if (topCornerRadius == 0f || !leftCornerEnabled) 0f
                else offset + topCornerRadius
            val right =
                if (topCornerRadius == 0f || !rightCornerEnabled) screenWidth.toFloat()
                else screenWidth - offset - topCornerRadius

            if (it.topEdgeEnabled) {
                path.moveTo(left, top)
                drawTopNotch(
                    path,
                    thickness,
                    topCornerRadius,
                    it.topEdgeOffset,
                    notificationSetting.topNotchSetting,
                    notificationSetting.outlinesSetting
                )
                path.lineTo(right, top)
            }
            else {
                path.moveTo(right, top)
            }
            path.rMoveTo(-offset, -offset)
        }
    }

    /**
     * 下辺の描画
     */
    private fun drawBottomOutLine(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        val bottomLeftCornerEnabled = notificationSetting.outlinesSetting.bottomLeftCornerEnabled
        val bottomRightCornerEnabled = notificationSetting.outlinesSetting.bottomRightCornerEnabled
        notificationSetting.outlinesSetting.let {
            val offset = thickness / 2
            val bottom = screenHeight - offset - it.bottomEdgeOffset
            val bottomCornerRadius = it.bottomCornerRadius.dp
            val left =
                if (bottomCornerRadius == 0f || !bottomLeftCornerEnabled) 0f
                else offset + bottomCornerRadius
            val right =
                if (bottomCornerRadius == 0f || !bottomRightCornerEnabled) screenWidth.toFloat()
                else screenWidth - offset - bottomCornerRadius

            if (it.bottomEdgeEnabled) {
                path.moveTo(right, bottom)
                drawBottomNotch(
                    path,
                    thickness,
                    bottomCornerRadius,
                    it.bottomEdgeOffset,
                    notificationSetting.bottomNotchSetting,
                    notificationSetting.outlinesSetting
                )
                path.lineTo(left, bottom)
            }
            else {
                path.moveTo(left, bottom)
            }
            path.rMoveTo(offset, offset)
        }
    }

    /**
     * 右辺の描画
     */
    private fun drawRightOutLine(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        val bottomRightCornerEnabled = notificationSetting.outlinesSetting.bottomRightCornerEnabled
        notificationSetting.outlinesSetting.let {
            val bottomCornerRadius = it.bottomCornerRadius.dp
            val offset = thickness / 2
            val bottomEdgeOffset = it.bottomEdgeOffset.toFloat()
            val right = screenWidth - offset
            val bottom =
                if (bottomCornerRadius == 0f || !bottomRightCornerEnabled) screenHeight - bottomEdgeOffset
                else screenHeight - offset - bottomEdgeOffset - bottomCornerRadius

            if (it.rightEdgeEnabled) {
                path.lineTo(right, bottom)
            }
            else {
                path.moveTo(right, bottom)
            }
            path.rMoveTo(0f, -offset)
        }
    }

    /**
     * 左辺の描画
     */
    private fun drawLeftOutline(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        val topLeftCornerEnabled = notificationSetting.outlinesSetting.topLeftCornerEnabled
        notificationSetting.outlinesSetting.let {
            val topCornerRadius = it.topCornerRadius.dp
            val offset = thickness / 2
            val topEdgeOffset = it.topEdgeOffset.toFloat()
            val top =
                if (topCornerRadius == 0f || !topLeftCornerEnabled) topEdgeOffset
                else offset + topEdgeOffset + topCornerRadius

            if (it.leftEdgeEnabled) {
                path.lineTo(offset, top)
            }
            else {
                path.moveTo(offset, top)
            }
            path.rMoveTo(0f, offset)
        }
    }

    private fun drawTopLeftCorner(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting,
    ) {
//        if (notificationSetting.topNotchSetting.type == NotchType.CORNER) {
//            drawTopCornerNotch(path, thickness, notificationSetting.topNotchSetting)
//        }
//        else {
        notificationSetting.outlinesSetting.let {
            val topCornerRadius = it.topCornerRadius.dp
            if (topCornerRadius == 0f) {
                return
            }
            val left = thickness / 2
            val top = left + it.topEdgeOffset
            val right = left + topCornerRadius * 2f
            val bottom = top + topCornerRadius * 2f

            if (it.topLeftCornerEnabled) {
                path.arcTo(left, top, right, bottom, 180f, 90f, true)
            }
            else {
                path.moveTo(left, top)
//                path.moveTo(left + topCornerRadius, top)
            }
        }
    //        }
    }

    private fun drawTopRightCorner(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting,
    ) {
        if (notificationSetting.topNotchSetting.type == NotchType.CORNER) {
            drawTopCornerNotch(path, thickness, notificationSetting.topNotchSetting)
            return
        }

        notificationSetting.outlinesSetting.let {
            val topCornerRadius = it.topCornerRadius.dp
            if (topCornerRadius == 0f) {
                return
            }
            val offset = thickness / 2
            val top = offset + it.topEdgeOffset
            val bottom = top + topCornerRadius * 2
            val right = screenWidth - offset
            val left = right - topCornerRadius * 2

            if (it.topRightCornerEnabled) {
                path.arcTo(left, top, right, bottom, 270f, 90f, true)
            }
            else {
                path.moveTo(right, top)
//                path.moveTo(right, top + topCornerRadius)
            }
        }
//        }
    }

    private fun drawBottomRightCorner(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        notificationSetting.outlinesSetting.let {
            val bottomCornerRadius = it.bottomCornerRadius.dp
            if (bottomCornerRadius == 0f) {
                return
            }
            val offset = thickness / 2
            val bottom = screenHeight - offset - it.bottomEdgeOffset
            val top = bottom - bottomCornerRadius * 2
            val right = screenWidth - offset
            val left = right - bottomCornerRadius * 2

            if (it.bottomRightCornerEnabled) {
                path.arcTo(left, top, right, bottom, 0f, 90f, true)
            }
            else {
                path.moveTo(right, bottom)
//                path.moveTo(right - bottomCornerRadius, bottom)
            }
        }
    }

    private fun drawBottomLeftCorner(
        path: Path,
        thickness: Float,
        notificationSetting: NotificationSetting
    ) {
        notificationSetting.outlinesSetting.let {
            val bottomCornerRadius = it.bottomCornerRadius.dp
            if (bottomCornerRadius == 0f) {
                return
            }
            val offset = thickness / 2
            val bottom = screenHeight - offset - it.bottomEdgeOffset
            val top = bottom - bottomCornerRadius * 2
            val right = offset + bottomCornerRadius * 2

            if (it.bottomLeftCornerEnabled) {
                path.arcTo(offset, top, right, bottom, 90f, 90f, true)
            }
            else {
                path.moveTo(offset, top)
//                path.moveTo(offset, top + bottomCornerRadius)
            }
        }
    }

    // ------ //

    private fun drawTopNotch(
        path: Path,
        thickness: Float,
        cornerRadius: Float,
        offset: Int,
        notchSetting: NotchSetting,
        outlinesSetting: OutlinesSetting
    ) {
        EdgeNotchDrawer.draw(displayRealSize, path, thickness, cornerRadius, offset, notchSetting, outlinesSetting)
    }

    private fun drawBottomNotch(
        path: Path,
        thickness: Float,
        cornerRadius: Float,
        offset: Int,
        notchSetting: NotchSetting,
        outlinesSetting: OutlinesSetting
    ) {
        EdgeNotchDrawer.draw(displayRealSize, path, thickness, cornerRadius, offset, notchSetting, outlinesSetting)
    }

    private fun drawFloatingNotch(
        path: Path,
        thickness: Float,
        notchSetting: NotchSetting
    ) {
        when (notchSetting.type) {
            NotchType.PUNCH_HOLE ->
                PunchHoleNotchDrawer(displayRealSize).draw(path, Rect(), thickness, notchSetting as PunchHoleNotchSetting)
            // TODO: Rectの扱い

            else -> {}
        }
    }

    private fun drawTopCornerNotch(
        path: Path,
        thickness: Float,
        notchSetting: NotchSetting
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        val verticalCenter = screenHeight / 2
        val rect =
            window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top < verticalCenter
            } ?: return

        CornerNotchDrawer().draw(displayRealSize, path, rect, thickness, notchSetting as CornerNotchSetting)
    }
}
