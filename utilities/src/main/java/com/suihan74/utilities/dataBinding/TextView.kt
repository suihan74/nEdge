package com.suihan74.utilities.dataBinding

import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TextViewBindingAdapters {
    @JvmStatic
    @BindingAdapter("textId")
    fun setTextId(textView: TextView, @StringRes textId: Int?) {
        if (textId == null || textId == 0) {
            textView.text = ""
        }
        else {
            textView.setText(textId)
        }
    }

    @JvmStatic
    @BindingAdapter("dateTime", "format")
    fun setLocalDateTime(textView: TextView, dateTime: LocalDateTime?, formatPattern: String?) {
        if (dateTime == null || formatPattern == null) {
            textView.text = ""
        }
        else {
            val formatter = DateTimeFormatter.ofPattern(formatPattern)
            textView.text = dateTime.format(formatter)
        }
    }

}
