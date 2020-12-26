package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

object TextViewBindingAdapters {
    /**
     * Int値を時刻として表示する
     */
    @JvmStatic
    @BindingAdapter("localTimeInt")
    fun setLocalTimeInt(textView: TextView, value: Int?) {
        textView.text =
            if (value == null) ""
            else try {
                LocalTime.ofSecondOfDay(value.toLong()).format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            catch (e: DateTimeException) {
                Log.d("localTimeInt", Log.getStackTraceString(e))
                ""
            }
    }
}
