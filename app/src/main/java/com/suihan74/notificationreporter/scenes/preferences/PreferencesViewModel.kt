package com.suihan74.notificationreporter.scenes.preferences

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.notificationreporter.repositories.PreferencesRepository

class PreferencesViewModel(
    prefRepo: PreferencesRepository
) : ViewModel() {
    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : MutableLiveData<Float> = prefRepo.lightLevel

    /** デフォルトの通知表示 */
    val notificationSetting = prefRepo.defaultNotificationSetting

    /** 通知表示の輪郭線の色 */
    val notificationColor = MutableLiveData(notificationSetting.value?.color ?: Color.WHITE).also {
        it.observeForever { changedColor ->
            if (notificationSetting.value?.color != changedColor) {
                notificationSetting.value = notificationSetting.value?.copy(
                    color = changedColor
                )
            }
        }
    }
}
