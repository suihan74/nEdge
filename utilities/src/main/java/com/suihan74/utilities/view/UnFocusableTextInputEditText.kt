package com.suihan74.utilities.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

/**
 * 戻るボタンでフォーカスを失うようにした`TextInputEditText`
 */
class UnFocusableTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    styleId: Int = R.attr.editTextStyle
) : TextInputEditText(context, attrs, styleId) {
    override fun clearFocus() {
        isFocusable = false
        isFocusableInTouchMode = false
        super.clearFocus()
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if(event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
        }
        return super.onKeyPreIme(keyCode, event)
    }
}
