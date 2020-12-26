package com.suihan74.notificationreporter.scenes.lockScreen.dataBinding

import android.graphics.Color
import android.service.notification.StatusBarNotification
import android.view.View
import androidx.annotation.FloatRange
import androidx.databinding.BindingAdapter

object ViewBindingAdapters {
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
            //view.setBackgroundColor(Color.GREEN)
            view.visibility = View.VISIBLE
        }
    }
}
