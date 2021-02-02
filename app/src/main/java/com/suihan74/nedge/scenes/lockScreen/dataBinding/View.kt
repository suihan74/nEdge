package com.suihan74.nedge.scenes.lockScreen.dataBinding

import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.databinding.BindingAdapter
import com.suihan74.nedge.models.NotificationSetting
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
    fun setNotificationVisibility(view: View, setting: NotificationSetting?) {
        setNotificationVisibility(view, setting, null)
    }

    /** 通知バー表示状態切り替え */
    @JvmStatic
    fun setNotificationVisibility(view: View, setting: Any?, endAction: (()->Unit)?) {
        setNotificationVisibility(view, setting != null, endAction)
    }

    /** 通知バー表示状態切り替え */
    @JvmStatic
    fun setNotificationVisibility(view: View, visible: Boolean, endAction: (()->Unit)?) {
        if (visible) {
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
        else {
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
    }

    @JvmStatic
    @BindingAdapter("android:layout_marginBottom")
    fun setMarginBottom(view: View, marginBottom: Float?) {
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(bottom = marginBottom?.toInt() ?: 0)
        }
    }

    @JvmStatic
    @BindingAdapter("layout_goneMarginBottom")
    fun setGoneMarginBottom(view: View, marginBottom: Float?) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            goneBottomMargin = marginBottom?.toInt() ?: 0
        }
    }
}
