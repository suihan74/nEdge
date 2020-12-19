package com.suihan74.notificationreporter.scenes.preferences

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.repositories.PreferencesRepository
import kotlinx.coroutines.launch

class PreferencesViewModel(
    prefRepo: PreferencesRepository
) : ViewModel() {
    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : MutableLiveData<Float> = prefRepo.lightLevel

    /** デフォルトの通知表示 */
    val notificationSetting = prefRepo.defaultNotificationSetting.also {
        it.observeForever { value ->
            viewModelScope.launch {
                prefRepo.updateDefaultNotificationSetting(value)
            }
        }
    }

    /** 通知表示の輪郭線の色 */
    val notificationColor = MutableLiveData(notificationSetting.value?.color ?: Color.WHITE).also {
        it.observeForever { changedColor ->
            val prevValue = notificationSetting.value
            if (prevValue != null && prevValue.color != changedColor) {
                notificationSetting.value = prevValue.copy(
                    color = changedColor
                )
            }
        }
    }

    // ------ //

    init {
        viewModelScope.launch {
            prefRepo.init()
        }
    }
}
