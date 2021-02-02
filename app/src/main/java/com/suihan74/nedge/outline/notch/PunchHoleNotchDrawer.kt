package com.suihan74.nedge.outline.notch

import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import com.suihan74.nedge.models.PunchHoleNotchSetting
import com.suihan74.utilities.extensions.dp

class PunchHoleNotchDrawer(displaySize: Point) : FloatingNotchDrawer<PunchHoleNotchSetting>(displaySize) {
    override fun draw(
        path: Path,
        rect: Rect,
        thickness: Float,
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
