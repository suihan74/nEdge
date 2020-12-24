package com.suihan74.utilities.bindings

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import java.lang.ref.WeakReference
import java.util.*

object SliderBindingAdapters {
    private val editingStates = HashSet<WeakReference<Slider>>()

    /** 双方向バインドのためのリスナを設定する */
    @JvmStatic
    @BindingAdapter(value = ["android:valueAttrChanged", "editingAttrChanged"], requireAll = false)
    fun bindListeners(
        slider: Slider,
        valueAttrChanged: InverseBindingListener?,
        editingAttrChanged: InverseBindingListener?
    ) {
        slider.addOnChangeListener { _, _, _ ->
            valueAttrChanged?.onChange()
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                editingStates.add(WeakReference(slider))
                editingAttrChanged?.onChange()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                editingStates.removeAll { wRef ->
                    wRef.get().let {
                        it == null || it == slider
                    }
                }
                editingAttrChanged?.onChange()
            }
        })
    }

    // ------ //
    // "android:value"の双方向バインド

    @JvmStatic
    @BindingAdapter(value = [
        "android:value",
        "android:valueFrom",
        "android:valueTo",
        "android:stepSize",
        "editing"],
        requireAll = false
    )
    fun bindValue(
        slider: Slider,
        value: Float?,
        lowerBound: Float?,
        upperBound: Float?,
        stepSize: Float?,
        editing: Boolean?
    ) {
        lowerBound?.let { slider.valueFrom = it }
        upperBound?.let { slider.valueFrom = it }
        stepSize?.let { slider.stepSize = it }

        if (value != null && slider.value != value) {
            slider.value = value
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value")
    fun bindValueInverse(slider: Slider) : Float {
        return slider.value
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "editing")
    fun bindEditingInverse(slider: Slider) : Boolean {
        return editingStates.any { wRef -> wRef.get() == slider }
    }
}
