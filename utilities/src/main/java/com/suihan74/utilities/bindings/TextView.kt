package com.suihan74.utilities.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@BindingAdapter("dateTime", "format")
fun TextView.setLocalDateTime(dateTime: LocalDateTime?, formatPattern: String?) {
    if (dateTime == null || formatPattern == null) {
        this.text = ""
        return
    }

    val formatter = DateTimeFormatter.ofPattern(formatPattern)
    this.text = dateTime.format(formatter)
}
