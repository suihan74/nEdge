package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.suihan74.notificationreporter.R
import com.suihan74.notificationreporter.models.KeywordMatchingType

object AutoCompleteTextViewBindingAdapters {
    @JvmStatic
    @BindingAdapter("android:value")
    fun bindKeywordMatchingType(view: AutoCompleteTextView, value: KeywordMatchingType?) {
        val context = view.context
        if (view.adapter == null) {
            val items = KeywordMatchingType.values().map { context.getString(it.textId) }
            val adapter = ArrayAdapter(context, R.layout.list_item_dropdown, items)
            view.setAdapter(adapter)
        }

        if (value != null) {
            // 第二引数にfalseを与えると、入力後の内容によってドロップダウンの表示項目がフィルタされなくなる
            view.setText(context.getString(value.textId), false)
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value")
    fun bindKeywordMatchingTypeInverse(view: AutoCompleteTextView) : KeywordMatchingType {
        val context = view.context
        return KeywordMatchingType.values().firstOrNull {
            context.getString(it.textId) == view.text.toString()
        } ?: KeywordMatchingType.NONE
    }

    @JvmStatic
    @BindingAdapter("android:valueAttrChanged")
    fun bindListeners(view: AutoCompleteTextView, listener: InverseBindingListener?) {
        view.setOnItemClickListener { _, _, _, _ ->
            listener?.onChange()
        }
    }
}
