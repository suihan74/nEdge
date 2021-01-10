package com.suihan74.notificationreporter.scenes.preferences.notch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suihan74.notificationreporter.models.NotchSetting
import com.suihan74.notificationreporter.scenes.preferences.page.SettingEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ノッチ設定画面用ViewModelベースクラス
 */
abstract class NotchSettingViewModel<NotchSettingT : NotchSetting>(
    notchPosition: NotchPosition,
    editorViewModel: SettingEditorViewModel,
) : ViewModel() {

    @Suppress("unchecked_cast")
    val setting =
        when (notchPosition) {
            NotchPosition.TOP -> editorViewModel.topNotchSetting
            NotchPosition.BOTTOM -> editorViewModel.bottomNotchSetting
        } as MutableLiveData<NotchSettingT>

    val editing =
        when (notchPosition) {
            NotchPosition.TOP -> editorViewModel.editingTopNotch
            NotchPosition.BOTTOM -> editorViewModel.editingBottomNotch
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
