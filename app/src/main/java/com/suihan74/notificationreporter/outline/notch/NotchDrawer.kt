package com.suihan74.notificationreporter.outline.notch

import android.graphics.Path
import android.graphics.Rect
import com.suihan74.notificationreporter.models.NotchSetting

/**
 * ノッチパスの描画
 */
interface NotchDrawer<SettingType: NotchSetting> {
    fun draw(
        path: Path,
        rect: Rect,
        thickness: Float,
        notchSetting: SettingType
    )
}
