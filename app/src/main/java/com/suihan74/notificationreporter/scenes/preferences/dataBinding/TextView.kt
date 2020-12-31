package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

object TextViewBindingAdapters {
    /**
     * 時刻を表示する
     */
    @JvmStatic
    @BindingAdapter(value = ["localTime", "dateTimeFormat"], requireAll = false)
    fun setLocalTimeInt(textView: TextView, value: LocalTime?, dateTimeFormat: String?) {
        textView.text =
            if (value == null) ""
            else try {
                val formatter = DateTimeFormatter.ofPattern(dateTimeFormat ?: "HH:mm")
                value.format(formatter)
            }
            catch (e: DateTimeException) {
                Log.d("localTime", Log.getStackTraceString(e))
                ""
            }
    }
}
