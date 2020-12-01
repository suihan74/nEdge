package com.suihan74.utilities.bindings

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider

object SliderBindingAdapters {
    /** 双方向バインドのためのリスナを設定する */
    @JvmStatic
    @BindingAdapter("android:valueAttrChanged")
    fun bindListeners(slider: Slider, valueAttrChanged: InverseBindingListener?) {
        slider.addOnChangeListener { _, _, _ ->
            valueAttrChanged?.onChange()
        }
    }

    // ------ //
    // "android:value"の双方向バインド

    @JvmStatic
    @BindingAdapter("android:value")
    fun bindValue(slider: Slider, value: Float?) {
        if (value != null && slider.value != value) {
            slider.value = value
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value")
    fun bindValueInverse(slider: Slider) : Float {
        return slider.value
    }
}
