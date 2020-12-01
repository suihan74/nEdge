package com.suihan74.notificationreporter.scenes.lockScreen

import android.content.pm.PackageManager
import android.graphics.Color
import android.service.notification.StatusBarNotification
import android.view.View
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.appcompat.widget.ThemeUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.R
import com.suihan74.utilities.extensions.appendDrawable

object BindingAdapters {
    /** バッテリのパーセンテージ表示 */
    @JvmStatic
    @BindingAdapter("batteryLevel", "charging")
    fun setBatteryLevelText(textView: TextView, batteryLevel: Int?, charging: Boolean?) {
        if (batteryLevel == null) {
            textView.text = ""
            return
        }

        textView.text = buildSpannedString {
            appendDrawable(
                textView,
                if (charging == true) R.drawable.ic_battery_charging
                else R.drawable.ic_battery_std
            )
            append("$batteryLevel%")
        }
    }

    /** 消灯後の画面の暗さを調節 */
    @JvmStatic
    @BindingAdapter("lightLevel")
    fun setLightLevel(view: View, @FloatRange(from = 0.0, to = 1.0) lightLevel: Float?) {
        if (lightLevel == null) {
            view.setBackgroundColor(Color.TRANSPARENT)
        }
        else {
            val alpha = (255 * (1.0f - lightLevel)).toInt()
            val color = alpha shl 24
            view.setBackgroundColor(color)
        }
    }

    /** 通知バーの色・表示 */
    @JvmStatic
    @BindingAdapter("notifications")
    fun setNotifications(view: View, notifications: List<StatusBarNotification>?) {
        if (notifications.isNullOrEmpty()) {
            view.visibility = View.GONE
        }
        else {
            // TODO: 通知ごとに色を設定する
            view.setBackgroundColor(Color.GREEN)
            view.visibility = View.VISIBLE
        }
    }
}
