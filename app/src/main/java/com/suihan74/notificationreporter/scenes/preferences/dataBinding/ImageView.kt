package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.models.NotificationSetting
import com.suihan74.notificationreporter.outline.OutlineDrawer

object ImageViewBindingAdapters {
    /**
     * `ImageView`に通知表示を描画する
     */
    @JvmStatic
    @BindingAdapter("notificationSetting")
    fun setNotificationSetting(imageView: ImageView, setting: NotificationSetting?) {
        if (setting == null) {
            imageView.setImageDrawable(null)
            return
        }

        try {
            val activity = imageView.context as? Activity ?: return
            val outlineDrawer = OutlineDrawer(activity.window)
            outlineDrawer.draw(imageView, setting)
        }
        catch (e: Throwable) {
            Log.e("notificationSetting", Log.getStackTraceString(e))
            imageView.setImageDrawable(null)
        }
    }

    /**
     * 他のアプリのアイコンを読み込む
     */
    @JvmStatic
    @BindingAdapter("applicationIcon")
    fun setApplicationIcon(imageView: ImageView, appInfo: ApplicationInfo?) {
        imageView.setImageDrawable(appInfo?.loadIcon(imageView.context.packageManager))
    }
}
