package com.suihan74.utilities.dataBinding

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter

/**
 * 真偽値で`android:visibility`を設定
 */
@BindingAdapter(
    value = ["android:visibility", "trueVisibility", "falseVisibility"],
    requireAll = false
)
fun View.setVisibility(b: Boolean?, trueVisibility: Int? = View.VISIBLE, falseVisibility: Int? = View.GONE) {
    this.visibility =
        if (b == true) trueVisibility ?: View.VISIBLE
        else falseVisibility ?: View.GONE
}

/**
 * リストが空の場合非表示にする
 */
@BindingAdapter(
    value = ["android:visibility", "trueVisibility", "falseVisibility"],
    requireAll = false
)
fun View.setVisibility(list: List<*>?, trueVisibility: Int? = View.VISIBLE, falseVisibility: Int? = View.GONE) {
    this.visibility =
        if (list.isNullOrEmpty()) falseVisibility ?: View.GONE
        else trueVisibility ?: View.VISIBLE
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

    /**
     * 直接`android:backgroundTint`に色intを渡す
     */
    @JvmStatic
    @BindingAdapter("android:backgroundTint")
    fun bindBackgroundTint(view: View, color: Int?) {
        view.backgroundTintList = color?.let { ColorStateList.valueOf(it) }
    }
}
