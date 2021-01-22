package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.fragment.app.Fragment
import com.suihan74.notificationreporter.models.NotchType

/**
 * ノッチ種類に対応する設定画面フラグメントを生成する
 */
fun NotchType.createSettingFragment(notchPosition: NotchPosition) : Fragment = when (this) {
    NotchType.NONE -> Fragment() // dummy
    NotchType.RECTANGLE -> RectangleNotchSettingFragment.createInstance(notchPosition)
    NotchType.WATER_DROP -> WaterDropNotchSettingFragment.createInstance(notchPosition)
    NotchType.PUNCH_HOLE -> PunchHoleNotchSettingFragment.createInstance(notchPosition)
    NotchType.CORNER -> CornerNotchSettingFragment.createInstance(notchPosition)
}
