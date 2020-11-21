package com.suihan74.utilities.bindings

import android.view.View
import androidx.databinding.BindingAdapter

/**
 * 真偽値で`android:visibility`を設定
 */
@BindingAdapter(value = ["android:visibility", "falseVisibility"], requireAll = false)
fun View.setVisibility(b: Boolean?, falseVisibility: Int?) {
    this.visibility =
        if (b == true) View.VISIBLE
        else falseVisibility ?: View.GONE
}
