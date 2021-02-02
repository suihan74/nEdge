package com.suihan74.nedge.scenes.preferences.dataBinding

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.graphics.*
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.suihan74.nedge.models.NotchSetting
import com.suihan74.nedge.models.NotificationSetting
import com.suihan74.nedge.outline.OutlineDrawer
import com.suihan74.utilities.extensions.dp

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

// ------ //

/**
 * 輪郭線の編集プレビュー用のアダプタ
 */
object ImageViewOutlinesPreviewBindingAdapters {
    /**
     * ノッチ背景描画
     */
    @JvmStatic
    @BindingAdapter("topNotchSetting", "bottomNotchSetting", "editingTopNotch", "editingBottomNotch")
    fun bindPreviewNotchBackground(
        imageView: ImageView,
        topNotchSetting: NotchSetting?,
        bottomNotchSetting: NotchSetting?,
        editingTopNotch: Boolean?,
        editingBottomNotch: Boolean?
    ) {
        if (editingTopNotch != true && editingBottomNotch != true) {
            imageView.setImageDrawable(null)
            imageView.visibility = ImageView.GONE
            return
        }

        val bitmap = Bitmap.createBitmap(imageView.rootView.width, imageView.rootView.height, Bitmap.Config.ARGB_8888)
        if (editingTopNotch == true) {
            topNotchSetting?.let { drawNotchRect(bitmap, it) }
        }
        if (editingBottomNotch == true) {
            bottomNotchSetting?.let { drawNotchRect(bitmap, it) }
        }

        imageView.setImageBitmap(bitmap)
        imageView.visibility = ImageView.VISIBLE
    }

    @JvmStatic
    private fun drawNotchRect(bitmap: Bitmap, notchSetting: NotchSetting) {
        Canvas(bitmap).let { canvas ->
            val rect = notchSetting.rect.let {
                val surplus = 20.dp.toInt()
                Rect(it.left - surplus, it.top - surplus, it.right + surplus, it.bottom + surplus)
            }

            canvas.drawRect(rect, Paint().apply {
                color = Color.argb(0xff, 0xff, 0x00, 0xff)
            })
        }
    }
}
