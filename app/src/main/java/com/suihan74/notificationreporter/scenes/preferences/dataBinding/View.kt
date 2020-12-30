package com.suihan74.notificationreporter.scenes.preferences.dataBinding

import android.view.View
import androidx.databinding.BindingAdapter
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import com.suihan74.utilities.dataBinding.setVisibility
import kotlin.math.absoluteValue

object ViewBindingAdapters {
    /**
     * ライトレベル調整時に追加の黒前景を表示する場合のプレビュー
     */
    @JvmStatic
    @BindingAdapter("editingLightLevel", "lightLevel")
    fun bindLightLevelOffPreview(
        view: View,
        editingLightLevel: PreferencesViewModel.EditingLightLevel?,
        lightLevel: Float?
    ) {
        if (editingLightLevel != PreferencesViewModel.EditingLightLevel.OFF || lightLevel == null) {
            view.visibility = View.GONE
            return
        }

        val active = lightLevel < .0f
        view.setVisibility(active)
        if (active) {
            view.alpha = lightLevel.absoluteValue
        }
    }
}
