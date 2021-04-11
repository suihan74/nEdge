package com.suihan74.utilities.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

val View.activity : Activity? get() = context.activity

/**
 * キーボードを隠して入力対象のビューをアンフォーカスする
 */
fun View.hideSoftInputMethod(focusTarget: View? = null) : Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val result = imm?.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    clearFocus()

    focusTarget?.let { target ->
        target.isFocusable = true
        target.isFocusableInTouchMode = true
        target.requestFocus()
    }

    return result ?: false
}
