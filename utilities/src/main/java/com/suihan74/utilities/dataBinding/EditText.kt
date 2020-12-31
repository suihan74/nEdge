package com.suihan74.utilities.dataBinding

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

object EditTextBindingAdapters {
    @JvmStatic
    @BindingAdapter("android:text")
    fun bindTextLong(editText: EditText, value: Long?) {
        if (value == null) {
            editText.text.clear()
        }
        else {
            editText.text.clear()
            editText.text.append(value.toString())
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:text")
    fun bindTextLongInverse(editText: EditText) : Long {
        return editText.text.toString().toLongOrNull() ?: 0L
    }

    @JvmStatic
    @BindingAdapter("android:textAttrChanged")
    fun bindTextLongListener(editText: EditText, listener: InverseBindingListener?) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onChange()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
