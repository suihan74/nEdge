package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.scenes.preferences.PreferencesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class NotchSettingViewModel<NotchSettingT : NotchSetting>(
    notchPosition: NotchPosition,
    preferencesViewModel: PreferencesViewModel,
) : ViewModel() {

    @Suppress("unchecked_cast")
    val setting =
        when (notchPosition) {
            NotchPosition.TOP -> preferencesViewModel.topNotchSetting
            NotchPosition.BOTTOM -> preferencesViewModel.bottomNotchSetting
        } as MutableLiveData<NotchSettingT>

    val editing =
        when (notchPosition) {
            NotchPosition.TOP -> preferencesViewModel.editingTopNotch
            NotchPosition.BOTTOM -> preferencesViewModel.editingBottomNotch
        }

    // ------ //

    init {
        viewModelScope.launch(Dispatchers.Main) {
            notchSettingMutex.withLock {
                initialize()
            }
        }
    }

    // ------ //

    private val notchSettingMutex = Mutex()

    protected abstract suspend fun initialize()

    protected fun <T> mutableLiveData() =
        MutableLiveData<T>().apply {
            observeForever {
                viewModelScope.launch(Dispatchers.Main) {
                    notchSettingMutex.withLock {
                        updateNotchSetting()
                    }
                }
            }
        }

    protected abstract suspend fun updateNotchSetting()
}
