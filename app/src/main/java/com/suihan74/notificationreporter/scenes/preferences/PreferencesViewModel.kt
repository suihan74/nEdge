package com.suihan74.notificationreporter.scenes.preferences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.suihan74.notificationreporter.repositories.PreferencesRepository

class PreferencesViewModel(
    prefRepo: PreferencesRepository
) : ViewModel() {
    /** バックライト消灯後の画面をさらに暗くする度合い */
    val lightLevel : MutableLiveData<Float> = prefRepo.lightLevel
}
