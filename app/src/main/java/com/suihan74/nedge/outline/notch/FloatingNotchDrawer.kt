package com.suihan74.nedge.outline.notch

import android.graphics.Point
import com.suihan74.nedge.models.NotchSetting

/**
 * 辺・角に関係なく画面内に描画するノッチパスの描画
 */
abstract class FloatingNotchDrawer<SettingType : NotchSetting>(
    displaySize: Point
) : NotchDrawer<SettingType> {
    protected val screenWidth = displaySize.x
    protected val screenHeight = displaySize.y
}
