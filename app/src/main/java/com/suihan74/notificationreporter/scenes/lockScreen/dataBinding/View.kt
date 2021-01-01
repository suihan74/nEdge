package com.suihan74.notificationreporter.scenes.lockScreen.dataBinding

import android.service.notification.StatusBarNotification
import android.view.View
import androidx.annotation.FloatRange
import androidx.databinding.BindingAdapter
import kotlin.math.absoluteValue

object ViewBindingAdapters {
    /** 消灯後の画面の暗さを調節 */
    @JvmStatic
    @BindingAdapter("lightLevel")
    fun setLightLevel(view: View, @FloatRange(from = -1.0, to = 1.0) lightLevel: Float?) {
        if (lightLevel == null) {
            view.visibility = View.GONE
        }
        else {
            view.alpha =
                if (lightLevel >= .0f) .0f
                else lightLevel.absoluteValue
            view.visibility = View.VISIBLE
        }
    }

    /** 通知バー表示状態切り替え */
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setNotificationVisibility(view: View, notification: StatusBarNotification?) {
        setNotificationVisibility(view, notification, null)
    }

    /** 通知バー表示状態切り替え */
    @JvmStatic
    fun setNotificationVisibility(view: View, notification: StatusBarNotification?, endAction: (()->Unit)?) {
        if (notification == null) {
            if (view.visibility == View.VISIBLE) {
                view.animate()
                    .alphaBy(1f)
                    .alpha(.0f)
                    .withEndAction {
                        endAction?.invoke()
                        view.visibility = View.GONE
                    }
                    .setDuration(600L)
                    .start()
            }
            else {
                view.visibility = View.GONE
            }
        }
        else {
            if (view.visibility == View.GONE) {
                view.animate()
                    .alphaBy(.0f)
                    .alpha(1f)
                    .withStartAction {
                        view.visibility = View.VISIBLE
                    }
                    .setDuration(600L)
                    .start()
            }
            else {
                view.animate()
                    .alphaBy(1f)
                    .alpha(.0f)
                    .withEndAction {
                        endAction?.invoke()
                        view.animate()
                            .setStartDelay(150L)
                            .alphaBy(.0f)
                            .alpha(1f)
                            .setDuration(600L)
                            .start()
                    }
                    .setDuration(600L)
                    .start()
            }
        }
    }
}
