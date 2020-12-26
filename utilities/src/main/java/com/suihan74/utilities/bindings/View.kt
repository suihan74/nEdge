package com.suihan74.utilities.bindings

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter

/**
 * 真偽値で`android:visibility`を設定
 */
@BindingAdapter(
    value = ["android:visibility", "falseVisibility"],
    requireAll = false
)
fun View.setVisibility(b: Boolean?, falseVisibility: Int? = View.GONE) {
    this.visibility =
        if (b == true) View.VISIBLE
        else falseVisibility ?: View.GONE
}

// ------ //

/**
 * `View`に対するBindingAdapter
 *
 * 拡張関数としてコード上では扱う必要がないものは全てここに含める
 */
object ViewBindingAdapters {
    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    @BindingAdapter("android:onTouch")
    fun bindOnTouchListener(view: View, listener: ((View, MotionEvent) -> Boolean)?) {
        if (listener == null) {
            view.setOnTouchListener(null)
        } else {
            view.setOnTouchListener { _, motionEvent ->
                if (motionEvent == null) true
                else listener(view, motionEvent)
            }
        }
    }
}
