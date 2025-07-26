package com.suihan74.nedge.scenes.preferences.dataBinding

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.slider.Slider
import com.suihan74.nedge.scenes.preferences.PreferencesViewModel
import com.suihan74.nedge.scenes.preferences.SliderWithButtons

object SliderBindingAdapters {
    data class LightLevelStateCache(
        val current: PreferencesViewModel.EditingLightLevel,
        val onState: PreferencesViewModel.EditingLightLevel,
        val offState: PreferencesViewModel.EditingLightLevel,
        val lifecycle: Lifecycle
    )

    private val lightLevelStates = HashMap<SliderWithButtons, LightLevelStateCache>()

    /** Lifecycleに紐づいた`Slider`の情報を除去する */
    fun onTerminateLifecycle(owner: LifecycleOwner) {
        val lifecycle = owner.lifecycle
        val targets = lightLevelStates.filter { it.value.lifecycle == lifecycle }
        targets.forEach {
            lightLevelStates.remove(it.key)
        }
    }

    // ------ //

    @JvmStatic
    @BindingAdapter("state", "onState", "offState", "lifecycle")
    fun bindLightLevel(
        slider: SliderWithButtons,
        state: PreferencesViewModel.EditingLightLevel?,
        onState: PreferencesViewModel.EditingLightLevel?,
        offState: PreferencesViewModel.EditingLightLevel?,
        lifecycle: Lifecycle
    ) {
        val current = state ?: offState ?: return
        lightLevelStates[slider] = LightLevelStateCache(
            current,
            onState!!,
            offState!!,
            lifecycle
        )
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "state")
    fun bindLightLevelInverse(slider: SliderWithButtons) : PreferencesViewModel.EditingLightLevel? {
        return lightLevelStates[slider]?.current
    }

    @JvmStatic
    @BindingAdapter("stateAttrChanged")
    fun bindLightLevelListeners(view: SliderWithButtons, stateAttrChanged: InverseBindingListener?) {
        view.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                lightLevelStates[view]?.let {
                    lightLevelStates[view] = it.copy(current = it.onState)
                }
                stateAttrChanged?.onChange()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                lightLevelStates[view]?.let {
                    lightLevelStates[view] = it.copy(current = it.offState)
                }
                stateAttrChanged?.onChange()
            }
        })
    }
}
