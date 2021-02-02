package com.suihan74.nedge.scenes.lockScreen.dataBinding

import android.service.notification.StatusBarNotification
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.suihan74.nedge.database.notification.NotificationEntity
import com.suihan74.nedge.database.notification.isDefault
import com.suihan74.nedge.models.InformationDisplayMode
import com.suihan74.utilities.extensions.text
import com.suihan74.utilities.extensions.title

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
     * 通知発生した設定の表示名を表示する
     */
    @JvmStatic
    @BindingAdapter("notification", "notificationEntity")
    fun setNotificationDisplayName(textView: TextView, sbn: StatusBarNotification?, entity: NotificationEntity?) {
        val visible =
            sbn != null && (entity?.setting?.informationDisplayMode?.code?.and(InformationDisplayMode.Label.code) ?: 0) > 0

        ViewBindingAdapters.setNotificationVisibility(textView, visible) {
            textView.text = when {
                sbn?.packageName == null -> ""

                entity?.displayName != null && !entity.isDefault -> entity.displayName

                else -> {
                    val pm = textView.context.packageManager
                    val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
                    pm.getApplicationLabel(appInfo)
                }
            }
        }
    }

    /**
     * 通知内容を表示する
     */
    @JvmStatic
    @BindingAdapter("notificationText", "notificationEntity")
    fun setNotificationText(textView: TextView, sbn: StatusBarNotification?, entity: NotificationEntity?) {
        val informationDisplayMode = entity?.setting?.informationDisplayMode ?: InformationDisplayMode.NONE
        val showTitle = informationDisplayMode.code.and(InformationDisplayMode.TITLE.code) > 0
        val showText = informationDisplayMode.code.and(InformationDisplayMode.TEXT.code) > 0
        val visible = sbn != null && (showTitle || showText)

        ViewBindingAdapters.setNotificationVisibility(textView, visible) {
            textView.text =
                sbn?.notification?.let {
                    val title = sbn.notification.title
                    val text = sbn.notification.text

                    when {
                        showTitle && showText -> "$title : $text"
                        showTitle -> title
                        showText -> text
                        else -> ""
                    }
                } ?: ""
        }
    }
}
