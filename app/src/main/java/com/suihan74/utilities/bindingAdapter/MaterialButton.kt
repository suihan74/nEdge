package com.suihan74.utilities.bindingAdapter

import android.graphics.drawable.Drawable
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton

/**
 * ボタンに関連する汎用的なBindingAdapter
 */
object MaterialButtonBindingAdapters {
    @JvmStatic
    @BindingAdapter("iconState", "iconTrue", "iconFalse")
    fun bindIconWithFlag(button: MaterialButton, iconState: Boolean?, iconTrue: Drawable?, iconFalse: Drawable?) {
        button.icon = when (iconState) {
            true -> iconTrue
            else -> iconFalse
        }
    }
}
