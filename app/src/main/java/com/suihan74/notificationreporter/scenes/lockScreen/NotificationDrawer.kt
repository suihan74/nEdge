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
        val topCornerRadius = notificationSetting.outlinesSetting.topCornerRadius

        path.moveTo(offset + topCornerRadius, offset)
        drawTopNotch(path, thickness, notificationSetting.topNotchSetting)
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
        val bottomCornerRadius = notificationSetting.outlinesSetting.bottomCornerRadius

        path.moveTo(right - bottomCornerRadius, bottom)
        drawBottomNotch(path, thickness, notificationSetting.bottomNotchSetting)
        path.lineTo(offset + bottomCornerRadius, bottom)
    }

    // ------ //

    private fun drawTopNotch(
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

    private fun drawBottomNotch(
        path: Path,
        thickness: Float,
        notchSetting: NotchSetting
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

        val verticalCenter = screenHeight / 2
        val rect =
            window.decorView.rootWindowInsets.displayCutout?.boundingRects?.firstOrNull {
                it.top > verticalCenter
            } ?: return

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

    // ------ //

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

        path.run {
            notchSetting.leftTopRadius.let { r ->
                // top left
                lineTo(left - r, top)
                arcTo(left - r * 2, top, left, top + r * 2, 270f, 90f, false)
            }

            notchSetting.leftBottomRadius.let { r ->
                // bottom left
                lineTo(left, bottom - r)
                arcTo(left, bottom - r * 2, left + r * 2, bottom, 180f, -90f, false)
            }

            notchSetting.rightBottomRadius.let { r ->
                // bottom edge
                lineTo(right - r, bottom)

                // bottom right
                arcTo(right - r * 2, bottom - r * 2, right, bottom, 90f, -90f, false)
            }

            notchSetting.rightTopRadius.let { r ->
                // top right
                lineTo(right, top + r)
                arcTo(right, top, right + r * 2, top + r * 2, 180f, 90f, false)
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

        path.apply {
            notchSetting.rightTopRadius.let { r ->
                // bottom right
                lineTo(right + r, bottom)
                arcTo(right, bottom - r * 2, right + r * 2, bottom, 90f, 90f, true)
            }

            notchSetting.rightBottomRadius.let { r ->
                // top right
                lineTo(right, top + r)
                arcTo(right - r * 2, top, right, top + r * 2, 0f, -90f, true)
            }

            notchSetting.leftBottomRadius.let { r ->
                // top edge
                lineTo(left + r, top)

                // top left
                arcTo(left, top, left + r * 2, top + r * 2, 270f, -90f, true)
            }

            notchSetting.leftTopRadius.let { r ->
                // bottom left
                lineTo(left, bottom - r)
                arcTo(left - r * 2, bottom - r * 2, left, bottom, 0f, 90f, true)
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

        val rootRadius = notchSetting.topRadius
        val rootDegree = notchSetting.heightAdjustment

        val left = rect.left - offset - widthAdjustmentPx
        val right = rect.right + offset + widthAdjustmentPx
        val top = rect.top + offset

        path.apply {
            val lx = left - rootRadius
            val rx = right + rootRadius
            val ly = top + rootRadius

            // top left
            lineTo(lx, top)
            arcTo(lx - rootRadius, top, lx + rootRadius, ly + rootRadius, 270f, rootDegree, false)

            // water drop
            val (sin, cos) = (PI * rootDegree / 180f).let {
                sin(it) to cos(it)
            }
            val r = ((rx - lx - 2 * rootRadius * sin) / (2 * sin)).toFloat()
            val cx = (lx + (rootRadius + r) * sin).toFloat()
            val cy = (ly - (rootRadius + r) * cos).toFloat()
            arcTo(cx - r, cy - r, cx + r, cy + r, 90f + rootDegree, -rootDegree * 2, false)

            // top right
            arcTo(right, top, rx + rootRadius, ly + rootRadius, 270f - rootDegree, rootDegree, false)
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
