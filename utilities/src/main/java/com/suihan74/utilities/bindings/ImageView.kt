package com.suihan74.utilities.bindings

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter

object ImageViewBindingAdapters {
    @JvmStatic
    @BindingAdapter("src")
    fun setSource(imageView: ImageView, @DrawableRes srcId: Int?) {
        if (srcId == null || srcId == 0x0) {
            imageView.setImageDrawable(null)
        }
        else {
            imageView.setImageResource(srcId)
        }
    }
}
