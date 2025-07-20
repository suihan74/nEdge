package com.suihan74.nedge.scenes.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import com.google.common.math.IntMath.pow
import com.suihan74.nedge.R
import com.suihan74.nedge.databinding.SliderWithButtonsBinding
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

class SliderWithButtons : ConstraintLayout {
    /**
     * スライダーをInt値として扱うための補正スケール
     *
     * 各設定値がすべてInt値として扱うために乗算する倍数
     * 本来の値が value=0.1f,stepSize=0.01f なら、scale=100
     */
    var scale : Int = 1
        private set

    private val binding : SliderWithButtonsBinding

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(inflater, R.layout.slider_with_buttons, this, true)

        binding.plusButton.setOnClickListener {
            val prev = binding.slider.value
            binding.slider.let {
                it.value = min(it.value + it.stepSize, it.valueTo)
            }
            Log.i("plusButton", "prev=$prev,current=${binding.slider.value},step=${binding.slider.stepSize}")
        }
        binding.minusButton.setOnClickListener {
            binding.slider.let {
                it.value = max(it.value - it.stepSize, it.valueFrom)
            }
        }
    }

    fun addOnSliderTouchListener(listener: Slider.OnSliderTouchListener) {
        binding.slider.addOnSliderTouchListener(listener)
    }

    // ------ //

    object SliderWithButtonsBindingAdapters {
        private val editingStates = HashSet<WeakReference<Slider>>()

        /** 双方向バインドのためのリスナを設定する */
        @JvmStatic
        @BindingAdapter(value = [
            "valueAttrChanged",
            "intValueAttrChanged",
            "editingAttrChanged"
            ], requireAll = false)
        fun bindListeners(
            view: SliderWithButtons,
            valueAttrChanged: InverseBindingListener?,
            intValueAttrChanged: InverseBindingListener?,
            editingAttrChanged: InverseBindingListener?
        ) {
            view.binding.slider.addOnChangeListener { _, _, _ ->
                valueAttrChanged?.onChange()
            }

            view.binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) {
                    editingStates.add(WeakReference(slider))
                    editingAttrChanged?.onChange()
                }

                @SuppressLint("RestrictedApi")
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

        @JvmStatic
        @BindingAdapter(value = [
            "value",
            "valueFrom",
            "valueTo",
            "stepSize"],
            requireAll = false
        )
        fun bindFloatValue(
            view: SliderWithButtons,
            value: Float?,
            valueFrom: Float?,
            valueTo: Float?,
            stepSize: Float?
        ) {
            val slider = view.binding.slider

            // stepSizeを基準にして、スライダー上のすべての値がInt値として扱える範囲に収まるように補正する
            // 中身は実際にはFloat値なので、外部と値をやりとりする際には補正を解除して実際の値を戻す
            val scale =
                pow(
                    10,
                    stepSize?.let {
                        val s = floor(log10(it)).toInt()
                        if (s >= 0) 0
                        else abs(s)
                    } ?: 0
                )
            view.scale = scale

            valueFrom?.let { slider.valueFrom = (it * scale).toInt().toFloat() }
            valueTo?.let { slider.valueTo = (it * scale).toInt().toFloat() }
            stepSize?.let { slider.stepSize = (it * scale).toInt().toFloat() }
            value?.let {
                val v = (it * scale).toInt().toFloat()
                slider.value = max(min(v, slider.valueTo), slider.valueFrom)
            }

            /*
            if (value != null && view.binding.slider.value != value) {
                // スライダーの目盛りが正確に割り振れない値を入れようとするとエラーになるため、補正してから代入する
                val step = max(0, ((min(value, slider.valueTo) - slider.valueFrom) / slider.stepSize).toInt())
                slider.value = slider.valueFrom + step * slider.stepSize
            }*/
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "value")
        fun bindFloatValueInverse(view: SliderWithButtons) : Float {
            return view.binding.slider.value / view.scale
        }

        // ------ //

        @JvmStatic
        @BindingAdapter(value = [
            "intValue",
            "valueFrom",
            "valueTo",
            "stepSize"],
            requireAll = false
        )
        fun bindIntValue(
            view: SliderWithButtons,
            value: Int?,
            valueFrom: Int?,
            valueTo: Int?,
            stepSize: Int?
        ) {
            bindFloatValue(
                view,
                value?.toFloat(),
                valueFrom?.toFloat(),
                valueTo?.toFloat(),
                stepSize?.toFloat()
            )
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "intValue")
        fun bindIntValueInverse(view: SliderWithButtons) : Int {
            return view.binding.slider.value.toInt()
        }

        // ------ //

        @JvmStatic
        @BindingAdapter("editing")
        fun bindEditing(view: SliderWithButtons, editing: Boolean?) {
            editingStates.add(WeakReference(view.binding.slider))
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "editing")
        fun bindEditingInverse(view: SliderWithButtons) : Boolean {
            return editingStates.any { wRef -> wRef.get() == view.binding.slider }
        }
    }
}
