package com.suihan74.notificationreporter.scenes.lockScreen.dataBinding

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.widget.TextView
import androidx.databinding.BindingAdapter

object TextViewBindingAdapters {
    /** バッテリのパーセンテージ表示 */
    @JvmStatic
    @BindingAdapter("batteryLevel")
    fun setBatteryLevelText(textView: TextView, batteryLevel: Int?) {
        textView.text =
            if (batteryLevel == null) ""
            else "$batteryLevel%"
    }

    /**
     * 通知発生したアプリ名を表示する
     */
    @JvmStatic
    @BindingAdapter("notificationAppName")
    fun setNotificationAppName(textView: TextView, sbn: StatusBarNotification?) {
        textView.text =
            if (sbn == null) ""
            else {
                val pm = textView.context.packageManager
                val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                pm.getApplicationLabel(appInfo)
            }
    }

    /**
     * 通知内容を表示する
     */
    @JvmStatic
    @BindingAdapter("notificationText")
    fun setNotificationText(textView: TextView, sbn: StatusBarNotification?) {
        textView.text =
            if (sbn == null) ""
            else {
                val (title, text) = sbn.notification.extras.run {
                    getString(Notification.EXTRA_TITLE) to getString(Notification.EXTRA_TEXT)
                }
                if (title == null || text == null) ""
                else "$title : $text"
            }
    }
}
