package com.suihan74.utilities.dataBinding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.min

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
        "android:stepSize"],
        requireAll = false
    )
    fun bindFloatValue(
        slider: Slider,
        value: Float?,
        valueFrom: Float?,
        valueTo: Float?,
        stepSize: Float?
    ) {
        valueFrom?.let { slider.valueFrom = it }
        valueTo?.let { slider.valueTo = it }
        stepSize?.let { slider.stepSize = it }

        if (value != null && slider.value != value) {
            // スライダーの目盛りが正確に割り振れない値を入れようとするとエラーになるため、補正してから代入する
            val step = max(0, ((min(value, slider.valueTo) - slider.valueFrom) / slider.stepSize).toInt())
            slider.value = slider.valueFrom + step * slider.stepSize
        }
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value")
    fun bindFloatValueInverse(slider: Slider) : Float {
        return slider.value
    }

    @JvmStatic
    @BindingAdapter(value = [
        "intValue",
        "android:valueFrom",
        "android:valueTo",
        "android:stepSize"],
        requireAll = false
    )
    fun bindIntValue(
        slider: Slider,
        value: Int?,
        valueFrom: Int?,
        valueTo: Int?,
        stepSize: Int?
    ) {
        bindFloatValue(
            slider,
            value?.toFloat(),
            valueFrom?.toFloat(),
            valueTo?.toFloat(),
            stepSize?.toFloat()
        )
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "intValue", event="android:valueAttrChanged")
    fun bindIntValueInverse(slider: Slider) : Int {
        return slider.value.toInt()
    }

    // ------ //

    @JvmStatic
    @BindingAdapter("editing")
    fun bindEditing(slider: Slider, editing: Boolean?) {
        editingStates.add(WeakReference(slider))
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "editing")
    fun bindEditingInverse(slider: Slider) : Boolean {
        return editingStates.any { wRef -> wRef.get() == slider }
    }
}
