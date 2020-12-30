package com.suihan74.utilities.dataBinding

import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

object ButtonBindingAdapters {
    @JvmStatic
    @BindingAdapter("src")
    fun setSource(button: Button, @DrawableRes srcId: Int?) {
        val context = button.context
        if (srcId == null || srcId == 0x0) {
            button.foreground = null
        }
        else {
            button.foreground = ContextCompat.getDrawable(context, srcId)
        }
    }
}
