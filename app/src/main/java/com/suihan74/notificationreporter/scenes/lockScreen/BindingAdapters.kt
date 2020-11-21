package com.suihan74.notificationreporter.scenes.lockScreen

import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.R
import com.suihan74.utilities.extensions.appendDrawable

object BindingAdapters {
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
}
