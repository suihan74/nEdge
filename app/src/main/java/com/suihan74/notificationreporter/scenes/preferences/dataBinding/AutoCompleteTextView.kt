package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.view.View
import android.widget.AdapterView
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
    fun bindValue(view: AutoCompleteTextView, value: KeywordMatchingType?) {
        if (view.adapter == null) {
            val context = view.context
            val items = KeywordMatchingType.values().map { context.getString(it.textId) }
            view.setAdapter(ArrayAdapter(context, R.layout.list_item_dropdown, items))
        }
        view.listSelection = KeywordMatchingType.values().indexOf(value)
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value")
    fun bindValueInverse(view: AutoCompleteTextView) : KeywordMatchingType {
        return KeywordMatchingType.values().getOrElse(view.listSelection) { KeywordMatchingType.NONE }
    }

    @JvmStatic
    @BindingAdapter("android:valueAttrChanged")
    fun bindListeners(view: AutoCompleteTextView, listener: InverseBindingListener?) {
        view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                listener?.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                listener?.onChange()
            }
        }
    }
}
